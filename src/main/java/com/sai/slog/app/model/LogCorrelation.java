package com.sai.slog.app.model;

import lombok.Data;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by saipkri on 21/02/17.
 */
@Data
@ToString(of = {"description"})
public class LogCorrelation {

    private Long graphId;

    private String logMessage;

    private String component;

    private String description;

    private boolean disabled;

    private Set<LogCorrelation> directlyCausedBy = new HashSet<>();

    private Set<LogCorrelation> potentiallyCausedBy = new HashSet<>();

    public void addDirectCause(final LogCorrelation logCorrelation) {
        this.directlyCausedBy.add(logCorrelation);
    }

    public void addPotentialCause(final LogCorrelation logCorrelation) {
        this.potentiallyCausedBy.add(logCorrelation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogCorrelation that = (LogCorrelation) o;

        return description.equals(that.description);

    }

    @Override
    public int hashCode() {
        return description.hashCode();
    }
}
