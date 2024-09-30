package model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class ExperimentResult {
    private int size;
    private int n;
    private int k;
    private double errorProbability;
    private int totalErrorsIntroduced;
    private int totalErrorsFixed;
    private double averageSuccessRate;
    private double averageDecodingTime;
}
