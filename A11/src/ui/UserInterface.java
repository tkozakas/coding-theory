package ui;

import processor.EncoderDecoder;
import processor.ImageProcessor;
import processor.TextProcessor;

import java.util.Scanner;

public class UserInterface {
    private final EncoderDecoder encoderDecoder;
    private final Scanner scanner = new Scanner(System.in);
    private static final int ERROR_PROBABILITY = 5; // % error probability
    private int[][] G;
    private int n;
    private int k;

    public UserInterface() {
        this.encoderDecoder = new EncoderDecoder();
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
        System.out.println("Generated generating matrix G:");
        printMatrix(G);
    }

    private void enterMatrix() {
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
            System.out.println("Entered generating matrix G:");
            printMatrix(G);
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

        int[] c = encoderDecoder.encode(m, G);
        System.out.println("Encoded vector:");
        printVector(c);
    }

    private void inputText() {
        System.out.println("Enter the text to encode:");
        String text = scanner.nextLine();

        TextProcessor textProcessor = new TextProcessor(encoderDecoder, G, k, ERROR_PROBABILITY);
        textProcessor.processText(text);
    }

    private void inputImage() {
        System.out.println("Enter the path to the image file:");
        String inputPath = scanner.nextLine();
        System.out.println("Enter the output path for the decoded image:");
        String outputPath = scanner.nextLine();

        ImageProcessor imageProcessor = new ImageProcessor(encoderDecoder, G, k, ERROR_PROBABILITY);
        imageProcessor.processImage(inputPath, outputPath);
    }

    private void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }
    }

    private void printVector(int[] vector) {
        for (int val : vector) {
            System.out.print(val + " ");
        }
        System.out.println();
    }
}
