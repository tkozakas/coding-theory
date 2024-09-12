import java.util.*;

public class EncoderDecoder {
    private final Random random = new Random();

    public int[] encode(int[][] G, int[] m) {
        return matrixVectorMultiply(G, m);
    }

    private static int[] matrixVectorMultiply(int[][] G, int[] m) {
        int[] c = new int[G[0].length];
        for (int i = 0; i < G[0].length; i++) {
            for (int j = 0; j < G.length; j++) {
                c[i] += G[j][i] * m[j];
            }
            c[i] %= 2;
        }
        return c;
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

    public int[][] parityCheckMatrix(int[][] G) {
        /*
            G = (I | P) =   (1, 0, 0 | 1, 0, 1)
                            (0, 1, 0 | 1, 1, 0)
                            (0, 0, 1 | 0, 1, 1)
            H = (P^T | I(n-k)) = (1, 1, 0, 1, 0, 0)
                                 (0, 1, 1, 0, 1, 0)
                                 (1, 0, 1, 0, 0, 1)
         */

        int k = G.length;
        int n = G[0].length;
        int[][] P = new int[k][n - k];

        for (int i = 0; i < k; i++) {
            System.arraycopy(G[i], k, P[i], 0, n - k);
        }

        int[][] Pt = new int[n - k][k];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < n - k; j++) {
                Pt[j][i] = P[i][j];
            }
        }

        int[][] H = new int[n - k][n];
        for (int i = 0; i < n - k; i++) {
            System.arraycopy(Pt[i], 0, H[i], 0, n - k);
            H[i][n - k + i] = 1;
        }

        return H;
    }

    public int[] syndrome(int[][] H, int[] r) {
        /*
            H = (1, 1, 0)
                (0, 1, 1)
                (1, 0, 1)
                (1, 0, 0)
                (0, 1, 0)
                (0, 0, 1)
            r = (1, 0, 1, 1, 0, 1)
            s = H^T * r = (1, 0, 1, 1, 1, 0) * (1, 0, 1)
                                               (1, 1, 0)
                                               (0, 1, 1)
                                               (1, 0, 0)
                                               (0, 1, 0)
                                               (0, 0, 1)

         */

        int[][] Ht = new int[H[0].length][H.length];
        for (int i = 0; i < H.length; i++) {
            for (int j = 0; j < H[0].length; j++) {
                Ht[j][i] = H[i][j];
            }
        }

        return matrixVectorMultiply(Ht, r);
    }

    public int hammingWeight(int[] vector) {
        int weight = 0;
        for (int bit : vector) {
            weight += bit;
        }
        return weight;
    }

    public List<CosetLeader> findCosetLeaders(int[][] H) {
        int n = H[0].length;
        Map<String, CosetLeader> cosetLeadersMap = new HashMap<>();

        for (int i = 0; i < (1 << n); i++) {
            int[] errorPattern = new int[n];
            for (int j = 0; j < n; j++) {
                errorPattern[j] = (i >> (n - 1 - j)) & 1;
            }

            int[] syndrome = syndrome(H, errorPattern);
            String syndromeStr = Arrays.toString(syndrome);

            if (!cosetLeadersMap.containsKey(syndromeStr) || hammingWeight(errorPattern) < cosetLeadersMap.get(syndromeStr).weight()) {
                cosetLeadersMap.put(syndromeStr, new CosetLeader(syndrome, errorPattern, hammingWeight(errorPattern)));
            }
        }

        return new ArrayList<>(cosetLeadersMap.values());
    }

    public int[] decode(int[] r, int[][] H, List<CosetLeader> cosetLeaders) {
        int[] syndrome = syndrome(H, r);

        CosetLeader cosetLeader = cosetLeaders.stream()
                .filter(cl -> Arrays.equals(cl.syndrome(), syndrome))
                .findFirst()
                .orElseThrow();

        if (cosetLeader.weight() == 0) {
            return Arrays.copyOfRange(r, 0, H.length);
        }

        int[] correctedVector = new int[r.length];
        for (int i = 0; i < r.length; i++) {
            correctedVector[i] = r[i] ^ cosetLeader.cosetLeader()[i];
        }

        return Arrays.copyOfRange(correctedVector, 0, H.length);
    }
}
