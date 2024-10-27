package hw2;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static hw2.Server.*;

public class Client1 {
    private static final String LOCAL_FILE_PATH = "./hw2/received-1.dat";
    private static final String SERVER_HOST = "localhost";

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            // 请求服务器发送文件
            out.writeUTF("WRITE");
            receiveFileFromServer(in);
            processFile(out, LOCAL_FILE_PATH, FileWriter.HEADER_SIZE, FileWriter.FIND_NUMBER);

            // 将更新后的文件发送回服务器
            //out.writeUTF("WRITE");
            //sendFileToServer(out, LOCAL_FILE_PATH);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processFile(DataOutputStream out, String filePath, int headerSize, int findNumber) throws IOException {
        long start = System.nanoTime();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            byte[] header = new byte[headerSize];
            fis.read(header);
            int[] headerInfo = FileWriter.byteArrayToIntArray(header);

            byte[] partRandom = new byte[headerInfo[1]];
            fis.read(partRandom);
            int[] partAInts = FileWriter.byteArrayToIntArray(partRandom);
            List<Integer> list = FileWriter.findIntsRandom(partAInts, findNumber);
            int num = partAInts[list.get(0)];
            List<Integer> byteLocations = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                byteLocations.add(list.get(i) * 4 + headerInfo.length);
            }
            long end = System.nanoTime();
            long duration = (end - start);
            System.out.println("A部分查找所需时间: " + duration / 1000000 + "(毫秒)");
            System.out.println("A部分整数值: " + num);
            System.out.println("A部分整数值个数: " + byteLocations.size());
            System.out.println(byteLocations);

            start = System.nanoTime();
            int[] newNumbers = new int[partAInts.length - byteLocations.size()];
            int index = 0;
            for (int number : partAInts) {
                if (number != num) {
                    newNumbers[index++] = number;
                }
            }

            byte[] partBBytes = new byte[headerInfo[3]];
            byte[] partCBytes = new byte[headerInfo[5]];
            fis.read(partBBytes);
            fis.read(partCBytes);
            fis.close();

            headerInfo[1] = headerInfo[1] - num * 4;
            headerInfo[2] = headerInfo[2] - num * 4;
            headerInfo[4] = headerInfo[4] - num * 4;
            byte[] headerBytes = FileWriter.intsToBytes(headerInfo);
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(headerBytes);
            fos.close();

            fos = new FileOutputStream(filePath, true);
            byte[] newBytes = FileWriter.intsToBytes(newNumbers);
            fos.write(newBytes);
            fos.write(partBBytes);
            fos.write(partCBytes);
            fos.close();

            end = System.nanoTime();
            duration = (end - start);
            System.out.println("A部分删除所需时间: " + duration / 1000000 + "(毫秒)");

            byte[] fileData = Files.readAllBytes(Paths.get(filePath));
            out.writeInt(fileData.length);
            out.write(fileData);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        };
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
