import java.util.Arrays;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EncoderDecoder {
    private final Random random = new Random();

    public int[] encode(int[] u, int[][] G) {
        return IntStream.range(0, G[0].length)
                .map(j -> IntStream.range(0, G.length)
                        .map(i -> G[i][j] * u[i])
                        .sum() % 2)
                .toArray();
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

    public int[] decode(int[] received, int[][] H) {
        int[] syndrome = calculateSyndrome(received, H);
        while (isNonZero(syndrome)) {
            int errorPosition = findErrorPosition(syndrome, H);
            if (errorPosition != -1) {
                received[errorPosition] = 1 - received[errorPosition];
            }
            syndrome = calculateSyndrome(received, H);
        }
        return received;
    }

    private int[] calculateSyndrome(int[] received, int[][] H) {
        return Arrays.stream(H).mapToInt(ints -> IntStream.range(0, received.length)
                        .map(j -> received[j] * ints[j])
                        .sum() % 2)
                .toArray();
    }

    private boolean isNonZero(int[] syndrome) {
        return IntStream.of(syndrome).anyMatch(value -> value != 0);
    }

    private int findErrorPosition(int[] syndrome, int[][] H) {
        for (int j = 0; j < H[0].length; j++) {
            int[] column = getColumn(H, j);
            if (Arrays.equals(syndrome, column)) {
                return j;
            }
        }
        return -1;
    }

    private int[] getColumn(int[][] matrix, int column) {
        return Arrays.stream(matrix).mapToInt(ints -> ints[column])
                .toArray();
    }

    public String arrayToString(int[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public String matrixToString(int[][] g) {
        return Arrays.stream(g)
                .map(row -> arrayToString(row) + "\n")
                .collect(Collectors.joining());
    }

    public int[][] generateParityMatrix(int[][] g) {
        return new int[g[0].length - g.length][g[0].length];
    }
}
