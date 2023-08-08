package com.driva.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemoryStats {

    private String memorySize;
    private String memoryUsed;
    private String memorySizeHuman;
    private String memoryUsedHuman;
    private Double memoryOccupiedPercentage;
    private Long dbSize;

}
