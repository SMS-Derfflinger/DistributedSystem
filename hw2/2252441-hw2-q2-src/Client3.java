package hw2;

import java.io.*;
import java.net.*;
import java.nio.file.*;

import static hw2.Server.*;
import static hw2.FileWriter.*;

public class Client3 {
    private static final String LOCAL_FILE_PATH = "received-3.dat";

    public static void main(String[] args) throws ClassNotFoundException {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            // 请求服务器发送文件
            out.writeUTF("WRITE");
            receiveFileFromServer(in);
            int[] partCInts = findInts(FIND_NUMBER, HEADER_SIZE, LOCAL_FILE_PATH);
            processFile(LOCAL_FILE_PATH, HEADER_SIZE, FIND_NUMBER, partCInts);

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

    private static void processFile(String filePath, int headerSize, int findNumber, int[] partCInts) throws IOException {
        long start = System.nanoTime();
        try {
            int[] headerInfo;
            byte[] partABytes;
            byte[] partBBytes;
            byte[] header;

            try (FileInputStream fis = new FileInputStream(filePath)) {
                header = new byte[headerSize];
                fis.read(header);
                headerInfo = byteArrayToIntArray(header);
                partABytes = new byte[headerInfo[1]];
                partBBytes = new byte[headerInfo[3]];
                fis.read(partABytes);
                fis.read(partBBytes);
            }

            int firstPos = binarySearch(partCInts, 0, findNumber);
            int foundNumber = partCInts[firstPos];
            int num = 0;
            int pos = firstPos;
            while (pos < partCInts.length) {
                if (partCInts[pos] == foundNumber) {
                    num++;
                    pos++;
                } else {
                    break;
                }
            }

            int[] newNumbers = new int[partCInts.length - num];
            int index = 0;
            for (int number : partCInts) {
                if (number != foundNumber) {
                    newNumbers[index++] = number;
                }
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(header);
            }

            try (FileOutputStream fos = new FileOutputStream(filePath, true)) {
                fos.write(partABytes);
                fos.write(partBBytes);
            }
            int partCSize = writeHuffmanCode(newNumbers, SEGMENT_LENGTH, filePath, false);

            try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
                raf.seek(5 * 4);
                raf.write(intToByteArray(partCSize));
            }

            long end = System.nanoTime();
            long duration = (end - start);
            System.out.println("C部分删除所需时间: " + duration / 1000000 + "(毫秒)");
        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    private static int[] findInts(int findNumber, int headerSize, String filePath) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filePath);
        byte[] header = new byte[headerSize];
        fis.read(header);
        fis.close();
        int[] headerInfo = byteArrayToIntArray(header);
        return findIntsPartC(findNumber, headerInfo, filePath);
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
