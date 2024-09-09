import java.util.*;
import java.util.stream.IntStream;

public class EncoderDecoder {
    private final Random random = new Random();

    /*
        užkoduotas vektorius c = m (vektorius) * G (matrica)
     */
    public int[] encode(int[][] G, int[] m) {
        return multiplyVectorMatrix(G, m);
    }


    public int[] decode(int[][] H, int[] r, List<CosetLeaderInfo> cosetLeaders) {
        int[] syndrome = multiplyVectorMatrix(H, r);
        String syndromeKey = arrayToBinaryString(syndrome);

        CosetLeaderInfo cosetLeader = cosetLeaders.stream()
                .filter(info -> arrayToBinaryString(info.syndrome()).equals(syndromeKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Coset leader not found"));

        if (cosetLeader.weight() == 0) {
            return r;
        }

        int[] decoded = new int[r.length];
        for (int i = 0; i < r.length; i++) {
            decoded[i] = (r[i] + cosetLeader.cosetLeader()[i]) % 2;
        }
        return decoded;
    }

    public int[] error(int[] c, int probability) {
        int[] r = new int[c.length];
        System.arraycopy(c, 0, r, 0, c.length);
        for (int i = 0; i < r.length; i++) {
            if (random.nextInt(100) < probability) {
                r[i] = (r[i] == 0) ? 1 : 0;
            }
        }
        return r;
    }

    public int[][] generateParityCheckMatrix(int[][] G, int n, int k) {
        /*
        H = (A^T|I) - stulpeliai virsta eilutėmis ir atvirkščiai

        G = (1, 0, | 1, 0, 1)
            (0, 1, | 0, 1, 1)
            <-I-> | <-A->

        H = (1, 0, | 1, 0, 0)
            (0, 1, | 0, 1, 0)
            (1, 1, | 0, 0, 1)
            <A^T-> | <-I->
        */

        // extract the submatrix A from G
        int[][] A = new int[k][n - k]; // (k eilučių, n - k stulpelių)
        IntStream.range(0, k)
                .forEach(i -> System.arraycopy(G[i], k, A[i], 0, n - k));

        // transpose the submatrix A to get A^T
        int[][] At = new int[n - k][k]; // (n - k eilučių, k stulpelių)
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < n - k; j++) {
                At[j][i] = A[i][j];
            }
        }

        // construct the parity check matrix H = (A^T | I)
        int[][] H = new int[n - k][n]; // (n - k eilučių, k stulpelių)
        for (int i = 0; i < n - k; i++) {
            System.arraycopy(At[i], 0, H[i], 0, k);
            for (int j = k; j < n; j++) {
                H[i][j] = (i == j - k) ? 1 : 0;
            }
        }

        return H;
    }

    public List<CosetLeaderInfo> findAllCosetLeaders(int[][] H) {
        int rows = H.length;
        int cols = H[0].length;

        // Using a Map to store coset leaders based on syndrome as a key (string representation)
        Map<String, CosetLeaderInfo> cosetLeadersMap = new HashMap<>();

        // Iterate through all possible error vectors of length cols (n)
        for (int i = 0; i < (1 << cols); i++) { // 2^n possible vectors
            int[] errorVector = new int[cols];
            int currentWeight = 0;

            // Generate the error vector
            for (int j = 0; j < cols; j++) {
                if ((i & (1 << j)) != 0) {
                    errorVector[j] = 1;
                    currentWeight++;
                }
            }

            int[] syndromeArray = multiplyVectorMatrix(H, errorVector);
            String syndromeKey = arrayToBinaryString(syndromeArray);
            int weight = computeWeight(errorVector);

            if (!cosetLeadersMap.containsKey(syndromeKey) || currentWeight < cosetLeadersMap.get(syndromeKey).weight()) {
                cosetLeadersMap.put(syndromeKey, new CosetLeaderInfo(errorVector, syndromeArray, weight));
            }
        }

        return new ArrayList<>(cosetLeadersMap.values());
    }

    private int[] multiplyVectorMatrix(int[][] matrix, int[] vector) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int[] result = new int[rows];

        for (int i = 0; i < rows; i++) {
            int sum = 0;
            for (int j = 0; j < cols; j++) {
                sum += matrix[i][j] * vector[j];
            }
            result[i] = sum % 2;
        }
        return result;
    }

    private static String arrayToBinaryString(int[] array) {
        StringBuilder binaryString = new StringBuilder();
        for (int bit : array) {
            binaryString.append(bit);
        }
        return binaryString.toString();
    }

    private int computeWeight(int[] vector) {
        return (int) Arrays.stream(vector).filter(i -> i == 1).count();
    }
}
