import java.util.Random;

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
        H = (-A^T|I)
        stulpeliai virsta eilutėmis ir atvirkščiai

        G = (1, 0, | 1, 0, 1)
            (0, 1, | 0, 1, 1)
            <-I-> | <-A->

        H = (1, 0, | 1, 0, 0)
            (0, 1, | 0, 1, 0)
            (1, 1, | 0, 0, 1)
            <-A^T-> | <-I->
        */

        int[][] At = new int[n - k][k];
    }
}
