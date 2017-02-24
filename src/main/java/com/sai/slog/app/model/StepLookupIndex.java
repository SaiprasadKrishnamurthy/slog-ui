package com.sai.slog.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by saipkri on 08/01/17.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StepLookupIndex {
    private String customerId;

    private String sessionId;

    private Long logTimestamp;

    private String functionality;

    private String logLevel;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StepLookupIndex that = (StepLookupIndex) o;

        if (!customerId.equals(that.customerId)) return false;
        if (!sessionId.equals(that.sessionId)) return false;
        return functionality.equals(that.functionality);

    }

    @Override
    public int hashCode() {
        int result = customerId.hashCode();
        result = 31 * result + sessionId.hashCode();
        result = 31 * result + functionality.hashCode();
        return result;
    }
}
