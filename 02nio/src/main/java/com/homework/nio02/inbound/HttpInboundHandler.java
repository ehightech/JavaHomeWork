package com.homework.nio02.inbound;

import com.homework.nio02.filter.HeaderHttpRequestFilter;
import com.homework.nio02.filter.HttpRequestFilter;
import com.homework.nio02.outbound.httpclient4.HttpOutboundHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.ReferenceCountUtil;
import java.util.List;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class HttpInboundHandler extends ChannelInboundHandlerAdapter {

  private final List<String> proxyServer;
  private HttpOutboundHandler handler;
  private HttpRequestFilter filter = new HeaderHttpRequestFilter();

  public HttpInboundHandler(List<String> proxyServer) {
    this.proxyServer = proxyServer;
    this.handler = new HttpOutboundHandler(this.proxyServer);
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) {
    ctx.flush();
  }

  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    try {
      FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
      handler.handle(fullHttpRequest, ctx, filter);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      ReferenceCountUtil.release(msg);
    }
  }
}
