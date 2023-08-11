package com.driva.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.driva.server.CachingService.CACHE_MISS_MAPPINGS;


@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupScheduler {

    private final CleanupProperties cleanupProperties;
    private final KeyStatsCacheRepository keyStatsCacheRepository;

    @Scheduled(cron = "${redis.cleanup.scheduler.cron.expression}")
    public void process() {
        log.info("========= Starting cleanup task =========");
        CACHE_MISS_MAPPINGS.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > cleanupProperties.getWindow());
        List<KeyStats> entries = keyStatsCacheRepository.getAllMetadata().stream().filter(
                entry -> {
                    if (!shouldProtect(entry)) {
                        if (entry.isActive()) {

                        } else {

                            
                        }
                    }
                }
        )
        // implement cleanup of metadata that are very old?
        // introduce max capacity for metadata?
        log.info("========= Finished cleanup task =========");
    }


    private boolean shouldProtect(KeyStats keyStats) {
        return (System.currentTimeMillis() - keyStats.getInsertedTime()) < cleanupProperties.getFreshnessThreshold();
    }

}
