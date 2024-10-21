package q1;

import java.io.RandomAccessFile;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.PriorityQueue; 

public class FileWriter {
    private static final int NUM_INTEGERS = 1024 * 128;
    private static final int MAX_RANDOM_NUMBER = 1024 * 128;
    private static final int RANDOM_SEED = 237;
    private static final String FILE_NAME = "./hw2/q1/2252441-hw2-q1.dat";

    public static void main(String[] args) {
        try {
            int[] numbers = generateRandomInts(NUM_INTEGERS, RANDOM_SEED);
            writeRandomInts(numbers, FILE_NAME);
            mergeSort(numbers, 0, numbers.length - 1);
            appendRandomInts(numbers, FILE_NAME);
            String encodedString = getHuffmanString(numbers);
            byte[] encodedBytes = stringToByte(encodedString);
            appendRandomBytes(encodedBytes, FILE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 归并排序
    private static void mergeSort(int[] numbers, int left, int right) {
        if (left >= right) {
            return;
        }
        int mid = (left + right) / 2;
        mergeSort(numbers, left, mid);
        mergeSort(numbers, mid + 1, right);
        merge(numbers, left, mid, right);
    }

    // 合并两个排好序的子数组
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
    private static int[] generateRandomInts(int size, int seed) {
        int[] numbers = new int[size];
        Random random = new Random(seed);
        for (int i = 0; i < size; i++) {
            numbers[i] = random.nextInt(MAX_RANDOM_NUMBER) + 1;
        }
        return numbers;
    }

    // 将霍夫曼编码转换为二进制字符串
    private static String getHuffmanString(int[] numbers) {
        HuffmanCode huffmanCode = new HuffmanCode(numbers);
        Map<Integer, String> huffmanCodes = huffmanCode.getHuffmanCodes();
        StringBuilder encodedString = new StringBuilder();
        for (int num : numbers) {
            encodedString.append(huffmanCodes.get(num));
        }
        return encodedString.toString();
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

    // 将byte数组追加到文件
    private static void appendRandomBytes(byte[] byteArray, String fileName) throws IOException {
        try {
            FileOutputStream fos = new FileOutputStream(fileName, true);
            fos.write(byteArray);
            fos.close();
            System.out.println("append over.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 将数组写入文件
    private static void writeRandomInts(int[] numbers, String fileName) throws IOException {
        try {
            RandomAccessFile file = new RandomAccessFile(fileName, "rw");
            for (int number : numbers) {
                byte[] byteArray = intToByteArray(number);
                file.write(byteArray);
            }
            file.close();
            System.out.println("write over.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 将数组追加到文件
    private static void appendRandomInts(int[] numbers, String fileName) throws IOException {
        try {
            FileOutputStream fos = new FileOutputStream(fileName, true);
            for (int number : numbers) {
                byte[] byteArray = intToByteArray(number);
                fos.write(byteArray);
            }
            fos.close();
            System.out.println("append over.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 将int转为byte[4]数组
    private static byte[] intToByteArray(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
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
