package com.driva.server.model;

import lombok.Data;

@Data
public class Average {
    private final Double averageSize;
    private final Double averageFrequency;
    private final Double averageLastQueriedTime;
}
