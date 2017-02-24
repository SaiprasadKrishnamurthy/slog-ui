package com.sai.slog.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by saipkri on 08/01/17.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Step {
    private String customerId;

    private String sessionId;

    private String functionality;

    private Long timestamp;

    private List<String> additionalDetails;
    private String sourceStep;
    private String destinationStep;
    private String action;
    private String logLevel;
    private String componentId;
    private String logFile;
    private String logEventId;
    private String isoTimestamp;


}
