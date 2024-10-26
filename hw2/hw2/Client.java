package hw2;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        String serverAddress = "localhost"; // 服务器地址
        int port = 12345; // 服务器端口
        String filePath = "file_to_send.txt"; // 要发送的文件路径

        try (Socket socket = new Socket(serverAddress, port);
             OutputStream outputStream = socket.getOutputStream();
             FileInputStream fileInputStream = new FileInputStream(filePath)) {

            System.out.println("连接到服务器，开始发送文件...");
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            System.out.println("文件发送完毕。");
        } catch (IOException e) {
            System.err.println("连接服务器或发送文件时发生错误: " + e.getMessage());
        }
    }
}
