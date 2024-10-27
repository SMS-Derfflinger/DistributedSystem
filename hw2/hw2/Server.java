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
                System.out.println("New client connected.");
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

            // 根据Client端请求进行操作
            String command = in.readUTF();
            if ("READ".equalsIgnoreCase(command)) {
                sendFileToClient(out);
            } else if ("WRITE".equalsIgnoreCase(command)) {
                sendAndReceive(out, in);
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

    private static void sendAndReceive(DataOutputStream out, DataInputStream in) throws IOException {
        lock.writeLock().lock();
        try {
            byte[] sendData = Files.readAllBytes(Paths.get(FILE_PATH));
            out.writeInt(sendData.length);
            out.write(sendData);
            out.flush();
            System.out.println("File sent to client.");

            System.out.println("Waiting for client response...");
            int fileSize = -1;
            while (fileSize == -1) {
                try {
                    if (in.available() > 0) {
                        fileSize = in.readInt();
                        System.out.println("Client response size: " + fileSize);
                    }
                    Thread.sleep(1000);
                } catch (EOFException e) {
                    System.out.println("Client disconnected.");
                    break;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Thread interrupted.");
                    break;
                }
            }

            byte[] receivedData = new byte[fileSize];
            while (true) {
                try {
                    if (in.available() > 0) {
                        in.readFully(receivedData);
                        break;
                    }
                    Thread.sleep(1000);
                } catch (EOFException e) {
                    System.out.println("Client disconnected.");
                    break;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Thread interrupted.");
                    break;
                }
            }

            FileOutputStream fos = new FileOutputStream(FILE_PATH);
            fos.write(receivedData);
            fos.close();
            System.out.println("File received and updated.");
            
        } finally {
            lock.writeLock().unlock();
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
