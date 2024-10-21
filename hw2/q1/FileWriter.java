package q1;

import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

public class FileWriter {
    private static final int NUM_INTEGERS = 1024 * 128;
    private static final int MAX_RANDOM_NUMBER = 1024 * 128;
    private static final int RANDOM_SEED = 237;
    private static final String FILE_NAME = "./q1/2252441-hw2-q1.dat";

    public static void main(String[] args) {
        try {
            int[] numbers = generateRandomInts(NUM_INTEGERS, RANDOM_SEED);
            writeRandom(numbers, FILE_NAME);
            mergeSort(numbers, 0, numbers.length - 1);
            appendRandom(numbers, FILE_NAME);
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

    // 将数组写入文件
    private static void writeRandom(int[] numbers, String fileName) throws IOException {
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
    private static void appendRandom(int[] numbers, String fileName) throws IOException {
        try {
            RandomAccessFile file = new RandomAccessFile(fileName, "rw");
            file.seek(file.length());
            for (int number : numbers) {
                byte[] byteArray = intToByteArray(number);
                file.write(byteArray);
            }
            file.close();
            System.out.println("append over.");
        } catch (Exception e) {
            e.printStackTrace();
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

    // 将int转为byte[4]数组
    private static byte[] intToByteArray(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }
}
