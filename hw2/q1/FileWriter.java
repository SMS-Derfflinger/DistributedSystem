package q1;

import java.io.RandomAccessFile;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class FileWriter {
    private static final int NUM_INTEGERS = 1024 * 1024 * 128;
    private static final int MAX_RANDOM_NUMBER = 1024 * 128;
    private static final int SEGMENT_LENGTH = 1024 * 1024;
    private static final int RANDOM_SEED = 237;
    private static final int FIND_NUMBER = 1024 * 64;
    private static final int HEADER_SIZE = 24;
    private static final String FILE_NAME = "./hw2/q1/2252441-hw2-q1.dat";

    public static void main(String[] args) {
        try {
            //createFile();
            findInts(FIND_NUMBER, HEADER_SIZE, FILE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createFile() {
        long start = System.nanoTime();
        try {
            initFile(FILE_NAME);
            int[] numbers = generateRandomInts(NUM_INTEGERS, RANDOM_SEED, MAX_RANDOM_NUMBER);
            appendRandomInts(numbers, FILE_NAME);
            mergeSort(numbers, 0, numbers.length - 1);
            System.out.println("sort over.");
            appendRandomInts(numbers, FILE_NAME);
            int bytesSize = writeHuffmanCode(numbers, SEGMENT_LENGTH, FILE_NAME);
            updateFile(numbers, bytesSize, FILE_NAME);
            long end = System.nanoTime();
            long duration = (end - start);
            System.out.println("生成所需时间: " + duration / 1000000 + "(毫秒)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 归并排序*/
    private static void mergeSort(int[] numbers, int left, int right) {
        if (left >= right) {
            return;
        }
        int mid = (left + right) / 2;
        mergeSort(numbers, left, mid);
        mergeSort(numbers, mid + 1, right);
        merge(numbers, left, mid, right);
    }

    /** 合并两个排好序的子数组*/
    private static void merge(int[] numbers, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        int[] leftNumbers = new int[n1];
        int[] rightNumbers = new int[n2];
        System.arraycopy(numbers, left, leftNumbers, 0, n1);
        System.arraycopy(numbers, mid + 1, rightNumbers, 0, n2);

        int i = 0, j = 0;
        int k = left;
        while (i < n1 && j < n2) {
            if (leftNumbers[i] <= rightNumbers[j]) {
                numbers[k] = leftNumbers[i];
                i++;
            } else {
                numbers[k] = rightNumbers[j];
                j++;
            }
            k++;
        }

        while (i < n1) {
            numbers[k] = leftNumbers[i];
            i++;
            k++;
        }
        while (j < n2) {
            numbers[k] = rightNumbers[j];
            j++;
            k++;
        }
    }

    // 生成随机数数组
    private static int[] generateRandomInts(int size, int seed, int maxNumber) {
        int[] numbers = new int[size];
        Random random = new Random(seed);
        for (int i = 0; i < size; i++) {
            numbers[i] = random.nextInt(maxNumber) + 1;
        }
        System.out.println("generate over.");
        return numbers;
    }

    // 传入排好序的数组，构建霍夫曼树，写入霍夫曼编码到文件中，返回编码的长度
    private static int writeHuffmanCode(int[] numbers, int segmentLength, String filePath) throws IOException {
        HuffmanCode huffmanCode = new HuffmanCode(numbers);
        Map<Integer, String> huffmanCodes = huffmanCode.getHuffmanCodes();
        StringBuilder encodedString;
        byte[] bytes = {};
        for (int i = 0; i < numbers.length / segmentLength * segmentLength; i += segmentLength) {
            encodedString = new StringBuilder();
            for (int j = 0; j < segmentLength; j++) {
                encodedString.append(huffmanCodes.get(numbers[j + i * segmentLength]));
            }
            String data = encodedString.toString();
            byte[] tempBytes = stringToByte(data);
            bytes = mergeByteArrays(bytes, tempBytes);
        }

        encodedString = new StringBuilder();
        for (int i = numbers.length / segmentLength * segmentLength; i < numbers.length; i++) {
            encodedString.append(huffmanCodes.get(numbers[i]));
        }
        String data = encodedString.toString();
        byte[] tempBytes = stringToByte(data);
        bytes = mergeByteArrays(bytes, tempBytes);
        appendRandomBytes(bytes, filePath);
        System.out.println("append over.");

        return bytes.length;
    }

    private static byte[] mergeByteArrays(byte[] array1, byte[] array2) {
        byte[] mergedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, mergedArray, 0, array1.length);
        System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
        return mergedArray;
    }

    // 将字符串转为byte数组
    private static byte[] stringToByte(String encodedString) {
        int byteLength = (encodedString.length() + 7) / 8;
        byte[] byteArray = new byte[byteLength];

        for (int i = 0; i < encodedString.length(); i++) {
            if (encodedString.charAt(i) == '1') {
                byteArray[i / 8] |= (1 << (7 - (i % 8)));
            }
        }
        return byteArray;
    }

    // 将int数组转为字节数组
    private static byte[] intsToBytes(int[] numbers) {
        byte[] bytes = new byte[numbers.length * 4];
        for (int i = 0; i < numbers.length; i++) {
            byte[] fourBytes = intToByteArray(numbers[i]);
            for (int j = 0; j < 4; j++) {
                bytes[i * 4 + j] = fourBytes[j];
            }
        }
        return bytes;
    }

    // 将int转为byte[4]数组
    private static byte[] intToByteArray(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    // 将byte数组追加到文件
    private static void appendRandomBytes(byte[] byteArray, String filePath) throws IOException {
        try {
            FileOutputStream fos = new FileOutputStream(filePath, true);
            fos.write(byteArray);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 将数组追加到文件
    private static void appendRandomInts(int[] numbers, String filePath) throws IOException {
        byte[] bytes = intsToBytes(numbers);
        appendRandomBytes(bytes, filePath);
        System.out.println("append over.");
    }

    // 追加1个int到文件
    private static void appendInt(int data, String filePath) throws IOException {
        try {
            FileOutputStream fos = new FileOutputStream(filePath, true);
            byte[] byteArray = intToByteArray(data);
            fos.write(byteArray);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 初始化文件头信息 */
    private static void initFile(String filePath) throws IOException {
        try {
            RandomAccessFile file = new RandomAccessFile(filePath, "rw");
            for (int i = 0; i < 6; i++) {
                byte[] byteArray = intToByteArray(0);
                file.write(byteArray);
            }
            file.close();
            System.out.println("init over.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 更新文件头信息*/
    private static void updateFile(int[] numbers, int bytesSize, String filePath) throws IOException {
        int aPosition = 6 * 4;
        int aLength = numbers.length * 4;
        int bPosition = aPosition + aLength;
        int bLength = numbers.length * 4;
        int cPosition = bPosition + bLength;
        int cLength = bytesSize;
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            raf.seek(0);
            raf.write(intToByteArray(aPosition));

            raf.seek(4);
            raf.write(intToByteArray(aLength));

            raf.seek(8);
            raf.write(intToByteArray(bPosition));

            raf.seek(12);
            raf.write(intToByteArray(bLength));

            raf.seek(16);
            raf.write(intToByteArray(cPosition));

            raf.seek(20);
            raf.write(intToByteArray(cLength));

            System.out.println("a部分起始位置: " + aPosition);
            System.out.println("a部分长度: " + aLength);
            System.out.println("b部分起始位置: " + bPosition);
            System.out.println("b部分长度: " + bLength);
            System.out.println("c部分起始位置: " + cPosition);
            System.out.println("c部分长度: " + cLength);
            System.out.println("over.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 将字节数组转换为int数组*/
    public static int[] byteArrayToIntArray(byte[] byteArray) {
        int length = byteArray.length / 4;
        int[] intArray = new int[length];

        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        for (int i = 0; i < length; i++) {
            intArray[i] = buffer.getInt();
        }

        return intArray;
    }

    /** 在文件中查找目标整数*/ 
    private static void findInts(int findNumber, int headerSize, String filePath) throws IOException {
        try {
            FileInputStream fis = new FileInputStream(filePath);
            byte[] header = new byte[headerSize];
            fis.read(header);
            int[] headerInfo = byteArrayToIntArray(header);
            findIntsPartA(fis, findNumber, headerInfo, filePath);
            findIntsPartB(findNumber, headerInfo, filePath);
        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    /** 使用传入的FileInputStream继续读取数组 */
    private static void findIntsPartA(FileInputStream fis, int findNumber, int[] headerInfo, String filePath) throws IOException {
        long start = System.nanoTime();
        try {
            byte[] partRandom = new byte[headerInfo[1]];
            fis.read(partRandom);
            int[] partAInts = byteArrayToIntArray(partRandom);
            List<Integer> list = findIntsRandom(partAInts, findNumber, headerInfo.length);

            long end = System.nanoTime();
            long duration = (end - start);
            System.out.println("A部分查找所需时间: " + duration / 1000000 + "(毫秒)");

            System.out.println("A部分整数值: " + list.size());
            System.out.println(list);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    /** 在int随机数组中查找不小于且最接近目标整数的整数，返回它在byte随机数组中的位置 */
    private static List<Integer> findIntsRandom(int[] numbers, int findNumber, int headerSize) {
        List<Integer> list = new ArrayList<>();
        int minNum = 2147483647;
        for (int i = 0; i < numbers.length; i++) {
            if (numbers[i] < findNumber || numbers[i] > minNum) {
                continue;
            }
            if (numbers[i] < minNum) {
                list = new ArrayList<>();
                minNum = numbers[i];
            }
            list.add(i * 4 + headerSize);
        }
        return list;
    }

    private static void findIntsPartB(int findNumber, int[] headerInfo, String filePath) throws IOException {
        try {
            long startTime = System.nanoTime();
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            int start = headerInfo[2];
            int length = headerInfo[3] / 4;
            int end = headerInfo[4];

            int firstPos = binarySearch(raf, start, length, findNumber);
            List<Integer> list = new ArrayList<>();
            while (firstPos < end) {
                raf.seek(firstPos);
                int value = raf.readInt();
                if (value == findNumber) {
                    list.add(firstPos);
                    firstPos += 4;
                } else {
                    break;
                }
            }
            long endTime = System.nanoTime();
            long duration = (endTime - startTime);
            System.out.println("B部分查找所需时间: " + duration / 1000000 + "(毫秒)");

            System.out.println("B部分整数值: " + list.size());
            System.out.println(list);
        } catch (IOException e) {
            e.printStackTrace();
        };
    }

    private static int binarySearch(RandomAccessFile raf, int start, int length, int findNumber) throws IOException {
        try {
            int low = 0;
            int high = length - 1;
            int firstPos = -1;

            while (low <= high) {
                int mid = (low + high) / 2;
                int midPos = start + mid * 4;
                raf.seek(midPos);
                int midValue = raf.readInt();
                if (midValue < findNumber) {
                    low = mid + 1;
                } else if (midValue > findNumber) {
                    high = mid - 1;
                } else {
                    firstPos = midPos;
                    high = mid - 1;
                }
            }
            return firstPos;
        } catch (IOException e) {
            e.printStackTrace();
        };
        return -1;
    }
}

class HuffmanNode implements Comparable<HuffmanNode> {
    int value;
    int frequency;
    HuffmanNode left;
    HuffmanNode right;

    public HuffmanNode(int value, int frequency) {
        this.value = value;
        this.frequency = frequency;
    }

    public HuffmanNode(int value, int frequency, HuffmanNode left, HuffmanNode right) {
        this.value = value;
        this.frequency = frequency;
        this.left = left;
        this.right = right;
    }

    // 按频率排序
    @Override
    public int compareTo(HuffmanNode other) {
        return Integer.compare(this.frequency, other.frequency);
    }
}

class HuffmanCode {
    private Map<Integer, String> huffmanCodes;
    private HuffmanNode root;

    public HuffmanCode() {
        huffmanCodes = new HashMap<>();
        root = null;
    }

    public HuffmanCode(int[] numbers) {
        this();
        this.buildHuffmanTree(numbers);
    }
    
    public Map<Integer, String> getHuffmanCodes() {
        return this.huffmanCodes;
    }

    public void buildHuffmanTree(int[] numbers) {
        Map<Integer, Integer> frequencyMap = new HashMap<>();

        for (int num : numbers) {
            frequencyMap.put(num, frequencyMap.getOrDefault(num, 0) + 1);
        }
        PriorityQueue<HuffmanNode> priorityQueue = new PriorityQueue<>();

        // 创建霍夫曼节点并加入优先队列
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            priorityQueue.offer(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        // 构建霍夫曼树
        while (priorityQueue.size() > 1) {
            HuffmanNode left = priorityQueue.poll();
            HuffmanNode right = priorityQueue.poll();
            HuffmanNode parent = new HuffmanNode(-1, left.frequency + right.frequency, left, right);
            priorityQueue.offer(parent);
        }

        root = priorityQueue.poll();
        generateCodes(root, "");
    }

    // 递归生成霍夫曼编码
    private void generateCodes(HuffmanNode node, String code) {
        if (node.left == null && node.right == null) {
            huffmanCodes.put(node.value, code);
            return;
        }
        generateCodes(node.left, code + "0");
        generateCodes(node.right, code + "1");
    }
}
