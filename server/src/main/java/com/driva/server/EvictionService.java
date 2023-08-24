package com.driva.server;

import com.driva.server.model.Average;
import com.driva.server.model.MathematicalMetrics;
import com.driva.server.model.MeanDeviation;
import com.driva.server.model.StandardDeviation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.driva.server.CachingService.KEY_STATS_MAP;
import static com.driva.server.KeyStatsCacheRepository.fromKey;
import static java.lang.Long.parseLong;


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
        Long memoryOccupied = parseLong(keyStatsCacheRepository.getUsedMemory());
        Long evictionThreshold = evictionProperties.getMemoryUsageThreshold();
        if (memoryOccupied > evictionThreshold) {
            // log.info("Invoking eviction mechanism");
            Map<String, Double> weights = weightCalculationM();

            List<Map.Entry<String, Double>> sortedWeights = weights.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toList());
            sortedWeights.stream()
                    .limit(memoryOccupied > evictionThreshold + 0.6 ? evictionProperties.getTopEvictionCandidates() : 1)
                    .forEach(candidate -> {
                        //  log.info(String.format("Evicting key from data [%s]", fromKey(candidate.getKey())));
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

    public void evictMRefactored() {
        //  log.info("========= Initiating eviction service =========");
        // Phases: Early Mid Late Game
        Long memoryOccupied = parseLong(keyStatsCacheRepository.getUsedMemory());
        Long evictionThreshold = evictionProperties.getMemoryUsageThreshold();
        Map<String, Double> weights = weightCalculationM();
        List<Map.Entry<String, Double>> sortedWeights = weights.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toList());
        log.info("Making space for newly added k");
        while (parseLong(keyStatsCacheRepository.getUsedMemory()) > evictionThreshold) {
            String candidateKey = sortedWeights.get(0).getKey();
            log.info(String.format("Erasing [%s]", candidateKey));
            cacheRepository.delete(fromKey(candidateKey));
            KeyStats keyStats = KEY_STATS_MAP.get(candidateKey);
            keyStats.setActive(false);
            KEY_STATS_MAP.put(candidateKey, keyStats);
            sortedWeights.remove(0);
        }

        //  log.info("========= Finished eviction service =========");
    }


//    public void evict() {
//        //  log.info("========= Initiating eviction service =========");
//        // Phases: Early Mid Late Game
//        Double memoryOccupied = keyStatsCacheRepository.percentageOccupied();
//        Double evictionThreshold = evictionProperties.getMemoryUsageThreshold();
//        if (memoryOccupied > evictionThreshold) {
//            log.info("Invoking eviction mechanism");
//            Map<String, Double> weights = weightCalculation();
//
//            List<Map.Entry<String, Double>> sortedWeights = weights.entrySet()
//                    .stream()
//                    .sorted(Map.Entry.comparingByValue())
//                    .collect(Collectors.toList());
//            sortedWeights.stream()
//                    .limit(memoryOccupied > evictionThreshold + 5 ? 3 : 1)
//                    .forEach(candidate -> {
//                        log.info(String.format("Evicting key from data [%s]", fromKey(candidate.getKey())));
//                        cacheRepository.delete(fromKey(candidate.getKey()));
//                        KeyStats keyStats = keyStatsCacheRepository.get(fromKey(candidate.getKey()));
//                        keyStats.setActive(false);
//                        keyStatsCacheRepository.update(keyStats);
//                    });
//        } else {
//            //     log.info("No need to invoke eviction mechanism");
//        }
//        //  log.info("========= Finished eviction service =========");
//    }

    // lowest weights -> eviction candidates

    private Map<String, Double> weightCalculationM() {
        Map<String, Double> keyWeightMappings = new TreeMap<>();
        Average average = calculateMathematicalMetrics();
        KEY_STATS_MAP.forEach((key, value) -> {
            if (value.isActive() && !shouldProtect(value)) {
                Double weight = evictionProperties.getFrequencyFactor() * value.getFrequency() * average.getAverageSize()
                        + evictionProperties.getSizeFactor() * value.getSize()
                        + evictionProperties.getCacheMissFactor() * value.getCacheMissDurationTime();
                       // + evictionProperties.getLastQueriedFactor() * lastQueriedTime(average.getAverageLastQueriedTime(), value.getLastQueriedTime());
//                Double weight = evictionProperties.getFrequencyFactor() * calculateFrequency(average.getAverageFrequency(), value.getFrequency())
//                        + evictionProperties.getSizeFactor() * calculateSizeWeight(average.getAverageSize(), value.getSize())
//                        + evictionProperties.getLastQueriedFactor() * lastQueriedTime(average.getAverageLastQueriedTime(), value.getLastQueriedTime());
                //   + evictionProperties.getCacheMissFactor() * value.getCacheMissDurationTime();
                keyWeightMappings.put(key, weight);
            }
        });
        return keyWeightMappings;
    }

    private static Average calculateMathematicalMetrics() {
        AtomicReference<Long> averageSizeAtomic = new AtomicReference<>(0L);
        AtomicReference<Long> averageTimeMillisQueriedAtomic = new AtomicReference<>(0L);
        AtomicReference<Long> averageFrequencyAtomic = new AtomicReference<>(0L);
        AtomicReference<Long> count = new AtomicReference<>(0L);
        KEY_STATS_MAP.forEach((key, value) -> {
            if (value.isActive()) {
                averageSizeAtomic.updateAndGet(v -> v + value.getSize());
                averageTimeMillisQueriedAtomic.updateAndGet(v -> v + value.getLastQueriedTime());
                averageFrequencyAtomic.updateAndGet(v -> v + value.getFrequency());
                count.getAndSet(count.get() + 1);
            }
        });
        Double averageObjectSize = (double) averageSizeAtomic.get() / count.get();
        Double averageLastQueriedTime = (double) averageTimeMillisQueriedAtomic.get() / count.get();
        Double averageFrequency = (double) averageFrequencyAtomic.get() / count.get();

        Average average = new Average(averageObjectSize, averageFrequency, averageLastQueriedTime);
//        StandardDeviation standardDeviation = calculateDeviation(average, count.get());
//        MeanDeviation meanDeviation = calculateMeanDeviation(average, count.get());
//
//        Double lowerCommonDeviation = average.getAverageSize() - meanDeviation.getMeanSizeDeviation();
//        Double higherCommonDeviation = average.getAverageSize() + meanDeviation.getMeanSizeDeviation();
//        Double bucketSize = meanDeviation.getMeanSizeDeviation() / 4;
//        Double bucket1Right = lowerCommonDeviation + bucketSize;

        return average;
    }

//    private static MeanDeviation calculateMeanDeviation(Average average, Long count) {
//        AtomicReference<Double> standardSizeDeviation = new AtomicReference<>(0.0);
//        AtomicReference<Double> standardLastQueriedDeviation = new AtomicReference<>(0.0);
//        AtomicReference<Double> standardFrequencyDeviation = new AtomicReference<>(0.0);
//        KEY_STATS_MAP.forEach((key, value) -> {
//            standardSizeDeviation.updateAndGet(v -> v - average.getAverageSize());
//            standardLastQueriedDeviation.updateAndGet(v -> v - average.getAverageLastQueriedTime());
//            standardFrequencyDeviation.updateAndGet(v -> v - average.getAverageFrequency());
//        });
//        return new MeanDeviation(
//                standardSizeDeviation.get() / count,
//                standardLastQueriedDeviation.get() / count,
//                standardFrequencyDeviation.get() / count);
//    }
//
//    private static StandardDeviation calculateDeviation(Average average, Long count) {
//        AtomicReference<Double> standardSizeDeviation = new AtomicReference<>(0.0);
//        AtomicReference<Double> standardLastQueriedDeviation = new AtomicReference<>(0.0);
//        AtomicReference<Double> standardFrequencyDeviation = new AtomicReference<>(0.0);
//        KEY_STATS_MAP.forEach((key, value) -> {
//            standardSizeDeviation.updateAndGet(v -> v + Math.pow(value.getSize() - average.getAverageSize(), 2));
//            standardLastQueriedDeviation.updateAndGet(v -> v + Math.pow(value.getLastQueriedTime() - average.getAverageLastQueriedTime(), 2));
//            standardFrequencyDeviation.updateAndGet(v -> v + Math.pow(value.getFrequency() - average.getAverageFrequency(), 2));
//        });
//        return new StandardDeviation(
//                Math.sqrt(standardSizeDeviation.get() / count),
//                Math.sqrt(standardLastQueriedDeviation.get() / count),
//                Math.sqrt(standardFrequencyDeviation.get() / count));
//    }


    private Double calculateFrequency(Double averageFrequency, Long frequency) {
        if (2 * frequency < averageFrequency) {
            return 0.1;
        } else if (frequency < averageFrequency) {
            return 0.3;
        } else if (frequency > 2 * averageFrequency) {
            return 0.7;
        } else {
            return 0.5;
        }
    }

    private Double calculateSizeWeight(Double averageObjectSize, Long entrySize) {
        if (2 * entrySize < averageObjectSize) {
            return 0.1;
        } else if (entrySize < averageObjectSize) {
            return 0.3;
        } else if (entrySize > 2 * averageObjectSize) {
            return 0.7;
        } else {
            return 0.5;
        }
    }

    private Double lastQueriedTime(Double avgLastQueriedTime, Long lastQueriedTime) {
        long timeElapsedSinceLastQueried = System.currentTimeMillis() - lastQueriedTime;
        if (2 * timeElapsedSinceLastQueried < avgLastQueriedTime) {
            return 0.7;
        } else if (timeElapsedSinceLastQueried < avgLastQueriedTime) {
            return 0.5;
        } else if (timeElapsedSinceLastQueried > 2 * avgLastQueriedTime) {
            return 0.1;
        } else {
            return 0.3;
        }
    }


//    private Map<String, Double> weightCalculation() {
//        List<KeyStats> entries = keyStatsCacheRepository.getAllMetadata();
//        Map<String, Double> keyWeightMappings = new TreeMap<>();
//        entries.forEach(entry -> {
//            if (entry.isActive() && !shouldProtect(entry)) {
//                if (entry.isActive()) {
//                    Double weight = evictionProperties.getFrequencyFactor() * entry.getFrequency() * entry.getSize() +
//                            evictionProperties.getCacheMissFactor() * entry.getCacheMissDurationTime();
//                    keyWeightMappings.put(entry.getKey(), weight);
//                }
//            }
//        });
//        return keyWeightMappings;
//    }

    // Fresh entries should be protected from eviction for the specified time interval
    private boolean shouldProtect(KeyStats keyStats) {
        return (System.currentTimeMillis() - keyStats.getInsertedTime()) < evictionProperties.getFreshnessThreshold();
    }
}
