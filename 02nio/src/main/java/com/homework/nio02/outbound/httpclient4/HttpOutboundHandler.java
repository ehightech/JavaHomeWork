package com.homework.nio02.outbound.httpclient4;

import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import com.homework.nio02.filter.HeaderHttpResponseFilter;
import com.homework.nio02.filter.HttpRequestFilter;
import com.homework.nio02.filter.HttpResponseFilter;
import com.homework.nio02.router.HttpEndpointRouter;
import com.homework.nio02.router.RandomHttpEndpointRouter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class HttpOutboundHandler {

  private CloseableHttpAsyncClient httpclient;
  private ExecutorService proxyService;
  private List<String> backendUrls;

  HttpResponseFilter filter = new HeaderHttpResponseFilter();
  HttpEndpointRouter router = new RandomHttpEndpointRouter();

  public HttpOutboundHandler(List<String> backends) {
    this.backendUrls = backends.stream().map(this::formatUrl).collect(Collectors.toList());
    int cores = Runtime.getRuntime().availableProcessors();
    long keepAliveTime = 1000;
    int queueSize = 2048;
    RejectedExecutionHandler handler = new CallerRunsPolicy();
    proxyService = new ThreadPoolExecutor(cores, cores, keepAliveTime,
        TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(queueSize),
        new NamedThreadFactory("proxyService"), handler);

    IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
        .setConnectTimeout(1000)
        .setSoTimeout(1000)
        .setIoThreadCount(cores)
        .setRcvBufSize(32 * 1024)
        .build();

    httpclient = HttpAsyncClients.custom()
        .setMaxConnTotal(40)
        .setMaxConnPerRoute(8)
        .setDefaultIOReactorConfig(ioReactorConfig)
        .setKeepAliveStrategy((response, context) -> 6000)
        .build();
    httpclient.start();
  }

  private String formatUrl(String backend) {
    return backend.endsWith("/") ? backend.substring(0, backend.length() - 1) : backend;
  }

  public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, HttpRequestFilter filter) {
    String backendUrl = router.route(this.backendUrls);
    final String url = backendUrl + fullRequest.uri();
    filter.filter(fullRequest, ctx);
    proxyService.submit(()->fetchGet(fullRequest, ctx, url));
  }

  private void fetchGet(final FullHttpRequest inbound, final ChannelHandlerContext ctx, final String url) {
    final HttpGet httpGet = new HttpGet(url);
    httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
    httpGet.setHeader("concurrent", inbound.headers().get("concurrent"));

    httpclient.execute(httpGet, new FutureCallback<>() {
      @Override
      public void completed(final HttpResponse endpointResponse) {
        try {
          handleResponse(inbound, ctx, endpointResponse);
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
        }
      }

      @Override
      public void failed(final Exception ex) {
        httpGet.abort();
        ex.printStackTrace();
      }

      @Override
      public void cancelled() {
        httpGet.abort();
      }
    });
  }

  private void handleResponse(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx, final HttpResponse endpointResponse) {
    FullHttpResponse response = null;
    try {
      byte[] body = EntityUtils.toByteArray(endpointResponse.getEntity());
      response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(body));

      response.headers().set("Content-Type", "application/json");
      response.headers().setInt("Content-Length", Integer.parseInt(endpointResponse.getFirstHeader("Content-Length").getValue()));

      filter.filter(response);
    } catch (Exception e) {
      e.printStackTrace();
      response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
      exceptionCaught(ctx, e);
    } finally {
      if (fullRequest != null) {
        if (!HttpUtil.isKeepAlive(fullRequest)) {
          ctx.write(response).addListener(ChannelFutureListener.CLOSE);
        } else {
          ctx.write(response);
        }
      }
      ctx.flush();
    }
  }

  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    ctx.close();
  }
}