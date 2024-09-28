package processor;

import model.ExperimentResult;
import ui.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Experiment {
    private final int runs = 100;
    private final Data data = Data.getInstance();

    public void run() {
        System.out.println("Starting experiments...");
        int[] inputSizes = {3, 5, 10, 15, 20, 30, 50};
        int[][] matrixSizes = {{5, 3}, {10, 7}, {15, 10}, {20, 15}, {30, 20}};
        int[] errorCounts = {1, 2, 3, 4, 5, 6};

        List<ExperimentResult> results = new ArrayList<>();

        for (int size : inputSizes) {
            System.out.printf("\n--- Input Size: %d ---\n", size);
            for (int[] matrixSize : matrixSizes) {
                int n = matrixSize[0];
                int k = matrixSize[1];
                if (n <= k) {
                    continue;
                }
                System.out.printf("Matrix Size: (n = %d, k = %d)\n", n, k);

                configureData(n, k);

                for (int errorCount : errorCounts) {
                    System.out.printf("Introducing %d deliberate errors.\n", errorCount);


                    ExperimentResult result = runExperiment(size, errorCount, runs);

                    results.add(result);

                    System.out.printf("Avg Success Rate: %.2f%%, Avg Errors Introduced: %.2f, Avg Errors Fixed: %.2f\n",
                            result.getAverageSuccessRate() * 100, result.getAverageErrorsIntroduced(),
                            result.getAverageErrorsFixed());
                }
            }
        }
        printResultsTable(results);
        System.out.println("Experiments completed.");
    }

    private void configureData(int n, int k) {
        data.setN(n);
        data.setK(k);
        data.generateGeneratingMatrix();
        data.generateParityCheckMatrix();
        data.generateCosetLeaders();
    }

    private ExperimentResult runExperiment(int size, int errorCount, int runs) {
        int totalErrorsIntroduced = 0;
        int totalErrorsFixed = 0;
        double totalSuccessRate = 0.0;

        for (int run = 0; run < runs; run++) {
            data.setInputBits(generateRandomBits(size));
            data.setCurrentBitPosition(0);

            int errorsIntroducedThisRun = 0;
            int errorsFixedThisRun = 0;

            while (data.getCurrentBitPosition() < data.getInputBits().length) {
                data.nextBlock();
                data.encodeBlock();
                int introducedErrors = introduceFixedErrors(errorCount);
                errorsIntroducedThisRun += introducedErrors;
                data.decodeBlock();
                errorsFixedThisRun += data.getFixedCount();
            }

            totalErrorsIntroduced += errorsIntroducedThisRun;
            totalErrorsFixed += errorsFixedThisRun;

            double successRate = errorsIntroducedThisRun == 0 ? 1.0 : (double) errorsFixedThisRun / errorsIntroducedThisRun;
            totalSuccessRate += successRate;
        }

        double averageSuccessRate = totalSuccessRate / runs;
        double averageErrorsIntroduced = (double) totalErrorsIntroduced / runs;
        double averageErrorsFixed = (double) totalErrorsFixed / runs;

        return new ExperimentResult(size, data.getN(), data.getK(), 0, totalErrorsIntroduced,
                totalErrorsFixed, averageSuccessRate, averageErrorsIntroduced, averageErrorsFixed);
    }

    private int introduceFixedErrors(int errorCount) {
        int[] encodedBlock = data.getEncodedBlock();
        int blockLength = encodedBlock.length;

        if (errorCount > blockLength) {
            errorCount = blockLength;
            System.out.printf("Error count adjusted to %d to fit block length.\n", blockLength);
        }

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < blockLength; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);

        for (int i = 0; i < errorCount; i++) {
            int pos = indices.get(i);
            encodedBlock[pos] ^= 1;
        }

        data.setBlockWithError(encodedBlock);
        return errorCount;
    }

    private int[] generateRandomBits(int size) {
        Random random = new Random();
        return random.ints(size, 0, 2).toArray();
    }

    private void printResultsTable(List<ExperimentResult> results) {
        System.out.println("\nResults Summary:");
        System.out.printf("%-12s %-8s %-8s %-20s %-20s %-15s %-20s %-20s%n",
                "Input Size", "n", "k", "Errors Introduced",
                "Errors Fixed", "Avg Success Rate (%)", "Avg Errors Introduced", "Avg Errors Fixed");
        for (ExperimentResult result : results) {
            System.out.printf("%-12d %-8d %-8d %-20d %-20d %-15.2f %-20.2f %-20.2f%n",
                    result.getSize(), result.getN(), result.getK(),
                    result.getTotalErrorsIntroduced(), result.getTotalErrorsFixed(),
                    result.getAverageSuccessRate() * 100, result.getAverageErrorsIntroduced(),
                    result.getAverageErrorsFixed());
        }
    }
}
