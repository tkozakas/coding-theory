import processor.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Experiment {
    private final Data data = Data.getInstance();
    private final List<Object[]> successRateResultsTable = new ArrayList<>();
    private final List<ExperimentResult> performanceResults = new ArrayList<>();
    private final List<ExperimentResult> matrixGenerationResults = new ArrayList<>();

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
        runSuccessRateExperiment();
        runPerformanceExperiment();
        runMatrixGenerationExperiment();

        printResults();
    }

    private String generateInputBits(int size, Random random) {
        StringBuilder inputBits = new StringBuilder();
        for (int i = 0; i < size; i++) {
            inputBits.append(random.nextInt(2) == 0 ? "0" : "1");
            if (i < size - 1) {
                inputBits.append(", ");
            }
        }
        return inputBits.toString();
    }

    private void introduceManualErrors(int mistakes, Random random) {
        data.setBlockWithError(data.getEncodedBlock().clone());
        for (int i = 0; i < mistakes; i++) {
            int position = random.nextInt(data.getBlockWithError().length);
            data.getBlockWithError()[position] = data.getBlockWithError()[position] == 0 ? 1 : 0;
        }

        data.setBlockWithoutCodeAndError(data.getBlockWithoutCode().clone());
        for (int i = 0; i < mistakes; i++) {
            int position = random.nextInt(data.getBlockWithoutCodeAndError().length);
            data.getBlockWithoutCodeAndError()[position] = data.getBlockWithoutCodeAndError()[position] == 0 ? 1 : 0;
        }
        data.setTotalErrors(mistakes);
        data.setTotalNoCodingErrors(mistakes);
    }

    private void runExperiment(int k, int n, int inputSize, int introducedMistakes, int iterations, Random random, List<Object[]> resultsTable) {
        long totalIntroducedErrors = 0;
        long totalFixedErrors = 0;

        for (int iteration = 0; iteration < iterations; iteration++) {
            data.setK(k);
            data.setN(n);
            data.generateInputBits("Vector", generateInputBits(inputSize, random));
            data.generateGeneratingMatrix();
            data.generateParityCheckMatrix();
            data.generateCosetLeaders();

            while (data.getCurrentBitPosition() < data.getInputBits().length) {
                data.nextBlock();
                data.encodeBlock();
                introduceManualErrors(introducedMistakes, random);
                data.decodeBlock();

                totalFixedErrors += data.getFixedCount();
                totalIntroducedErrors += data.getTotalErrors();
            }

            data.clear();
        }

        double averageSuccessRate = totalIntroducedErrors > 0 ? ((double) totalFixedErrors / totalIntroducedErrors) * 100.0 : 100.0;
        resultsTable.add(new Object[]{inputSize, averageSuccessRate});
    }

    public void runSuccessRateExperiment() {
        int k = 8;
        int totalIterations = 100;
        int totalInputSize = 250;
        Random random = new Random();

        int[] nValues = new int[10];
        for (int i = 0; i < 10; i++) {
            nValues[i] = k + i + 1;
        }

        int[] mistakesArray = {1};

        for (int inputSize = 2; inputSize <= totalInputSize; inputSize *= 2) {
            for (int introducedMistakes : mistakesArray) {
                for (int n : nValues) {
                    List<Object[]> resultsTable = new ArrayList<>();
                    runExperiment(k, n, inputSize, introducedMistakes, totalIterations, random, resultsTable);
                    successRateResultsTable.addAll(resultsTable);
                }
            }
        }
    }

    public void runPerformanceExperiment() {
        int k = 8;
        int[] mistakesArray = {1, 2, 3};
        int totalIterations = 100;
        int totalInputSize = 500;
        Random random = new Random();

        for (int introducedMistakes : mistakesArray) {
            for (int n = k + 1; n <= k + 10; n++) {
                List<Object[]> resultsTable = new ArrayList<>();
                for (int inputSize = 2; inputSize <= totalInputSize; inputSize *= 2) {
                    runExperiment(k, n, inputSize, introducedMistakes, totalIterations, random, resultsTable);
                }
                performanceResults.add(new ExperimentResult(k, n, introducedMistakes, resultsTable));
            }
        }
    }

    public void runMatrixGenerationExperiment() {
        int[] ks = {4, 8, 16};
        int totalIterations = 10;

        for (int k : ks) {
            for (int n = k + 1; n <= k + 10; n++) {
                List<Object[]> resultsTable = new ArrayList<>();
                long totalGenMatrixTime = 0;
                long totalParityCheckTime = 0;
                long totalCosetLeaderTime = 0;

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

                long avgGenMatrixTime = totalGenMatrixTime / totalIterations;
                long avgParityCheckTime = totalParityCheckTime / totalIterations;
                long avgCosetLeaderTime = totalCosetLeaderTime / totalIterations;
                resultsTable.add(new Object[]{k, n, avgGenMatrixTime, avgParityCheckTime, avgCosetLeaderTime});
                matrixGenerationResults.add(new ExperimentResult(k, n, 0, resultsTable));
            }
        }
    }

    private void printResults() {
        printSuccessRateTable();
        printPerformanceResults(performanceResults);
        printMatrixGenerationResults(matrixGenerationResults);
    }

    private void printPerformanceResults(List<ExperimentResult> allResults) {
        for (ExperimentResult result : allResults) {
            System.out.println("\nPerformance Results for k = " + result.k + ", n = " + result.n + ", Mistakes = " + result.introducedMistakes);
            printFormattedTable(result.resultsTable, new String[]{"Input Size", "Avg Success Rate (%)"});
        }
    }

    private void printMatrixGenerationResults(List<ExperimentResult> allResults) {
        for (ExperimentResult result : allResults) {
            System.out.println("\nMatrix Generation Results for k = " + result.k);
            printFormattedTable(result.resultsTable, new String[]{"k", "n", "Avg Gen Matrix Time (ns)", "Avg Parity Check Time (ns)", "Avg Coset Leader Time (ns)"});
        }
    }

    private void printSuccessRateTable() {
        System.out.println("\nSuccess Rate Results Table:");
        printFormattedTable(successRateResultsTable, new String[]{"Input Size", "Avg Success Rate (%)"});
    }

    private void printFormattedTable(List<Object[]> resultsTable, String[] headers) {
        // Print headers
        for (String header : headers) {
            System.out.printf("%-20s | ", header);
        }
        System.out.println();
        System.out.println("-".repeat(20 * headers.length + 3));

        // Print table rows
        for (Object[] row : resultsTable) {
            for (Object value : row) {
                System.out.printf("%-20s | ", value);
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) {
        Experiment experiment = new Experiment();
        experiment.run();
    }
}
