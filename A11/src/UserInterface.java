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

        int[][] g = new int[k][n];
        System.out.println("Įveskite generuojančią matricą G (" + k + "x" + n + "):");
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < n; j++) {
                g[i][j] = scanner.nextInt();
            }
        }

        System.out.print("Įveskite klaidos tikimybę (0 <= pe <= 1): ");
        double pe = scanner.nextDouble();
        System.out.print("Įveskite pranešimą kaip vektorių (pvz., 101): ");
        String input = scanner.next();

        int[] message = IntStream.range(0, input.length()).map(i -> Character.getNumericValue(input.charAt(i))).toArray();

        int[] encodedMessage = encoderDecoder.encode(message, g);
        System.out.println("Užkoduotas vektorius: " + encoderDecoder.arrayToString(encodedMessage));

        int[] transmittedMessage = encoderDecoder.sendThroughChannel(encodedMessage, pe);
        System.out.println("Kanalu išsiųstas vektorius: " + encoderDecoder.arrayToString(transmittedMessage));

        int[] decodedMessage = encoderDecoder.decode(transmittedMessage);
        System.out.println("Dekoduotas vektorius: " + encoderDecoder.arrayToString(decodedMessage));
    }
}
