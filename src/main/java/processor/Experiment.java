package processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Experiment {
    private final Data data = Data.getInstance();

    private static class ExperimentResult {
        int k, n;
        double pe;
        List<Object[]> resultsTable;

        public ExperimentResult(int k, int n, double pe, List<Object[]> resultsTable) {
            this.k = k;
            this.n = n;
            this.pe = pe;
            this.resultsTable = resultsTable;
        }
    }

    public void run() {
        runPerformanceExperiment();
        runMatrixGenerationExperiment();
    }

    public void runPerformanceExperiment() {
        int[] ks = {8};
        int totalIterations = 100;
        int totalInputSize = 500;
        double[] probabilities = {0.0001, 0.001, 0.01};
        Random random = new Random();
        List<ExperimentResult> allResults = new ArrayList<>();

        for (double pe : probabilities) {
            data.setPe(pe);

            for (int k : ks) {
                int n = k + 1;
                List<Object[]> resultsTable = new ArrayList<>();

                for (int inputSize = 2; inputSize <= totalInputSize; inputSize *= 2) {
                    long totalDecodingTime = 0;
                    long totalEncodingTime = 0;
                    long totalErrorIntroductionTime = 0;

                    for (int iteration = 0; iteration < totalIterations; iteration++) {
                        data.setK(k);
                        data.setN(n);

                        StringBuilder inputBits = new StringBuilder();
                        for (int i = 0; i < inputSize; i++) {
                            inputBits.append(random.nextInt(2) == 0 ? 0 : 1);
                            if (i < inputSize - 1) {
                                inputBits.append(", ");
                            }
                        }
                        data.generateInputBits("Vector", inputBits.toString());
                        data.generateGeneratingMatrix();
                        data.generateParityCheckMatrix();
                        data.generateCosetLeaders();

                        long decodingStartTime = System.nanoTime();

                        while (data.getCurrentBitPosition() < data.getInputBits().length) {
                            data.nextBlock();

                            long encodingStartTime = System.nanoTime();
                            data.encodeBlock();
                            long encodingEndTime = System.nanoTime();
                            totalEncodingTime += (encodingEndTime - encodingStartTime);

                            long errorIntroStartTime = System.nanoTime();
                            data.introduceErrors();
                            long errorIntroEndTime = System.nanoTime();
                            totalErrorIntroductionTime += (errorIntroEndTime - errorIntroStartTime);

                            data.decodeBlock();
                        }

                        long decodingEndTime = System.nanoTime();
                        totalDecodingTime += (decodingEndTime - decodingStartTime);

                        data.clear();
                    }

                    long averageDecodingTime = totalDecodingTime / totalIterations;
                    long averageEncodingTime = totalEncodingTime / totalIterations;
                    long averageErrorIntroductionTime = totalErrorIntroductionTime / totalIterations;

                    resultsTable.add(new Object[]{
                            inputSize, averageEncodingTime, averageDecodingTime, averageErrorIntroductionTime
                    });
                }

                allResults.add(new ExperimentResult(k, n, pe, resultsTable));
            }
        }

        printPerformanceResults(allResults);
    }

    public void runMatrixGenerationExperiment() {
        int[] ks = {4, 8, 16};
        List<ExperimentResult> allResults = new ArrayList<>();

        for (int k : ks) {
            List<Object[]> resultsTable = new ArrayList<>();

            for (int n = k + 1; n <= k + 10; n++) {
                long totalGenMatrixTime = 0;
                long totalParityCheckTime = 0;
                long totalCosetLeaderTime = 0;
                int totalIterations = 10;

                int cosetLeaderCount = (int) Math.pow(2, n - k);

                for (int iteration = 0; iteration < totalIterations; iteration++) {
                    data.setK(k);
                    data.setN(n);

                    long genMatrixStartTime = System.nanoTime();
                    data.generateGeneratingMatrix();
                    long genMatrixEndTime = System.nanoTime();
                    totalGenMatrixTime += (genMatrixEndTime - genMatrixStartTime);

                    long parityCheckStartTime = System.nanoTime();
                    data.generateParityCheckMatrix();
                    long parityCheckEndTime = System.nanoTime();
                    totalParityCheckTime += (parityCheckEndTime - parityCheckStartTime);

                    long cosetLeaderStartTime = System.nanoTime();
                    data.generateCosetLeaders();
                    long cosetLeaderEndTime = System.nanoTime();
                    totalCosetLeaderTime += (cosetLeaderEndTime - cosetLeaderStartTime);
                }

                long averageGenMatrixTime = totalGenMatrixTime / totalIterations;
                long averageParityCheckTime = totalParityCheckTime / totalIterations;
                long averageCosetLeaderTime = totalCosetLeaderTime / totalIterations;

                resultsTable.add(new Object[]{
                        k, n, averageGenMatrixTime, averageParityCheckTime, averageCosetLeaderTime, cosetLeaderCount
                });
            }

            allResults.add(new ExperimentResult(k, 0, 0.0, resultsTable));
        }

        printMatrixGenerationResults(allResults);
    }


    private void printPerformanceResults(List<ExperimentResult> allResults) {
        for (ExperimentResult result : allResults) {
            System.out.println("\nPerformance Results for k = " + result.k + ", n = " + result.n + ", pe = " + result.pe);
            printPerformanceTable(result.resultsTable);
        }
    }

    private void printMatrixGenerationResults(List<ExperimentResult> allResults) {
        for (ExperimentResult result : allResults) {
            System.out.println("\nMatrix Generation Results for k = " + result.k);
            printMatrixGenerationTable(result.resultsTable);
        }
    }

    private void printPerformanceTable(List<Object[]> resultsTable) {
        System.out.println("\nPerformance Results Table:");
        System.out.println("-.".repeat(75));
        System.out.printf("%-12s | %-20s | %-20s | %-25s\n",
                "Input Size", "Avg Encoding Time (ns)", "Avg Decoding Time (ns)", "Avg Error Introduction Time (ns)");
        System.out.println("-.".repeat(75));

        for (Object[] result : resultsTable) {
            System.out.printf("%-12d | %-20d | %-20d | %-25d\n",
                    (int) result[0],
                    (long) result[1],
                    (long) result[2],
                    (long) result[3]);
        }

        System.out.println("-.".repeat(75));
    }

    private void printMatrixGenerationTable(List<Object[]> resultsTable) {
        System.out.println("\nMatrix Generation Results Table:");
        System.out.println("-.".repeat(75));
        System.out.printf("%-12s | %-12s | %-25s | %-25s | %-25s | %-20s\n",
                "k", "n", "Avg Gen Matrix Time (ns)", "Avg Parity Check Time (ns)", "Avg Coset Leader Time (ns)", "Coset Leaders");
        System.out.println("-.".repeat(75));

        for (Object[] result : resultsTable) {
            System.out.printf("%-12d | %-12d | %-25d | %-25d | %-25d | %-20d\n",
                    (int) result[0],
                    (int) result[1],
                    (long) result[2],
                    (long) result[3],
                    (long) result[4],
                    (int) result[5]);
        }

        System.out.println("-.".repeat(75));
    }


    public static void main(String[] args) {
        Experiment experiment = new Experiment();
        experiment.run();
    }
}
