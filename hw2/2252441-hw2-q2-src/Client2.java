package hw2;

import java.io.*;
import java.net.*;
import java.nio.file.*;

import static hw2.Server.*;
import static hw2.FileWriter.*;

public class Client2 {
    private static final String LOCAL_FILE_PATH = "received-2.dat";

    public static void main(String[] args) throws ClassNotFoundException {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            // 请求服务器发送文件
            out.writeUTF("WRITE");
            receiveFileFromServer(in);
            findInts(FIND_NUMBER, HEADER_SIZE, LOCAL_FILE_PATH);
            processFile(LOCAL_FILE_PATH, HEADER_SIZE, FIND_NUMBER);

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

    private static void processFile(String filePath, int headerSize, int findNumber) throws IOException {
        long start = System.nanoTime();
        try {
            int[] headerInfo;
            byte[] partABytes;
            byte[] partBBytes;
            byte[] partCBytes;
            
            try (FileInputStream fis = new FileInputStream(filePath)) {
                byte[] header = new byte[headerSize];
                fis.read(header);
                headerInfo = byteArrayToIntArray(header);
                partABytes = new byte[headerInfo[1]];
                partBBytes = new byte[headerInfo[3]];
                partCBytes = new byte[headerInfo[5]];
                fis.read(partABytes);
                fis.read(partBBytes);
                fis.read(partCBytes);
            }

            int[] partBInts = byteArrayToIntArray(partBBytes);
            int firstPos = binarySearch(partBInts, 0, findNumber);
            int foundNumber = partBInts[firstPos];
            int num = 0;
            int pos = firstPos;
            while (pos < partBInts.length) {
                if (partBInts[pos] == foundNumber) {
                    num++;
                    pos++;
                } else {
                    break;
                }
            }

            int[] newNumbers = new int[partBInts.length - num];
            int index = 0;
            for (int number : partBInts) {
                if (number != foundNumber) {
                    newNumbers[index++] = number;
                }
            }

            headerInfo[3] = headerInfo[3] - num * 4;
            headerInfo[4] = headerInfo[4] - num * 4;
            byte[] headerBytes = intsToBytes(headerInfo);

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(headerBytes);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath, true)) {
                byte[] newBytes = intsToBytes(newNumbers);
                fos.write(partABytes);
                fos.write(newBytes);
                fos.write(partCBytes);
            }

            long end = System.nanoTime();
            long duration = (end - start);
            System.out.println("B部分删除所需时间: " + duration / 1000000 + "(毫秒)");
        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    private static void findInts(int findNumber, int headerSize, String filePath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filePath);
        byte[] header = new byte[headerSize];
        fis.read(header);
        fis.close();
        int[] headerInfo = byteArrayToIntArray(header);
        findIntsPartB(findNumber, headerInfo, filePath);
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
