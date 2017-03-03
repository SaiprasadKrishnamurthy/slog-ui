package com.sai.slog.app.model;

import lombok.Data;

import java.util.List;

/**
 * Created by saipkri on 03/02/17.
 */
@Data
public class Log {
    private String id;
    private String path;
    private String customerId;
    private String message;
    private String type;
    private List<String> tags;
    private String timestamp;
    private String logTimestamp;
    private String error;
    private String __total__;

    public String getError() {
        if (message != null && message.contains("ERROR")) {
            return "error";
        } else {
            return "";
        }
    }

    public String getLogTimestamp() {
        return logTimestamp.replace("T", " ");
    }
}
