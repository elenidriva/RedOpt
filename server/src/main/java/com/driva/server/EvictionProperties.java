package com.driva.server;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties("redis.eviction")
public class EvictionProperties {

    private Long memoryUsageThreshold = 2097152L;
    private Long freshnessThreshold = 600000L;
    private Long topEvictionCandidates = 2L;

    @Value("${redis.eviction.factors.cacheMiss}")
    private Double cacheMissFactor;

    @Value("${redis.eviction.factors.frequency}")
    private Double frequencyFactor;

    @Value("${redis.eviction.factors.size}")
    private Double sizeFactor;

    @Value("${redis.eviction.factors.lastQueried}")
    private Double lastQueriedFactor;
//    private Double cacheMissWeight = 50.0;
//    private Double objectSizeWeight = 50.0;
}
