package com.homework.nio02.inbound;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import java.util.List;

public class HttpInboundInitializer extends ChannelInitializer<SocketChannel> {

  private List<String> proxyServer;

  public HttpInboundInitializer(List<String> proxyServer) {
    this.proxyServer = proxyServer;
  }

  @Override
  protected void initChannel(SocketChannel ch) {
    ChannelPipeline cp = ch.pipeline();
    cp.addLast(new HttpServerCodec());
    cp.addLast(new HttpObjectAggregator(1024 * 1024));
    cp.addLast(new HttpInboundHandler(proxyServer));
  }
}
