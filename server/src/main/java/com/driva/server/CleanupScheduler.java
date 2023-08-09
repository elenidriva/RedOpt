package com.driva.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static com.driva.server.CachingService.CACHE_MISS_MAPPINGS;


@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupScheduler {

    private final CleanupProperties cleanupProperties;

    @Scheduled(cron = "${redis.cleanup.scheduler.cron.expression}")
    public void process() {
        log.info("========= Starting cleanup task =========");
        CACHE_MISS_MAPPINGS.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > cleanupProperties.getWindow());
        // implement cleanup of metadata that are very old?
        // introduce max capacity for metadata?
        log.info("========= Finished cleanup task =========");
    }
}
