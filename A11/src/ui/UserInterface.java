package ui;

import processor.EncoderDecoder;
import processor.ImageProcessor;
import processor.Processor;
import processor.TextProcessor;

import java.util.Scanner;

public class UserInterface {
    private final Scanner scanner = new Scanner(System.in);
    private final EncoderDecoder encoderDecoder;

    private double pe = 0.0001; // Probability of error
    private int q = 2; // Number of symbols in the alphabet

    private int[][] G;
    private int n;
    private int k;

    public UserInterface() {
        encoderDecoder = new EncoderDecoder();
    }

    public void start() {
        mainMenu();
        inputMenu();
    }

    private void inputMenu() {
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
                case 4 -> mainMenu();
                case 6 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void mainMenu() {
        while (true) {
            System.out.printf("Probability of error: %.5f%nNumber of symbols in the alphabet: %d%n%n", pe, q);
            System.out.printf("""
                    Choose an option:
                    1. Enter generating matrix
                    2. Generate generating matrix
                    3. Change probability of error
                    4. Change number of symbols in the alphabet
                    5. Debug mode (currently %s)
                    Choice:\s""", encoderDecoder.isDebug() ? "ON" : "OFF");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> enterMatrix();
                case 2 -> generateMatrix();
                case 3 -> {
                    System.out.print("Enter the new probability of error: ");
                    pe = scanner.nextDouble();
                    mainMenu();
                }
                case 4 -> {
                    System.out.print("Enter the new number of symbols in the alphabet: ");
                    q = scanner.nextInt();
                    mainMenu();
                }
                case 5 -> {
                    encoderDecoder.setDebug(!encoderDecoder.isDebug());
                    mainMenu();
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
        inputVector();
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
        inputVector();
    }

    private void inputVector() {
        System.out.println("Enter the vector to encode:");
        String vectorInput = scanner.nextLine();
        String[] numbers = vectorInput.trim().split("\\s+");
        int[] m = new int[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            m[i] = Integer.parseInt(numbers[i]);
        }

        Processor processor = new Processor(encoderDecoder, G, k, pe, q);
        processor.processBlock(m, k);
    }

    private void inputText() {
        System.out.println("Enter the text to encode:");
        String text = scanner.nextLine();

        TextProcessor textProcessor = new TextProcessor(encoderDecoder, G, k, pe, q);
        textProcessor.processText(text);
    }

    private void inputImage() {
        System.out.println("Enter the path to the image file:");
        String inputPath = scanner.nextLine();
        System.out.println("Enter the output path for the decoded image:");
        String outputPath = scanner.nextLine();

        ImageProcessor imageProcessor = new ImageProcessor(encoderDecoder, G, k, pe, q);
        imageProcessor.processImage(inputPath, outputPath);
    }
}
