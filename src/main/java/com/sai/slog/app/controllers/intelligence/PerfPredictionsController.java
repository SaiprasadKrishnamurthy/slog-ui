package com.sai.slog.app.controllers.intelligence;

import com.sai.slog.app.model.PerfPredictionRequest;
import com.sai.slog.app.model.PerformanceLogEvent;
import com.sai.slog.app.service.LogService;
import lombok.Data;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by saipkri on 03/02/17.
 */
@Data
public class PerfPredictionsController {

    private String functionality;
    private List<String> functionalities;
    private List<PerformanceLogEvent> performanceLogEvents;
    private LogService logService = new LogService();
    private List<String> headers = new ArrayList<>();
    private Object[] values = null;
    private boolean render;
    private boolean renderPredict;
    private double perfPredictionValue;


    public PerfPredictionsController() {
        functionalities = logService.allPerfLogFunctionalities();
    }

    public void view() {
        headers.clear();
        performanceLogEvents = logService.perfSamples(functionality);
        if (!performanceLogEvents.isEmpty()) {
            headers.addAll(performanceLogEvents.get(0).getMetricMap().keySet());
        }
        render = !headers.isEmpty();
        values = new Object[headers.size()];
    }

    public void predict() {
        renderPredict = false;
        System.out.println(headers);
        System.out.println(Arrays.deepToString(values));
        String filterValue = Stream.of(values).filter(v -> v != null && !NumberUtils.isNumber(v.toString())).findFirst().map(o -> o.toString()).get();
        String filterVar = "";
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null && values[i].toString().trim().length() != 0 && !NumberUtils.isNumber(values[i].toString().trim())) {
                filterVar = headers.get(i);
                break;
            }
        }
        String valueToBePredicted = "";
        for (int i = 0; i < values.length; i++) {
            if (values[i] == null || values[i].toString().trim().length() == 0) {
                valueToBePredicted = headers.get(i);
                break;
            }
        }
        final String predictionVar = valueToBePredicted;
        final String _filterVar = filterVar;
        List<String> independantVariables = headers.stream().filter(h -> !h.equals(predictionVar.trim()) & !h.equals(_filterVar.trim())).collect(toList());
        ;
        System.out.println("Filter: " + filterValue);
        System.out.println("Value to be predicted: " + valueToBePredicted);
        System.out.println("independantVariables: " + independantVariables);
        System.out.println("Filter variable: " + filterVar);

        List<PerformanceLogEvent> filtered = performanceLogEvents.stream().filter(pe -> pe.getMetricMap().get(_filterVar).toString().equals(filterValue.trim())).collect(toList());

        double[][] independantVarArr = new double[filtered.size()][independantVariables.size()];
        double[] dependentVarArr = new double[filtered.size()];

        PerfPredictionRequest perfPredictionRequest = new PerfPredictionRequest();
        for (int i = 0; i < filtered.size(); i++) {
            PerformanceLogEvent performanceLogEvent = filtered.get(i);
            for (int j = 0; j < independantVariables.size(); j++) {
                independantVarArr[i][j] = Double.parseDouble(performanceLogEvent.getMetricMap().get(independantVariables.get(j)).toString().trim());
            }
        }

        for (int i = 0; i < filtered.size(); i++) {
            PerformanceLogEvent performanceLogEvent = filtered.get(i);
            dependentVarArr[i] = Double.parseDouble(performanceLogEvent.getMetricMap().get(valueToBePredicted.trim()).toString().trim());
        }

        List<Double> input = Stream.of(values).filter(v -> v.toString().trim().length() > 0 && !v.equals(filterValue)).map(d -> Double.parseDouble(d.toString().trim())).collect(toList());
        perfPredictionRequest.setIndependentVars(independantVarArr);
        perfPredictionRequest.setDependentVars(dependentVarArr);
        Double[] objects = input.toArray(new Double[input.size()]);
        perfPredictionRequest.setInput(objects);
        System.out.println(perfPredictionRequest);
        perfPredictionValue = logService.predictPerf(perfPredictionRequest);
        renderPredict = true;
    }
}
