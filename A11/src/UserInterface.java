import java.util.*;
import java.util.stream.Collectors;

public class UserInterface {
    private final EncoderDecoder encoderDecoder = new EncoderDecoder();
    private final Random random = new Random();
    private Scanner scanner = new Scanner(System.in);

    private int[][] G;
    private int n;
    private int k;

    private static final int PROBABILITY = 5; // % error probability

    public UserInterface() {
        prompt();
    }

    public void prompt() {
        matrixMenu();
        inputMenu();
    }

    private void inputVector() {
        scanner = new Scanner(System.in);
        System.out.println("Enter the vector to encode:");
        int[] m = scanner.nextLine().chars()
                .filter(Character::isDigit)
                .map(c -> c - '0')
                .toArray();

        if (m.length != k) {
            System.out.printf("Error: The length of the message (%d) does not match the number of rows in the generating matrix (%d).%n", m.length, k);
            System.out.printf("Please generate a new generating matrix with k = %d, or enter a message of length %d.%n", k, m.length);
            matrixMenu();
        }

        sendVector(m);
    }

    private void inputMenu() {
        scanner = new Scanner(System.in);
        System.out.println("""
                Choose an option:
                1. Enter binary form vector
                2. Enter text
                Choice:\s""");

        int choice = scanner.nextInt();
        switch (choice) {
            case 1 -> inputVector();
            case 2 -> inputText();
        }
    }

    private void inputText() {
        scanner = new Scanner(System.in);
        System.out.println("Enter the text to encode:");
        String text = scanner.nextLine();
        List<int[]> decodedResults = new ArrayList<>();

        for (char c : text.toCharArray()) {
            String binaryString = Integer.toBinaryString(c);
            binaryString = "0".repeat(8 - binaryString.length()) + binaryString;
            int[] bits = binaryString.chars().map(b -> b - '0').toArray();

            for (int i = 0; i < bits.length; i += k) {
                int[] m = new int[k];
                int bitsToCopy = Math.min(k, bits.length - i);
                System.arraycopy(bits, i, m, 0, bitsToCopy);
                // If bitsToCopy < k, the remaining bits in m are zeros by default

                int[] decodedMessage = sendVector(m);
                decodedResults.add(decodedMessage);
            }
        }

        System.out.println("\nCollected decoded messages:");
        for (int[] decoded : decodedResults) {
            System.out.println(Arrays.toString(decoded));
        }

        // Collect all decoded bits
        StringBuilder decodedBits = new StringBuilder();
        for (int[] decoded : decodedResults) {
            for (int bit : decoded) {
                decodedBits.append(bit);
            }
        }

        // Convert decoded bits back to text
        StringBuilder decodedText = new StringBuilder();
        String bitsString = decodedBits.toString();

        // Make sure the total bits are a multiple of 8 for full characters
        int totalBits = bitsString.length();
        int fullBytes = totalBits / 8;

        for (int i = 0; i < fullBytes * 8; i += 8) {
            String byteStr = bitsString.substring(i, i + 8);
            int charCode = Integer.parseInt(byteStr, 2);
            decodedText.append((char) charCode);
        }

        System.out.println("Decoded text: " + decodedText);
    }

    private void matrixMenu() {
        System.out.print("""
                Choose an option:
                1. Enter generating matrix
                2. Generate generating matrix
                Choice:\s"""
        );
        int choice = scanner.nextInt();

        switch (choice) {
            case 1 -> enterMatrix();
            case 2 -> generateMatrix();
        }
    }

    private int[] sendVector(int[] m) {
        int[] c = encoderDecoder.encode(G, m);
        int[] r = encoderDecoder.error(c, PROBABILITY);
        int[][] H = encoderDecoder.parityCheckMatrix(G);
        List<CosetLeader> cosetLeaders = encoderDecoder.findCosetLeaders(H);
        cosetLeaders.sort(Comparator.comparingInt(CosetLeader::weight));
        int[] decoded = encoderDecoder.decode(r, H, cosetLeaders);

        System.out.println("Generating matrix:\n" + matrixAsString(G));
        System.out.println("Parity check matrix:\n" + matrixAsString(H));
        System.out.println("Coset leaders:\nSyndrome | Coset leader | Weight\n" + cosetLeaders.stream()
                .map(cl -> Arrays.toString(cl.syndrome()) + " | " + vectorAsString(cl.cosetLeader()) + " | " + cl.weight())
                .collect(Collectors.joining("\n")));

        System.out.println();

        System.out.println("Vector to encode: " + vectorAsString(m));
        System.out.println("Encoded vector: " + vectorAsString(c));
        System.out.println("Received vector (with %d%% probability error): ".formatted(PROBABILITY) + vectorAsString(r));

        int[] decodedMessage = new int[k];
        System.arraycopy(decoded, 0, decodedMessage, 0, k);
        System.out.println("Decoded message: " + vectorAsString(decodedMessage));

        return decodedMessage;
    }

    private void generateMatrix() {
        System.out.print("Enter the number of columns (n): ");
        n = scanner.nextInt();
        System.out.print("Enter the number of rows (k): ");
        k = scanner.nextInt();

        G = new int[k][n];

        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                G[i][j] = i == j ? 1 : 0;
            }
            for (int j = k; j < n; j++) {
                G[i][j] = random.nextInt(2);
            }
        }
    }

    private void enterMatrix() {
        System.out.print("Enter the number of columns (n): ");
        n = scanner.nextInt();
        System.out.print("Enter the number of rows (k): ");
        k = scanner.nextInt();

        G = new int[k][n];

        if (k > n) {
            System.out.println("Error: The number of rows (k) should be less than or equal to the number of columns (n).");
            enterMatrix();
        }
        System.out.println("Enter the matrix:");
        scanner.nextLine();
        for (int i = 0; i < k; i++) {
            G[i] = scanner.nextLine().chars()
                    .filter(Character::isDigit)
                    .map(c -> c - '0')
                    .toArray();
        }
    }

    private String matrixAsString(int[][] matrix) {
        return "[" + Arrays.stream(matrix)
                .map(row -> Arrays.stream(row)
                        .mapToObj(String::valueOf)
                        .collect(Collectors.joining(", ")))
                .collect(Collectors.joining("\n")) + "]";
    }

    private String vectorAsString(int[] vector) {
        return "[" + Arrays.stream(vector)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(", ")) + "]";
    }
}
