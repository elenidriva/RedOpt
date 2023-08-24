package com.driva.server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.driva.server.CachingService.CACHE_MISS_MAPPINGS;
import static com.driva.server.CachingService.KEY_STATS_MAP;
import static com.driva.server.KeyStatsCacheRepository.fromKey;


@Slf4j
@Service
@RequiredArgsConstructor
public class CleanupService {

    private final CleanupProperties cleanupProperties;
    private final EvictionProperties evictionProperties;
    private final KeyStatsCacheRepository keyStatsCacheRepository;


    public void cleanupM() {
        //   log.info("========= Initiating cleanup service =========");
        CACHE_MISS_MAPPINGS.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > cleanupProperties.getWindow());
//        log.info("Performed in memory cleanup");
//        Map<String, Double> weights = new TreeMap<>();
//        Map<String, KeyStats> keyStats = KEY_STATS_MAP;
//        if (keyStatsCacheRepository.percentageOccupied() > evictionProperties.getMemoryUsageThreshold()) {
//            keyStats.forEach((key, value) -> {
//                if (!value.isActive()) {
//                    double weight = cleanupProperties.getInactiveWeight() * value.getFrequency()
//                            + lastQueriedTime(value.getLastQueriedTime());
//                    weights.put(key, weight);
//                }
//            });
//
//            List<Map.Entry<String, Double>> sortedWeights = weights.entrySet()
//                    .stream()
//                    .sorted(Map.Entry.comparingByValue())
//                    .collect(Collectors.toList());
//            sortedWeights.stream()
//                    .limit(evictionProperties.getTopEvictionCandidates())
//                    .forEach(candidate -> {
//                        log.info(String.format("Evicting key from metadata [%s]", fromKey(candidate.getKey())));
//                        // Periodic
//                        KEY_STATS_MAP.remove(candidate.getKey());
//                    });
//
//        } else {
//            //  log.info("No need to invoke metadata cleanup mechanism");
//        }
        // introduce max capacity for metadata?
        // log.info("========= Finished cleanup service =========");
    }


    public void cleanup() {
     //   log.info("========= Initiating cleanup service =========");
        CACHE_MISS_MAPPINGS.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue() > cleanupProperties.getWindow());
        log.info("Performed in memory cleanup");
        Map<String, Double> weights = new TreeMap<>();
        List<KeyStats> keyStats = keyStatsCacheRepository.getAllMetadata();
        if (keyStatsCacheRepository.percentageOccupied() > evictionProperties.getMemoryUsageThreshold()) {
            keyStats.forEach(
                    entry -> {
                            if (!entry.isActive()) {
                               double weight = cleanupProperties.getInactiveWeight() * entry.getFrequency()
                                        + lastQueriedTime(entry.getLastQueriedTime());
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
          //  log.info("No need to invoke metadata cleanup mechanism");
        }
        // introduce max capacity for metadata?
       // log.info("========= Finished cleanup service =========");
    }


        private Double lastQueriedTime(Long lastQueriedTime) {
        long timeElapsedSinceLastQueried = System.currentTimeMillis() - lastQueriedTime;
         if (timeElapsedSinceLastQueried <= 3 * 60000) {
             return 0.5;
         } else if(timeElapsedSinceLastQueried <= 5 * 60000) {
             return 0.3;
         } else if(timeElapsedSinceLastQueried <= 10 * 60000) {
            return 0.2;
         } else {
             return 0.1;
         }
    }

//    private boolean shouldProtect(KeyStats keyStats) {
//        return (System.currentTimeMillis() - keyStats.getInsertedTime()) < cleanupProperties.getFreshnessThreshold();
//    }


//        AtomicReference<Double> atomicAverageFrequency = new AtomicReference<>(0.0);
//        keyStats.forEach( entry -> {
//            atomicAverageFrequency.updateAndGet(v -> v + entry.getFrequency());
//                });
//        Double avg = atomicAverageFrequency.get() / keyStats.size();
//        if (avg < 10
//        )

}
