package hw2;

import java.io.*;
import java.net.*;

public class Client3 {
    private static final String LOCAL_FILE_PATH = "./hw2/received-3.dat";
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             FileOutputStream fileOut = new FileOutputStream(LOCAL_FILE_PATH)) {

            // 接收文件大小
            long fileSize = in.readLong();
            System.out.println("Receiving file of size: " + fileSize + " bytes");

            // 接收文件数据
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalRead = 0;
            while (totalRead < fileSize && (bytesRead = in.read(buffer)) != -1) {
                fileOut.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
            }
            System.out.println("File received and saved to " + LOCAL_FILE_PATH);

            // 处理本地文件，例如查找和删除操作
            //processFile(LOCAL_FILE_PATH, 42); // 假设查找并删除值42

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
