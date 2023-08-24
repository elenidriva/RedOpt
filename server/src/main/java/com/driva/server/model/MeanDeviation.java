package com.driva.server.model;

import lombok.Data;

@Data
public class MeanDeviation {
    private final Double meanSizeDeviation;
    private final Double meanLastQueriedDeviation;
    private final Double meanFrequencyDeviation;
}
