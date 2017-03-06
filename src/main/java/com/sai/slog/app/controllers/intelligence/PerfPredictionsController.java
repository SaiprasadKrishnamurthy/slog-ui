package com.sai.slog.app.controllers.intelligence;

import com.sai.slog.app.model.PerformanceLogEvent;
import com.sai.slog.app.service.LogService;
import lombok.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        System.out.println(headers);
        System.out.println(Arrays.deepToString(values));
    }
}
