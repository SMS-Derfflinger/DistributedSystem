package hw2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;

public class Server {
    private static final String FILE_PATH = "./hw2/2252441-hw2-q1.dat";

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server is listening on port 12345");

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());
                // 为每个客户端启动一个新的线程进行处理
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (socket;
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             RandomAccessFile file = new RandomAccessFile(FILE_PATH, "r"); // 使用只读模式
             FileChannel fileChannel = file.getChannel()) {

            // 对文件加锁，只在写操作时需要锁，读操作可以并发
            synchronized (Server.class) {
                System.out.println("File being accessed by client: " + socket.getInetAddress());

                // 发送文件大小
                long fileSize = file.length();
                out.writeLong(fileSize);
                out.flush();

                // 发送文件数据
                byte[] buffer = new byte[4096];
                int bytesRead;
                file.seek(0);
                while ((bytesRead = file.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                }
                System.out.println("File sent to client: " + socket.getInetAddress());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
