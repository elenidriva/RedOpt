package com.driva.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("redis.eviction.scheduler")
public class EvictionSchedulerProperties {

    private Double memoryUsageThreshold = 80.00;
    private Long freshnessThreshold = 600000L;

}
