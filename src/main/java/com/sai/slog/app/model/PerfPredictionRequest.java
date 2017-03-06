package com.sai.slog.app.model;

import lombok.Data;

/**
 * Created by saipkri on 06/03/17.
 */
@Data
public class PerfPredictionRequest {
    private double[][] independentVars;
    private double[] dependentVars;
    private Double[] input;
}
