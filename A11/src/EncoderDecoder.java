import java.util.Random;

public class EncoderDecoder {
    private final Random random = new Random();

    public int[] encode(int[] message, int[][] G) {
        int n = G[0].length;
        int[] encoded = new int[n];
        for (int i = 0; i < message.length; i++) {
            for (int j = 0; j < n; j++) {
                encoded[j] = (encoded[j] + message[i] * G[i][j]) % 2;
            }
        }
        return encoded;
    }

    public int[] sendThroughChannel(int[] message, double pe) {
        int[] transmitted = new int[message.length];
        for (int i = 0; i < message.length; i++) {
            if (random.nextDouble() < pe) {
                transmitted[i] = 1 - message[i];
            } else {
                transmitted[i] = message[i];
            }
        }
        return transmitted;
    }

    public int[] decode(int[] received) {
        return received;
    }

    public String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        for (int value : array) {
            sb.append(value);
        }
        return sb.toString();
    }
}
