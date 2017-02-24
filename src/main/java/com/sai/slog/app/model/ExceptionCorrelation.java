package com.sai.slog.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Created by saipkri on 23/02/17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExceptionCorrelation {
    private String file;
    private String correlationRuleName;
    private List<Log> logs;
}
