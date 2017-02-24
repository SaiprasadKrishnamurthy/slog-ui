package com.sai.slog.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;

/**
 * Created by saipkri on 07/01/17.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Subscriber {
    private static final SimpleDateFormat FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final SimpleDateFormat FMT_MONTH = new SimpleDateFormat("yyyy-MM-dd");
    private String email;
    private String subscribedOn;
    private int deliveredCount;
    private boolean bounced;
    private boolean blocked;

    public String subscribedOnDayOnly() {
        try {
            return FMT_MONTH.format(FMT.parse(subscribedOn));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
