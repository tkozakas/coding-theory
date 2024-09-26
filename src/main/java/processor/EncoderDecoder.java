package processor;

import model.CosetLeader;

import java.util.*;

public class EncoderDecoder {
    private final Random random = new Random();
    private boolean debug;
    private int introducedErrors = 0;
    private int fixedErrors = 0;
    private final List<Integer> errorPositions = new ArrayList<>();

    /**
     * Encodes a message using the generator matrix G.
     *
     * @param m message to encode
     * @param G generator matrix
     * @return encoded message
     */
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
                    introducedErrors++;
                    errorPositions.add(i);
                } else if (q > 2) {
                    // Change the symbol to a random one
                    int currentSymbol = r[i];
                    int newSymbol;
                    do {
                        newSymbol = random.nextInt(q);
                    } while (newSymbol == currentSymbol);
                    r[i] = newSymbol;
                    introducedErrors++;
                    errorPositions.add(i);
                } else {
                    throw new IllegalArgumentException("Invalid value for q: " + q);
                }

                if (debug) {
                    System.out.printf("Error introduced at position %d: new symbol = %d%n%n", i, r[i]);
                }
            }
        }

        System.out.println("Transmitted vector with errors (r): " + Arrays.toString(r));
        return r;
    }

    /**
     * Generates a random generator matrix G.
     *
     * @param k message length
     * @param n codeword length
     * @return generator matrix
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
     * @param G generator matrix
     * @return parity-check matrix
     * G = [I_k | P]
     * H = [P^T | I_(n-k)]
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

    public int[] computeSyndrome(int[][] H, int[] r) {
        int[] s = new int[H.length];
        for (int i = 0; i < H.length; i++) {
            for (int j = 0; j < H[0].length; j++) {
                s[i] ^= H[i][j] & r[j];
            }
        }
        return s;
    }

    public Map<String, CosetLeader> findCosetLeaders(int[][] H, int maxWeight) {
        int n = H[0].length; // Number of columns in H
        int m = H.length;    // Number of rows in H (syndrome length)
        int totalSyndromes = 1 << m; // Total possible syndromes (2^m)

        // Use LinkedHashMap to preserve the order of insertion (syndromes)
        Map<String, CosetLeader> cosetLeadersMap = new LinkedHashMap<>();

        // Initialize coset leaders with maximum weight (n + 1), assuming no error pattern yet
        initializeCosetLeaders(cosetLeadersMap, m, n);

        if (debug) {
            System.out.println("\n=== Finding Coset Leaders ===");
            System.out.println("Syndrome | Error Pattern | Hamming Weight");
        }

        // Generate and evaluate error patterns for each weight up to maxWeight
        for (int weight = 0; weight <= maxWeight; weight++) {
            List<int[]> errorPatterns = generateErrorPatterns(n, weight);
            updateCosetLeaders(cosetLeadersMap, H, errorPatterns, weight);
        }

        // Remove any coset leaders with null error patterns, which were never updated
        cosetLeadersMap.entrySet().removeIf(entry -> entry.getValue().errorPattern() == null);

        if (debug) {
            System.out.println("Total coset leaders found: " + cosetLeadersMap.size());
        }

        return cosetLeadersMap;
    }

    private void initializeCosetLeaders(Map<String, CosetLeader> cosetLeadersMap, int m, int n) {
        int totalSyndromes = 1 << m;
        for (int i = 0; i < totalSyndromes; i++) {
            int[] syndrome = new int[m];
            for (int j = 0; j < m; j++) {
                syndrome[j] = (i >> (m - 1 - j)) & 1;
            }
            String syndromeStr = Arrays.toString(syndrome);
            cosetLeadersMap.put(syndromeStr, new CosetLeader(syndrome, null, n + 1)); // Initialize with max weight
        }
    }

    private void updateCosetLeaders(Map<String, CosetLeader> cosetLeadersMap, int[][] H, List<int[]> errorPatterns, int weight) {
        for (int[] errorPattern : errorPatterns) {
            int[] syndrome = computeSyndrome(H, errorPattern);
            String syndromeStr = Arrays.toString(syndrome);

            CosetLeader existingLeader = cosetLeadersMap.get(syndromeStr);
            if (existingLeader != null && weight < existingLeader.weight()) {
                // Update the coset leader with the new error pattern if it has a lower weight
                cosetLeadersMap.put(syndromeStr, new CosetLeader(syndrome, errorPattern, weight));
                if (debug) {
                    System.out.println(syndromeStr + " | " + Arrays.toString(errorPattern) + " | " + weight);
                }
            }
        }
    }

    private List<int[]> generateErrorPatterns(int n, int w) {
        List<int[]> patterns = new ArrayList<>();
        int[] pattern = new int[n];
        generateErrorPatternsRecursive(patterns, pattern, 0, 0, w);
        return patterns;
    }

    private void generateErrorPatternsRecursive(List<int[]> patterns, int[] pattern, int start, int ones, int w) {
        if (ones == w) {
            patterns.add(pattern.clone());
            return;
        }
        for (int i = start; i < pattern.length; i++) {
            pattern[i] = 1;
            generateErrorPatternsRecursive(patterns, pattern, i + 1, ones + 1, w);
            pattern[i] = 0;
        }
    }

    public int[] decodeStepByStep(int[] r, int[][] H, Map<String, CosetLeader> cosetLeadersMap) {
        int n = r.length;
        int i = 0; // zero-based index
        int[] rCopy = Arrays.copyOf(r, n);

        while (true) {
            int[] syndrome = computeSyndrome(H, rCopy);
            String syndromeStr = Arrays.toString(syndrome);
            CosetLeader cosetLeader = cosetLeadersMap.get(syndromeStr);
            int w = (cosetLeader != null) ? cosetLeader.weight() : n + 1;

            if (w == 0) {
                // rCopy is a codeword
                if (debug) {
                    System.out.println("Decoded codeword: " + Arrays.toString(rCopy));
                }
                return rCopy;
            }

            if (i >= n) {
                // Reached the end, cannot decode
                System.out.println("Error: Cannot decode the received vector.");
                return rCopy;
            }

            // Flip bit i
            rCopy[i] ^= 1; // Flip the bit

            // Compute new syndrome and coset leader weight
            int[] syndromeNew = computeSyndrome(H, rCopy);
            String syndromeNewStr = Arrays.toString(syndromeNew);
            CosetLeader cosetLeaderNew = cosetLeadersMap.get(syndromeNewStr);
            int wNew = (cosetLeaderNew != null) ? cosetLeaderNew.weight() : n + 1;

            if (wNew < w) {
                fixedErrors++;
                if (debug) {
                    System.out.printf("Flipped bit %d, new coset leader weight %d < %d%n", i + 1, wNew, w);
                }
                // Keep rCopy as is (with the bit flipped)
            } else {
                // Revert the change
                rCopy[i] ^= 1; // Flip the bit back
            }

            i++;
        }
    }

    private void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
    }

    public int getIntroducedErrors() {
        return introducedErrors;
    }

    public int getFixedErrors() {
        return fixedErrors;
    }

    public List<Integer> getErrorPositions() {
        return errorPositions;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void clearErrorPositions() {
        errorPositions.clear();
    }

    public void clearErrors() {
        introducedErrors = 0;
    }
}
