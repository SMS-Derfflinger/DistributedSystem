package hw2;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.concurrent.locks.*;

public class Server {
    private static final String FILE_PATH = "./hw2/2252441-hw2-q1.dat";
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();
    public static final int SERVER_PORT = 1145;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is listening on port " + SERVER_PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(Socket socket) {
        try (socket;
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            // 根据客户端请求进行操作
            String command = in.readUTF();
            if ("READ".equalsIgnoreCase(command)) {
                // 读操作
                sendFileToClient(out);
            } else if ("WRITE".equalsIgnoreCase(command)) {
                // 写操作
                receiveFileFromClient(in);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFileToClient(DataOutputStream out) throws IOException {
        // 使用读锁
        lock.readLock().lock();
        try {
            byte[] fileData = Files.readAllBytes(Paths.get(FILE_PATH));
            out.writeInt(fileData.length);
            out.write(fileData);
            System.out.println("File sent to client.");
        } finally {
            lock.readLock().unlock();
        }
    }

    private static void receiveFileFromClient(DataInputStream in) throws IOException {
        // 使用写锁
        lock.writeLock().lock();
        try {
            int fileSize = in.readInt();
            byte[] fileData = new byte[fileSize];
            in.readFully(fileData);
            Files.write(Paths.get(FILE_PATH), fileData, StandardOpenOption.WRITE);
            System.out.println("File received and updated.");
        } finally {
            lock.writeLock().unlock();
        }
    }
}
