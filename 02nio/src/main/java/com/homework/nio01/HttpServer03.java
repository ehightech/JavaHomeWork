package com.homework.nio01;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class HttpServer03 {

  public static void main(String[] args) throws IOException {
    ExecutorService executorService = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors());
    final ServerSocket serverSocket = new ServerSocket(8803);
    while (true) {
      try {
        final Socket socket = serverSocket.accept();
        executorService.execute(() -> service(socket));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static void service(Socket socket) {
    try {
      log.info("http server 03 receives a connection");
      PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
      printWriter.println("HTTP/1.1 200 OK");
      printWriter.println("Content-Type:text/html;charset=utf-8");
      String body = "hello,nio3";
      printWriter.println("Content-Length:" + body.getBytes().length);
      printWriter.println();
      printWriter.write(body);
      printWriter.close();
      socket.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
