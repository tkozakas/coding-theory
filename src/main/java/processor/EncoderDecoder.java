package processor;

import model.CosetLeader;

import java.util.*;

public class EncoderDecoder {
    private final static Random random = new Random();
    private static final boolean debug = false;

    /**
     * Encodes a message using the generator matrix G.
     *
     * @param m message to encode
     * @param G generator matrix
     * @return encoded message
     */
    public int[] encode(int[] m, int[][] G) {
        int[] c = matrixVectorMultiply(m, G);
        if (debug) {
            StringBuilder message = Processor.getStringFromBits(m);
            System.out.println("\n=========================================\n");
            System.out.printf("Encoding message (m): %s (%s)%n", Arrays.toString(m), message);
            System.out.println("\nGenerating matrix (G):");
            printMatrix(G);
            System.out.println("\nEncoded message (c): " + Arrays.toString(c));
        }
        return c;
    }

    /**
     * Multiplies a matrix by a vector.
     * @param m vector
     * @param G matrix
     * @return result of the multiplication
     */
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

    /**
     * Introduces errors in a codeword.
     * @param c codeword
     * @param pe probability of error
     * @param q number of symbols in the alphabet
     * @return codeword with errors
     */
    public int[] introduceErrors(int[] c, double pe, int q) {
        if (c == null || c.length == 0) {
            throw new IllegalArgumentException("Invalid codeword: " + Arrays.toString(c));
        }
        int[] r = Arrays.copyOf(c, c.length);

        if (debug) {
            System.out.println("\n=== Introducing Errors ===");
            System.out.println("Original codeword (c): " + Arrays.toString(c));
        }

        for (int i = 0; i < r.length; i++) {
            double a = random.nextDouble();
            if (a < pe) {
                if (q == 2) {
                    r[i] ^= 1; // Flip the bit
                } else if (q > 2) {
                    // Change the symbol to a random one
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
                }
            }
        }

        if (debug) {
            System.out.println("Transmitted vector with errors (r): " + Arrays.toString(r));
        }
        return r;
    }

    /**
     * Generates a random k x n generating matrix.
     *
     * @param k number of rows
     * @param n number of columns
     * @return generating matrix
     */
    public int[][] generateGeneratingMatrix(int k, int n) {
        int[][] matrix = new int[k][n];
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                matrix[i][j] = (i == j) ? 1 : 0;
            }
            for (int j = k; j < n; j++) {
                matrix[i][j] = (int) (Math.random() * 2);
            }
        }
        return matrix;
    }

    /**
     * Generates the parity-check matrix H from the generator matrix G.
     * G = [I_k | P]
     * H = [P^T | I_(n-k)]
     * @param G generator matrix
     * @return parity-check matrix
     */
    public int[][] generateParityCheckMatrix(int[][] G) {
        int k = G.length;
        int n = G[0].length;

        if (n <= k) {
            throw new IllegalArgumentException("Invalid matrix dimensions: n must be greater than k to generate a parity-check matrix.");
        }

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

    /**
     * Computes the syndrome of a received vector.
     * @param H parity-check matrix
     * @param r received vector
     * @return syndrome
     */
    public int[] computeSyndrome(int[][] H, int[] r) {
        int[] s = new int[H.length];
        for (int i = 0; i < H.length; i++) {
            for (int j = 0; j < H[0].length; j++) {
                s[i] ^= H[i][j] & r[j];
            }
        }
        return s;
    }

    /**
     * Finds the coset leaders for each syndrome without a maximum weight constraint.
     *
     * @param H parity-check matrix
     * @return map of coset leaders
     */
    public Map<String, CosetLeader> findCosetLeaders(int[][] H) {
        int n = H[0].length; // Length of the codeword
        int m = H.length; // Number of syndromes

        Map<String, CosetLeader> cosetLeadersMap = new LinkedHashMap<>();
        initializeCosetLeaders(cosetLeadersMap, m, n);

        if (debug) {
            System.out.println("\n=== Finding All Optimal Coset Leaders ===");
            System.out.println("Syndrome | Error Pattern | Hamming Weight");
        }

        int weight = 0;
        int unassignedSyndromes = cosetLeadersMap.size();

        // Continue generating error patterns until all coset leaders are assigned with optimal weights
        while (unassignedSyndromes > 0) {
            List<int[]> errorPatterns = generateErrorPatterns(n, weight);
            unassignedSyndromes -= updateCosetLeaders(cosetLeadersMap, H, errorPatterns, weight);
            weight++;
        }

        if (debug) {
            System.out.println("Total coset leaders found: " + cosetLeadersMap.size());
        }

        return cosetLeadersMap;
    }

    private void initializeCosetLeaders(Map<String, CosetLeader> cosetLeadersMap, int m, int n) {
        int totalSyndromes = (int) Math.pow(2, m);
        for (int i = 0; i < totalSyndromes; i++) {
            int[] syndrome = new int[m];
            for (int j = 0; j < m; j++) {
                syndrome[j] = (i >> (m - 1 - j)) & 1;
            }
            String syndromeStr = Arrays.toString(syndrome);
            cosetLeadersMap.put(syndromeStr, new CosetLeader(syndrome, null, n + 1));
        }
    }

    /**
     * Updates coset leaders map with new error patterns and returns the count of updated syndromes.
     *
     * @param cosetLeadersMap map of coset leaders
     * @param H               parity-check matrix
     * @param errorPatterns   list of error patterns of the current weight
     * @param weight          current weight of error patterns
     * @return count of updated syndromes
     */
    private int updateCosetLeaders(Map<String, CosetLeader> cosetLeadersMap, int[][] H, List<int[]> errorPatterns, int weight) {
        int updatedCount = 0;
        for (int[] errorPattern : errorPatterns) {
            int[] syndrome = computeSyndrome(H, errorPattern);
            String syndromeStr = Arrays.toString(syndrome);

            CosetLeader existingLeader = cosetLeadersMap.get(syndromeStr);
            if (existingLeader != null && weight < existingLeader.weight()) {
                // Update the coset leader with the new error pattern if it has a lower weight
                cosetLeadersMap.put(syndromeStr, new CosetLeader(syndrome, errorPattern, weight));
                updatedCount++;
                if (debug) {
                    System.out.println(syndromeStr + " | " + Arrays.toString(errorPattern) + " | " + weight);
                }
            }
        }
        return updatedCount;
    }

    /**
     * Generates all error patterns of a given weight.
     * @param n length of the error pattern
     * @param w weight of the error pattern
     * @return list of error patterns
     */
    private List<int[]> generateErrorPatterns(int n, int w) {
        List<int[]> patterns = new ArrayList<>();
        int[] pattern = new int[n];
        int totalPatterns = (int) Math.pow(2, n);
        for (int i = 0; i < totalPatterns; i++) {
            if (Integer.bitCount(i) == w) {
                for (int j = 0; j < n; j++) {
                    pattern[j] = (i >> j) & 1;
                }
                patterns.add(pattern.clone());
            }
        }
        return patterns;
    }

    /**
     * Decodes a received vector using the parity-check matrix H and coset leaders.
     * @param r received vector
     * @param H parity-check matrix
     * @param cosetLeadersMap map of coset leaders
     * @return decoded vector
     */
    public int[] decodeStepByStep(int[] r, int[][] H, Map<String, CosetLeader> cosetLeadersMap) {
        int n = r.length;
        int i = 0;
        int[] rCopy = Arrays.copyOf(r, n);

        while (true) {
            int[] syndrome = computeSyndrome(H, rCopy);
            String syndromeStr = Arrays.toString(syndrome);
            CosetLeader cosetLeader = cosetLeadersMap.get(syndromeStr);
            int w = (cosetLeader != null) ? cosetLeader.weight() : n + 1;

            if (w == 0) {
                if (debug) {
                    System.out.println("Corrected codeword: " + Arrays.toString(rCopy));
                }
                return rCopy;
            }

            if (i >= n) {
                System.out.println("Error: Cannot decode the received vector.");
                return rCopy;
            }

            // Flip bit i
            rCopy[i] ^= 1;

            // Compute new syndrome and coset leader weight
            int[] syndromeNew = computeSyndrome(H, rCopy);
            String syndromeNewStr = Arrays.toString(syndromeNew);
            CosetLeader cosetLeaderNew = cosetLeadersMap.get(syndromeNewStr);
            int wNew = (cosetLeaderNew != null) ? cosetLeaderNew.weight() : n + 1;

            // If the new weight is less than the previous weight, keep the bit flipped
            if (wNew < w) {
                if (debug) {
                    System.out.printf("Flipped bit %d, new coset leader weight %d < %d%n", i + 1, wNew, w);
                }
            } else {
                rCopy[i] ^= 1;
            }
            i++;
        }
    }

    public StringBuilder printMatrix(int[][] matrix) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int[] row : matrix) {
            stringBuilder.append(Arrays.toString(row)).append("\n");
        }
        System.out.println(stringBuilder);
        return stringBuilder;
    }
}
