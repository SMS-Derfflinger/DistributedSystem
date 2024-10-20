package q1;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Random;

public class FileWriter {
    private static final int NUM_INTEGERS = 1024 * 128;
    private static final int MAX_RANDOM_NUMBER = 1024 * 128;
    private static final String FILE_NAME = "./q1/2252441-hw2-q1.txt";

    public static void main(String[] args) {
        try {
            Random random = new Random(237);
            RandomAccessFile file = new RandomAccessFile(FILE_NAME, "rw");
            for (int i = 0; i < NUM_INTEGERS; i++) {
                int randomInt = random.nextInt(MAX_RANDOM_NUMBER) + 1;
                byte[] byteArray = intToByteArray(randomInt);
                file.write(byteArray);
            }
            file.close();
            System.out.println("over.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // convert an integer to a byte array
    private static byte[] intToByteArray(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }
}
