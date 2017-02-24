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
public class StepLookupByCustomerAndFunctionalityIndex {
    private String customerId;

    private String sessionId;

    private Long logTimestamp;

    private String functionality;

}
