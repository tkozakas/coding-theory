package processor;

import model.CosetLeader;

import java.util.*;

public class EncoderDecoder {
    private final Random random = new Random();

    public int[] encode(int[] m, int[][] G) {
        return matrixVectorMultiply(m, G);
    }

    private int[] matrixVectorMultiply(int[] m, int[][] G) {
        int[] c = new int[G[0].length];
        for (int i = 0; i < G[0].length; i++) {
            for (int j = 0; j < m.length; j++) {
                c[i] += m[j] * G[j][i];
            }
            c[i] %= 2;
        }
        return c;
    }

    public int[] introduceErrors(int[] c, int probability) {
        int[] r = Arrays.copyOf(c, c.length);
        for (int i = 0; i < r.length; i++) {
            if (random.nextInt(100) < probability) {
                r[i] ^= 1; // Flip the bit
            }
        }
        return r;
    }

    public int[][] generateParityCheckMatrix(int[][] G) {
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
            System.arraycopy(Pt[i], 0, H[i], 0, k);
            H[i][k + i] = 1;
        }

        return H;
    }

    public int[] computeSyndrome(int[][] H, int[] r) {
        int[] s = new int[H.length];
        for (int i = 0; i < H.length; i++) {
            for (int j = 0; j < H[0].length; j++) {
                s[i] += H[i][j] * r[j];
            }
            s[i] %= 2;
        }
        return s;
    }

    public List<CosetLeader> findCosetLeaders(int[][] H) {
        int n = H[0].length;
        Map<String, CosetLeader> cosetLeadersMap = new HashMap<>();

        for (int i = 0; i < (1 << n); i++) {
            int[] errorPattern = new int[n];
            for (int j = 0; j < n; j++) {
                errorPattern[j] = (i >> (n - 1 - j)) & 1;
            }

            int[] syndrome = computeSyndrome(H, errorPattern);
            String syndromeStr = Arrays.toString(syndrome);

            int weight = hammingWeight(errorPattern);
            CosetLeader existingLeader = cosetLeadersMap.get(syndromeStr);

            if (existingLeader == null || weight < existingLeader.weight()) {
                cosetLeadersMap.put(syndromeStr, new CosetLeader(syndrome, errorPattern, weight));
            }
        }

        return new ArrayList<>(cosetLeadersMap.values());
    }

    public int[] decode(int[] r, int[][] H, List<CosetLeader> cosetLeaders) {
        int[] syndrome = computeSyndrome(H, r);

        CosetLeader cosetLeader = cosetLeaders.stream()
                .filter(cl -> Arrays.equals(cl.syndrome(), syndrome))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No matching coset leader found."));

        int[] correctedVector = new int[r.length];
        for (int i = 0; i < r.length; i++) {
            correctedVector[i] = (r[i] + cosetLeader.cosetLeader()[i]) % 2;
        }

        return correctedVector;
    }

    private int hammingWeight(int[] vector) {
        int weight = 0;
        for (int bit : vector) {
            weight += bit;
        }
        return weight;
    }
}
