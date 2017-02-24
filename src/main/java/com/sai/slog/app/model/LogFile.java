package com.sai.slog.app.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by saipkri on 13/02/17.
 */
@Data
public class LogFile {
    private String fileName;
    private int startRecord;
    private int size;
    private List<Log> logs = new ArrayList<>();
}
