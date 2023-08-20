package com.driva.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.driva.server.CachingService.KEY_STATS_MAP;
import static com.driva.server.KeyStatsCacheRepository.fromKey;


@Slf4j
@Service
@RequiredArgsConstructor
public class EvictionService {

    private final EvictionProperties evictionProperties;
    private final KeyStatsCacheRepository keyStatsCacheRepository;
    private final CacheRepository cacheRepository;

    // Scenarios:
    // 1. Max Cache capacity
    // 2. Random Eviction
    // 3. All Eviction


    public void evictM() {
        //  log.info("========= Initiating eviction service =========");
        // Phases: Early Mid Late Game
        Double memoryOccupied = keyStatsCacheRepository.percentageOccupied();
        Double evictionThreshold = evictionProperties.getMemoryUsageThreshold();
        if (memoryOccupied > evictionThreshold) {
            log.info("Invoking eviction mechanism");
            Map<String, Double> weights = weightCalculationM();

            List<Map.Entry<String, Double>> sortedWeights = weights.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toList());
            sortedWeights.stream()
                    .limit(memoryOccupied > evictionThreshold + 5 ? 3 : 1)
                    .forEach(candidate -> {
                        log.info(String.format("Evicting key from data [%s]", fromKey(candidate.getKey())));
                        cacheRepository.delete(fromKey(candidate.getKey()));
                        KeyStats keyStats = KEY_STATS_MAP.get(candidate.getKey());
                        keyStats.setActive(false);
                        KEY_STATS_MAP.put(candidate.getKey(), keyStats);
                    });
        } else {
            //     log.info("No need to invoke eviction mechanism");
        }
        //  log.info("========= Finished eviction service =========");
    }


    public void evict() {
      //  log.info("========= Initiating eviction service =========");
        // Phases: Early Mid Late Game
        Double memoryOccupied = keyStatsCacheRepository.percentageOccupied();
        Double evictionThreshold = evictionProperties.getMemoryUsageThreshold();
        if (memoryOccupied > evictionThreshold) {
            log.info("Invoking eviction mechanism");
            Map<String, Double> weights = weightCalculation();

            List<Map.Entry<String, Double>> sortedWeights = weights.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toList());
            sortedWeights.stream()
                    .limit(memoryOccupied > evictionThreshold + 5 ? 3 : 1)
                    .forEach(candidate -> {
                        log.info(String.format("Evicting key from data [%s]", fromKey(candidate.getKey())));
                        cacheRepository.delete(fromKey(candidate.getKey()));
                        KeyStats keyStats = keyStatsCacheRepository.get(fromKey(candidate.getKey()));
                        keyStats.setActive(false);
                        keyStatsCacheRepository.update(keyStats);
                    });
        } else {
       //     log.info("No need to invoke eviction mechanism");
        }
      //  log.info("========= Finished eviction service =========");
    }

    // lowest weights -> eviction candidates

    private Map<String, Double> weightCalculationM() {
        Map<String, Double> keyWeightMappings = new TreeMap<>();
        KEY_STATS_MAP.forEach((key, value) -> {
            if (value.isActive() && !shouldProtect(value)) {
                if (value.isActive()) {
                    Double weight = evictionProperties.getFrequencyFactor() * value.getFrequency() * value.getSize() +
                            evictionProperties.getCacheMissFactor() * value.getCacheMissDurationTime();
                    keyWeightMappings.put(key, weight);
                }
            }
        });
        return keyWeightMappings;
    }

    private Map<String, Double> weightCalculation() {
        List<KeyStats> entries = keyStatsCacheRepository.getAllMetadata();
        Map<String, Double> keyWeightMappings = new TreeMap<>();
        entries.forEach( entry -> {
        if (entry.isActive() && !shouldProtect(entry)) {
            if (entry.isActive()) {
                Double weight = evictionProperties.getFrequencyFactor() * entry.getFrequency() * entry.getSize() +
                        evictionProperties.getCacheMissFactor() * entry.getCacheMissDurationTime();
                keyWeightMappings.put(entry.getKey(), weight);
            }
        }
        });
        return keyWeightMappings;
    }

    // Fresh entries should be protected from eviction for the specified time interval
    private boolean shouldProtect(KeyStats keyStats) {
        return (System.currentTimeMillis() - keyStats.getInsertedTime()) < evictionProperties.getFreshnessThreshold();
    }
}
