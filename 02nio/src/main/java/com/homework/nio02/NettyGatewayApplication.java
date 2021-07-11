package com.homework.nio02;

import com.homework.nio02.inbound.HttpInboundServer;
import java.util.Arrays;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyGatewayApplication {

  public final static String GATEWAY_NAME = "NettyGateway";
  public final static String GATEWAY_VERSION = "1.0.0";

  public static void main(String[] args) {
    String proxyPort = System.getProperty("proxyPort", "8888");
    String proxyServers = System.getProperty(
        "proxyServers",
        "http://localhost:8801,http://localhost:8802,http://localhost:8803");
    int port = Integer.parseInt(proxyPort);
    String gatewayInfo = GATEWAY_NAME + " " + GATEWAY_VERSION;
    System.out.println(gatewayInfo + " starting...");
    HttpInboundServer server = new HttpInboundServer(port, Arrays.asList(proxyServers.split(",")));
    System.out.println(gatewayInfo
        + " started at http://localhost:" + port
        + " for server:" + server);
    try {
      server.run();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}
