package com.homework.nio01;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class HttpServer02 {

  public static void main(String[] args) throws IOException {
    ServerSocket serverSocket = new ServerSocket(8802);
    while (true) {
      try {
        final Socket socket = serverSocket.accept();
        new Thread(() -> {
          service(socket);
        }).start();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static void service(Socket socket) {
    try {
      log.info("http server 02 receives a connection");
      PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
      printWriter.println("HTTP/1.1 200 OK");
      printWriter.println("Content-Type:text/html;charset=utf-8");
      String body = "hello,nio2";
      printWriter.println("Content-length:" + body.getBytes().length);
      printWriter.println();
      printWriter.write(body);
      printWriter.close();
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
