package com.driva.server;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.driva.server.KeyStatsCacheRepository.fromKey;


@Slf4j
@Service
@RequiredArgsConstructor
public class EvictionScheduler {

    private final EvictionProperties evictionProperties;
    private final KeyStatsCacheRepository keyStatsCacheRepository;
    private final CachingService cachingService;

    @Scheduled(cron = "${redis.eviction.scheduler.cron.expression}")
    public void process() {
        log.info("========= Starting eviction task =========");
        // Phases: Early Mid Late Game
        weightCalculation();
        if (keyStatsCacheRepository.percentageOccupied() > evictionProperties.getMemoryUsageThreshold()) {
            log.info("Invoking eviction mechanism");
            Map<String, Double> weights = weightCalculation();

            List<Map.Entry<String, Double>> sortedWeights = weights.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toList());
            sortedWeights.stream()
                    .limit(evictionProperties.getTopEvictionCandidates())
                    .forEach(candidate -> {
                        log.info(String.format("Evicting key [%s]", fromKey(candidate.getKey())));
                        cachingService.delete(fromKey(candidate.getKey()));
                    });
        } else {
            log.info("No need to invoke eviction mechanism");
        }
        log.info("========= Finished eviction task =========");
    }


    private Map<String, Double> weightCalculation() {
        List<KeyStats> entries = keyStatsCacheRepository.getAllMetadata();
        Map<String, Double> keyWeightMappings = new TreeMap<>();
        entries.forEach( entry -> {
            if (entry.isActive()) {
                Double weight = evictionProperties.getFrequencyFactor() * entry.getFrequency() * entry.getSize() +
                        evictionProperties.getCacheMissFactor() * entry.getCacheMissDurationTime();
                keyWeightMappings.put(entry.getKey(), weight);
            }
        });
        return keyWeightMappings;
    }
}
