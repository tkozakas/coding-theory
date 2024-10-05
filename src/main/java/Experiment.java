import processor.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Experiment {
    private final Data data = Data.getInstance();

    private static class ExperimentResult {
        int k, n;
        int introducedMistakes;
        List<Object[]> resultsTable;

        public ExperimentResult(int k, int n, int introducedMistakes, List<Object[]> resultsTable) {
            this.k = k;
            this.n = n;
            this.introducedMistakes = introducedMistakes;
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
        int[] mistakes = {1, 2, 3};
        Random random = new Random();
        List<ExperimentResult> allResults = new ArrayList<>();

        for (int introducedMistakes : mistakes) {

            for (int k : ks) {
                int n = k + 1;
                List<Object[]> resultsTable = new ArrayList<>();

                for (int inputSize = 2; inputSize <= totalInputSize; inputSize *= 2) {
                    long totalDecodingTime = 0;
                    long totalEncodingTime = 0;
                    long totalErrorIntroductionTime = 0;
                    long totalIntroducedErrors = 0;
                    long totalFixedErrors = 0;

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
                            introduceManualErrors(introducedMistakes, random);
                            long errorIntroEndTime = System.nanoTime();
                            totalErrorIntroductionTime += (errorIntroEndTime - errorIntroStartTime);
                            totalIntroducedErrors += introducedMistakes;

                            data.decodeBlock();

                            int fixedErrors = data.getFixedCount();
                            totalFixedErrors += fixedErrors;

                            System.out.printf("Errors: %d, Fixed: %d\n Block: %s\n Encoded: %s\n With Error: %s\n Corrected: %s\n Decoded: %s\n\n",
                                    data.getTotalErrors(), fixedErrors,
                                    Arrays.toString(data.getBlock()),
                                    Arrays.toString(data.getEncodedBlock()),
                                    Arrays.toString(data.getBlockWithError()),
                                    Arrays.toString(data.getCorrectedBlock()),
                                    Arrays.toString(data.getDecodedBlock()));
                        }

                        long decodingEndTime = System.nanoTime();
                        totalDecodingTime += (decodingEndTime - decodingStartTime);

                        data.clear();
                    }

                    long averageDecodingTime = totalDecodingTime / totalIterations;
                    long averageEncodingTime = totalEncodingTime / totalIterations;
                    long averageErrorIntroductionTime = totalErrorIntroductionTime / totalIterations;

                    double averageSuccessRate = 100.0;
                    if (totalIntroducedErrors > 0) {
                        averageSuccessRate = ((double) totalFixedErrors / totalIntroducedErrors) * 100.0;
                    }

                    resultsTable.add(new Object[]{
                            inputSize, averageEncodingTime, averageDecodingTime, averageErrorIntroductionTime, averageSuccessRate
                    });
                }

                allResults.add(new ExperimentResult(k, n, introducedMistakes, resultsTable));
            }
        }

        printPerformanceResults(allResults);
    }

    private void introduceManualErrors(int mistakes, Random random) {
        data.setBlockWithError(data.getEncodedBlock().clone());
        for (int i = 0; i < mistakes; i++) {
            int position = random.nextInt(data.getBlockWithError().length);
            data.getBlockWithError()[position] = data.getBlockWithError()[position] == 0 ? 1 : 0;
        }

        data.setBlockWithoutCodeError(data.getBlockWithoutCode().clone());
        for (int i = 0; i < mistakes; i++) {
            int position = random.nextInt(data.getBlockWithoutCodeError().length);
            data.getBlockWithoutCodeError()[position] = data.getBlockWithoutCodeError()[position] == 0 ? 1 : 0;
        }
        data.setTotalErrors(mistakes);
        data.setTotalNoCodingErrors(mistakes);
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

            allResults.add(new ExperimentResult(k, 0, 0, resultsTable));
        }

        printMatrixGenerationResults(allResults);
    }

    private void printPerformanceResults(List<ExperimentResult> allResults) {
        for (ExperimentResult result : allResults) {
            System.out.println("\nPerformance Results for k = " + result.k + ", n = " + result.n + ", Mistakes = " + result.introducedMistakes);
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
        System.out.printf("%-12s | %-20s | %-20s | %-25s | %-20s\n",
                "Input Size", "Avg Encoding Time (ns)", "Avg Decoding Time (ns)", "Avg Error Introduction Time (ns)", "Avg Success Rate (%)");
        System.out.println("-.".repeat(75));

        for (Object[] result : resultsTable) {
            System.out.printf("%-12d | %-20d | %-20d | %-25d | %-20.2f\n",
                    (int) result[0],
                    (long) result[1],
                    (long) result[2],
                    (long) result[3],
                    (double) result[4]);
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
