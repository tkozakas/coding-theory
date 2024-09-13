package processor;

import model.CosetLeader;

import java.util.*;

public class EncoderDecoder {
    private final Random random = new Random();
    private boolean debug;
    private int introducedErrors = 0;

    public int[] encode(int[] m, int[][] G) {
        StringBuilder message = Processor.getStringFromBits(m);
        System.out.println("\n=========================================\n");
        System.out.printf("Encoding message (m): %s (%s)%n", Arrays.toString(m), message);

        int[] c = matrixVectorMultiply(m, G);
        if (debug) {
            System.out.println("\nGenerating matrix (G):");
            printMatrix(G);
            System.out.println("\nCodeword (c): " + Arrays.toString(c));
        } else {
            System.out.println("Encoded message (c): " + Arrays.toString(c));
        }
        return c;
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

    public int[] introduceErrors(int[] c, double pe, int q) {
        int[] r = Arrays.copyOf(c, c.length);

        if (debug) {
            System.out.println("\n=== Introducing Errors ===");
            System.out.println("Original codeword (c): " + Arrays.toString(c));
        }

        for (int i = 0; i < r.length; i++) {
            double a = random.nextDouble();
            if (a < pe) {
                if (q == 2) {
                    r[i] ^= 1;
                } else if (q > 2) {
                    int currentSymbol = r[i];
                    int newSymbol;
                    do {
                        newSymbol = random.nextInt(q);
                    } while (newSymbol == currentSymbol);
                    r[i] = newSymbol;
                } else {
                    throw new IllegalArgumentException("Invalid value for q: " + q);
                }

                if (debug) {
                    System.out.printf("Error introduced at position %d: new symbol = %d%n%n", i, r[i]);
                    introducedErrors++;
                }
            }
        }

        System.out.println("Transmitted vector with errors (r): " + Arrays.toString(r));
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

        if (debug) {
            System.out.println("\n=== Parity-Check Matrix Generation ===");
            System.out.println("Parity-check matrix (H):");
            printMatrix(H);
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

        if (debug) {
            System.out.println("\n=== Finding Coset Leaders ===");
            System.out.println("Syndrome | Error Pattern | Hamming Weight");
        }

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
                if (debug) {
                    System.out.println(syndromeStr + " | " + Arrays.toString(errorPattern) + " | " + weight);
                }
            }
        }

        if (debug) {
            System.out.println("Total coset leaders found: " + cosetLeadersMap.size());
        }

        return new ArrayList<>(cosetLeadersMap.values());
    }

    public int[] decode(int[] r, int[][] H, int k, List<CosetLeader> cosetLeaders) {
        int[] syndrome = computeSyndrome(H, r);

        CosetLeader cosetLeader = cosetLeaders.stream()
                .filter(cl -> Arrays.equals(cl.syndrome(), syndrome))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No matching coset leader found."));

        if (debug) {
            System.out.println("\n=== Decoding ===");
            System.out.println("Syndrome (s): " + Arrays.toString(syndrome));
            System.out.printf("Selected coset leader: %s%n", Arrays.toString(cosetLeader.cosetLeader()));
        }

        int[] correctedVector = new int[r.length];
        for (int i = 0; i < r.length; i++) {
            correctedVector[i] = (r[i] + cosetLeader.cosetLeader()[i]) % 2;
        }

        int[] decodedMessage = new int[k];
        System.arraycopy(correctedVector, 0, decodedMessage, 0, k);

        System.out.printf("Corrected codeword: %s%n", Arrays.toString(correctedVector));
        System.out.printf("Decoded message: %s (%s)%n", Arrays.toString(decodedMessage), Processor.getStringFromBits(decodedMessage));
        return decodedMessage;
    }

    private int hammingWeight(int[] vector) {
        int weight = 0;
        for (int bit : vector) {
            weight += bit;
        }
        return weight;
    }

    private void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }

    public int getIntroducedErrors() {
        return introducedErrors;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }
}
