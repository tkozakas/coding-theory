import java.util.Random;
import java.util.stream.IntStream;

public class EncoderDecoder {
    private final Random random = new Random();

    public int[] encode(int[][] G, int[] m) {
        /*
        Kodavimas:
        v = (v1, v2, ..., vn)
        g = (g11, g12, ..., g1k, | g1(k+1), ..., g1n)
            (g21, g22, ..., g2k, | g2(k+1), ..., g2n)
            (..., ..., ..., ..., | ..., ..., ..., ...)
            (gk1, gk2, ..., gkk, | gk(k+1), ..., gkn)
            <------- k ------->  | <----- n - k ---->

        užkoduotas vektorius c = m (vektorius) * G (matrica)
         */
        int k = G.length;
        int n = G[0].length;
        int[] c = new int[n];
        for (int i = 0; i < n; i++) {
            c[i] = 0;
            for (int j = 0; j < k; j++) {
                c[i] += m[j] * G[j][i];
            }
            c[i] %= 2;
        }
        return c;
    }

    public int[] send(int[] c) {
        /* Random klaidų įvedimas */
        int[] r = new int[c.length];
        System.arraycopy(c, 0, r, 0, c.length);
        int errorCount = random.nextInt(c.length);
        for (int i = 0; i < errorCount; i++) {
            int errorIndex = random.nextInt(c.length);
            r[errorIndex] = r[errorIndex] == 0 ? 1 : 0;
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

        // Step 1: Extract the submatrix A from G
        int[][] A = new int[k][n - k]; // (k eilučių, n - k stulpelių)
        IntStream.range(0, k)
                .filter(_ -> n - k >= 0)
                .forEach(i -> System.arraycopy(G[i], k, A[i], 0, n - k));

        // Step 2: Transpose the submatrix A to get A^T
        int[][] At = new int[n - k][k]; // (n - k eilučių, k stulpelių)
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < n - k; j++) {
                At[j][i] = A[i][j];
            }
        }

        // Step 3: Construct the parity check matrix H = (A^T | I)
        int[][] H = new int[n - k][n]; // (n - k eilučių, k stulpelių)
        for (int i = 0; i < n - k; i++) {
            System.arraycopy(At[i], 0, H[i], 0, k);
            for (int j = k; j < n; j++) {
                H[i][j] = (i == j - k) ? 1 : 0;
            }
        }

        return H;
    }
}
