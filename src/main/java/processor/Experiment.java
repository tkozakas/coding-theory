package processor;

import model.ExperimentResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Experiment {
    private final Data data = Data.getInstance();

    public void run() {
        System.out.println("Starting experiments...");

        int inputSize = 80;
        double errorProbability = 0.0001;

        List<int[]> matrixSizes = generateMatrixSizes();

        List<ExperimentResult> results = new ArrayList<>();

        for (int[] matrixSize : matrixSizes) {
            int n = matrixSize[0];
            int k = matrixSize[1];
            if (n <= k) {
                continue;
            }

            System.out.printf("\n--- Matrix Size: n = %d, k = %d ---\n", n, k);

            configureData(n, k);
            int runs = 1;
            ExperimentResult result = runExperiment(inputSize, runs, errorProbability);

            results.add(result);

            System.out.printf("Avg Success Rate: %.2f%%\n", result.getAverageSuccessRate() * 100);
            System.out.printf("Avg Decoding Time: %.2f ms\n", result.getAverageDecodingTime());
        }

        printResultsTable(results);
        System.out.println("Experiments completed.");
    }

    private List<int[]> generateMatrixSizes() {
        List<int[]> matrixSizes = new ArrayList<>();
        // Generate (n, k) pairs where n ranges from k+1 to 80 and k ranges from 5 to 50
        for (int k = 5; k <= 50; k += 10) {
            for (int n = k + 1; n <= Math.min(k + 20, 80); n += 5) {
                matrixSizes.add(new int[]{n, k});
            }
        }
        return matrixSizes;
    }

    private void configureData(int n, int k) {
        data.setN(n);
        data.setK(k);
        data.generateGeneratingMatrix();
        data.generateParityCheckMatrix();
        data.generateCosetLeaders();
    }

    private ExperimentResult runExperiment(int inputSize, int runs, double errorProbability) {
        int totalErrorsIntroduced = 0;
        int totalErrorsFixed = 0;
        double totalSuccessRate = 0.0;
        double totalDecodingTime = 0.0;

        for (int run = 0; run < runs; run++) {
            data.setInputBits(generateRandomBits(inputSize));
            data.setCurrentBitPosition(0);
            long decodingTimeThisRun = 0;
            int errorsIntroducedThisRun = 0;
            int errorsFixedThisRun = 0;
            while (data.getCurrentBitPosition() < data.getInputBits().length) {
                data.nextBlock();
                data.encodeBlock();

                data.setPe(errorProbability);
                data.introduceErrors();
                errorsIntroducedThisRun += data.getErrorCount();

                long startTime = System.nanoTime();
                data.decodeBlock();
                long endTime = System.nanoTime();

                decodingTimeThisRun += (endTime - startTime) / 1_000_000;
                errorsFixedThisRun += data.getFixedCount();
            }

            totalErrorsIntroduced += errorsIntroducedThisRun;
            totalErrorsFixed += errorsFixedThisRun;

            double successRate = errorsIntroducedThisRun == 0 ? 1.0
                    : (double) errorsFixedThisRun / errorsIntroducedThisRun;
            totalSuccessRate += successRate;
            totalDecodingTime += decodingTimeThisRun;
        }
        double averageSuccessRate = totalSuccessRate / runs;
        double averageDecodingTime = totalDecodingTime / runs;

        return new ExperimentResult(inputSize, data.getN(), data.getK(), 0, totalErrorsIntroduced,
                totalErrorsFixed, averageSuccessRate, averageDecodingTime);
    }

    private int[] generateRandomBits(int size) {
        Random random = new Random();
        return random.ints(size, 0, 2).toArray();
    }

    private void printResultsTable(List<ExperimentResult> results) {
        System.out.println("\nResults Summary:");
        System.out.printf("%-12s %-8s %-8s %-20s %-20s %-18s %-18s%n",
                "Input Size", "n", "k", "Errors Introduced",
                "Errors Fixed", "Success Rate (%)", "Avg Decoding Time (ms)");

        for (ExperimentResult result : results) {
            if (result != null) {
                System.out.printf("%-12d %-8d %-8d %-20d %-20d %-18.2f %-18.5f%n",
                        result.getSize(), result.getN(), result.getK(),
                        result.getTotalErrorsIntroduced(), result.getTotalErrorsFixed(),
                        result.getAverageSuccessRate() * 100, result.getAverageDecodingTime());
            }
        }
    }
}
