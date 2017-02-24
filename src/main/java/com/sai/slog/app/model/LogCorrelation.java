package com.sai.slog.app.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by saipkri on 21/02/17.
 */
@Data
@EqualsAndHashCode(exclude = {"directlyCausedBy", "potentiallyCausedBy"})
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
}
