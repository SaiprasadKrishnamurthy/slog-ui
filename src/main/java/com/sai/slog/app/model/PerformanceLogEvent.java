package com.sai.slog.app.model;

import lombok.Data;

import java.util.Map;

/**
 * Created by saipkri on 05/03/17.
 */
@Data
public class PerformanceLogEvent {
    private String functionality;

    private Long timestamp;

    private Map<String, Object> metricMap;
}
