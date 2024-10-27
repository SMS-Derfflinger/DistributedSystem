package hw2;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import static hw2.Server.*;
import static hw2.FileWriter.*;

public class Client1 {
    private static final String LOCAL_FILE_PATH = "received-1.dat";

    public static void main(String[] args) throws ClassNotFoundException {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            // 请求服务器发送文件
            out.writeUTF("WRITE");
            receiveFileFromServer(in);
            processFile(out, LOCAL_FILE_PATH, HEADER_SIZE, FIND_NUMBER);

            sendFileToServer(out, LOCAL_FILE_PATH);
            System.out.println("Connection closed.");
            File file = new File(LOCAL_FILE_PATH);
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            out.writeUTF("READ");
            receiveFileFromServer(in);
            System.out.println("Connection closed.");
            findInts(FIND_NUMBER, HEADER_SIZE, LOCAL_FILE_PATH);
            File file = new File(LOCAL_FILE_PATH);
            file.delete();
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
            int[] headerInfo = byteArrayToIntArray(header);

            byte[] partRandom = new byte[headerInfo[1]];
            fis.read(partRandom);
            int[] partAInts = byteArrayToIntArray(partRandom);
            List<Integer> list = findIntsRandom(partAInts, findNumber);
            int num = partAInts[list.getFirst()];
            List<Integer> byteLocations = new ArrayList<>();
            for (Integer integer : list) {
                byteLocations.add(integer * 4 + headerInfo.length);
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

            headerInfo[1] = headerInfo[1] - byteLocations.size() * 4;
            headerInfo[2] = headerInfo[2] - byteLocations.size() * 4;
            headerInfo[4] = headerInfo[4] - byteLocations.size() * 4;
            byte[] headerBytes = intsToBytes(headerInfo);
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(headerBytes);
            fos.close();

            fos = new FileOutputStream(filePath, true);
            byte[] newBytes = intsToBytes(newNumbers);
            fos.write(newBytes);
            fos.write(partBBytes);
            fos.write(partCBytes);
            fos.close();

            end = System.nanoTime();
            duration = (end - start);
            System.out.println("A部分删除所需时间: " + duration / 1000000 + "(毫秒)");
        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    private static void findInts(int findNumber, int headerSize, String filePath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filePath);
        byte[] header = new byte[headerSize];
        fis.read(header);
        int[] headerInfo = byteArrayToIntArray(header);
        findIntsPartA(fis, findNumber, headerInfo, filePath);
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
        out.flush();
        System.out.println("File sent to server.");
    }
}
