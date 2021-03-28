package com.nerotomato.nio;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 单线程的socket程序
 * Created by nero on 2021/3/27.
 */
public class HttpServer3 {
    public static void main(String[] args) {
        try {
            System.out.println("availableProcessors:" + Runtime.getRuntime().availableProcessors());
            ExecutorService executorService = Executors
                    .newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 2);

            ServerSocket serverSocket = new ServerSocket(8803);
            while (true) {
                System.out.println("等待连接......");
                Socket socket = serverSocket.accept();
                System.out.println("连接成功...");
                executorService.execute(() ->
                        service(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //返回给客户端信息
    private static void service(Socket socket) {
        try {
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            pw.println("HTTP/1.1 200 OK");
            pw.println("Content-Type:text/html;charaset=utf-8");
            String body = "Hello,nio";
            pw.println("Content-Length:" + body.getBytes().length);
            pw.println();
            pw.write(body);
            pw.flush();
            pw.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
