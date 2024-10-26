package hw2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.Executors;

public class Server {
    private static final String FILE_PATH = "./hw2/2252441-hw2-q1.dat";
    private static final int THREAD_NUM = 3;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            System.out.println("Server is listening on port 12345");

            var executor = Executors.newFixedThreadPool(THREAD_NUM);

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
             RandomAccessFile file = new RandomAccessFile(FILE_PATH, "rw");
             FileChannel fileChannel = file.getChannel()) {

            // 对文件加锁，防止多个客户端同时写入
            try (FileLock lock = fileChannel.lock()) {
                System.out.println("File locked by server for client: " + socket.getInetAddress());

                // 发送文件大小
                long fileSize = file.length();
                out.writeLong(fileSize);

                // 发送文件数据
                byte[] buffer = new byte[4096];
                int bytesRead;
                file.seek(0);
                while ((bytesRead = file.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                System.out.println("File sent to client: " + socket.getInetAddress());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
