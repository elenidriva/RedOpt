package com.driva.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class EvictionScheduler {

    private final EvictionProperties evictionProperties;
    private final KeyStatsCacheRepository keyStatsCacheRepository;

    @Scheduled(cron = "${redis.eviction.scheduler.cron.expression}")
    public void process() {
        log.info("========= Starting eviction task =========");
        // Phases: Early Mid Late Game
        //

        if (keyStatsCacheRepository.percentageOccupied() > evictionProperties.getMemoryUsageThreshold()) {
            log.info("Invoking eviction mechanism");
        } else {
            log.info("No need to invoke eviction mechanism");
        }

        log.info("========= Finished eviction task =========");
    }
}
