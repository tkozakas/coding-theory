package ui;

import processor.EncoderDecoder;
import processor.ImageProcessor;
import processor.Processor;
import processor.TextProcessor;

import java.util.Scanner;

public class UserInterface {
    private final EncoderDecoder encoderDecoder;
    private final Scanner scanner = new Scanner(System.in);

    private static final double PE = 0.0001; // Probability of error
    private static final int Q = 2; // Number of symbols in the alphabet

    private int[][] G;
    private int n;
    private int k;

    private static final boolean DEBUG = true;

    public UserInterface() {
        this.encoderDecoder = new EncoderDecoder(DEBUG);
    }

    public void start() {
        matrixMenu();
        mainMenu();
    }

    private void mainMenu() {
        while (true) {
            System.out.println("""
                    Choose an option:
                    1. Process Vector
                    2. Process Text
                    3. Process Image
                    4. Generate new matrix
                    5. Exit
                    Choice:\s""");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> inputVector();
                case 2 -> inputText();
                case 3 -> inputImage();
                case 4 -> matrixMenu();
                case 6 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void matrixMenu() {
        while (true) {
            System.out.print("""
                    Choose an option:
                    1. Enter generating matrix
                    2. Generate generating matrix
                    Choice:\s""");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> {
                    enterMatrix();
                    return;
                }
                case 2 -> {
                    generateMatrix();
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void generateMatrix() {
        System.out.print("Enter the number of columns (n): ");
        n = scanner.nextInt();
        System.out.print("Enter the number of rows (k): ");
        k = scanner.nextInt();
        scanner.nextLine();

        G = new int[k][n];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                G[i][j] = i == j ? 1 : 0;
            }
            for (int j = k; j < n; j++) {
                G[i][j] = (int) (Math.random() * 2);
            }
        }
    }

    private void enterMatrix() {
        try {
            System.out.print("Enter the number of columns (n): ");
            n = scanner.nextInt();
            System.out.print("Enter the number of rows (k): ");
            k = scanner.nextInt();
            scanner.nextLine();

            G = new int[k][n];
            if (k > n) {
                System.out.println("Error: The number of rows (k) should be less than or equal to the number of columns (n).");
                enterMatrix();
            } else {
                System.out.println("Enter the matrix row by row (only 0s and 1s):");
                for (int i = 0; i < k; i++) {
                    String rowInput = scanner.nextLine();
                    String[] numbers = rowInput.trim().split("\\s+");
                    if (numbers.length != n) {
                        System.out.println("Error: Incorrect number of columns. Please enter the row again.");
                        i--;
                        continue;
                    }
                    for (int j = 0; j < n; j++) {
                        G[i][j] = Integer.parseInt(numbers[j]);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error: Invalid input. Please try again.");
            enterMatrix();
        }
    }

    private void inputVector() {
        System.out.println("Enter the vector to encode:");
        String vectorInput = scanner.nextLine();
        String[] numbers = vectorInput.trim().split("\\s+");
        int[] m = new int[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            m[i] = Integer.parseInt(numbers[i]);
        }

        Processor processor = new Processor(encoderDecoder, G, PE, Q);
        processor.processBlock(m, k);
    }

    private void inputText() {
        System.out.println("Enter the text to encode:");
        String text = scanner.nextLine();

        TextProcessor textProcessor = new TextProcessor(encoderDecoder, G, k, PE, Q);
        textProcessor.processText(text);
    }

    private void inputImage() {
        System.out.println("Enter the path to the image file:");
        String inputPath = scanner.nextLine();
        System.out.println("Enter the output path for the decoded image:");
        String outputPath = scanner.nextLine();

        ImageProcessor imageProcessor = new ImageProcessor(encoderDecoder, G, k, PE, Q);
        imageProcessor.processImage(inputPath, outputPath);
    }
}
