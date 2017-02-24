package com.sai.slog.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by saipkri on 15/02/17.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlertConfig {

    private String customerId;

    private String name;

    private String keyword;
    private int timeWindowInDays = 10;
    private int leastCountThreshold = 5;
    private int lowCountThreshold = 20;
    private int mediumCountThreshold = 60;
    private int highCountThreshold = 90;
    private int highestCountThreshold = 100;
}
