package com.driva.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.driva.server.CachingService.CACHE_MISS_MAPPINGS;
import static com.driva.server.KeyStatsCacheRepository.fromKey;


@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupScheduler {

    private final CleanupProperties cleanupProperties;
    private final EvictionProperties evictionProperties;
    private final KeyStatsCacheRepository keyStatsCacheRepository;

    @Scheduled(cron = "${redis.cleanup.scheduler.cron.expression}")
    public void process() {
        log.info("========= Starting cleanup task =========");
        CACHE_MISS_MAPPINGS.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > cleanupProperties.getWindow());
        log.info("Performed in memory cleanup");
        Map<String, Double> weights = new TreeMap<>();
        List<KeyStats> keyStats = keyStatsCacheRepository.getAllMetadata();
        if (keyStatsCacheRepository.percentageOccupied() > evictionProperties.getMemoryUsageThreshold()) {
            keyStats.forEach(
                    entry -> {
                        double weight;
                        if (!shouldProtect(entry)) {
                            if (entry.isActive()) {
                                weight = cleanupProperties.getActiveWeight() * entry.getFrequency();
                            } else {
                                weight = cleanupProperties.getInactiveWeight() * entry.getFrequency();
                            }
                            weights.put(entry.getKey(), weight);
                        }
                    });

            List<Map.Entry<String, Double>> sortedWeights = weights.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toList());
            sortedWeights.stream()
                    .limit(evictionProperties.getTopEvictionCandidates())
                    .forEach(candidate -> {
                        log.info(String.format("Evicting key from metadata [%s]", fromKey(candidate.getKey())));
                        // Periodic
                        keyStatsCacheRepository.delete(fromKey(candidate.getKey()));
                    });

        } else {
            log.info("No need to invoke metadata cleanup mechanism");
        }
        // introduce max capacity for metadata?
        log.info("========= Finished cleanup task =========");
    }


    private boolean shouldProtect(KeyStats keyStats) {
        return (System.currentTimeMillis() - keyStats.getInsertedTime()) < cleanupProperties.getFreshnessThreshold();
    }


//        AtomicReference<Double> atomicAverageFrequency = new AtomicReference<>(0.0);
//        keyStats.forEach( entry -> {
//            atomicAverageFrequency.updateAndGet(v -> v + entry.getFrequency());
//                });
//        Double avg = atomicAverageFrequency.get() / keyStats.size();
//        if (avg < 10
//        )

}
