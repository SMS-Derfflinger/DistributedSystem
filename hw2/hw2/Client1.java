package hw2;

import java.io.*;
import java.net.*;
import java.nio.file.*;

import static hw2.Server.*;

public class Client1 {
    private static final String LOCAL_FILE_PATH = "./hw2/received-1.dat";
    private static final String SERVER_HOST = "localhost";

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            // 请求服务器发送文件
            out.writeUTF("READ");
            receiveFileFromServer(in);

            // 对本地文件进行处理，例如查找和删除
            //processFile(LOCAL_FILE_PATH, 42);

            // 将更新后的文件发送回服务器
            //out.writeUTF("WRITE");
            //sendFileToServer(out, LOCAL_FILE_PATH);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveFileFromServer(DataInputStream in) throws IOException {
        int fileSize = in.readInt();
        byte[] fileData = new byte[fileSize];
        in.readFully(fileData);
        Files.write(Paths.get(LOCAL_FILE_PATH), fileData);
        System.out.println("File received and saved to " + LOCAL_FILE_PATH);
    }

    private static void sendFileToServer(DataOutputStream out, String filePath) throws IOException {
        byte[] fileData = Files.readAllBytes(Paths.get(filePath));
        out.writeInt(fileData.length);
        out.write(fileData);
        System.out.println("File sent to server.");
    }
}
