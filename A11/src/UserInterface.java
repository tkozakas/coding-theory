import java.util.Scanner;
import java.util.stream.IntStream;

public class UserInterface {
    private final EncoderDecoder encoderDecoder = new EncoderDecoder();

    public UserInterface() {
        prompt();
    }

    public void prompt() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Įveskite kodo ilgį (n): ");
        int n = scanner.nextInt();
        System.out.print("Įveskite kodo dimensiją (k): ");
        int k = scanner.nextInt();

        int[][] G = generateMatrix(n, k);
        System.out.println("Kodo matrica: \n" + encoderDecoder.matrixToString(G));
        int[][] H = encoderDecoder.generateParityMatrix(G);

        System.out.print("Įveskite klaidos tikimybę (0 <= pe <= 1): ");
        double pe = scanner.nextDouble();
        System.out.print("Įveskite pranešimą kaip vektorių (pvz., 101): ");
        String input = scanner.next();

        int[] u = IntStream.range(0, input.length()).map(i -> Character.getNumericValue(input.charAt(i))).toArray();

        int[] encodedMessage = encoderDecoder.encode(u, G);
        System.out.println("Užkoduotas vektorius: " + encoderDecoder.arrayToString(encodedMessage));

        int[] transmittedMessage = encoderDecoder.sendThroughChannel(encodedMessage, pe);
        System.out.println("Kanalu išsiųstas vektorius: " + encoderDecoder.arrayToString(transmittedMessage));

        int[] decodedMessage = encoderDecoder.decode(transmittedMessage, H);
        System.out.println("Dekoduotas vektorius: " + encoderDecoder.arrayToString(decodedMessage));
    }

    private int[][] generateMatrix(int n, int k) {
        int[][] G = new int[k][n];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < n; j++) {
                G[i][j] = (j == i) ? 1 : 0;
            }
        }
        return G;
    }
}
