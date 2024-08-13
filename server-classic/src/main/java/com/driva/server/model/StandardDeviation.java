package com.driva.server.model;

import lombok.Data;

@Data
public class StandardDeviation {
    private final Double standardSizeDeviation;
    private final Double standardLastQueriedDeviation;
    private final Double standardFrequencyDeviation;
}
