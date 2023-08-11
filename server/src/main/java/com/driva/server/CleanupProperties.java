package com.driva.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("redis.cleanup")
public class CleanupProperties {

    private int window = 5 * 6000;
    private Long freshnessThreshold = 600000L;

    private String cronExpression = "* /2 * * * ?";
}
