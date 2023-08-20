package com.driva.server;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.driva.server.KeyStatsCacheRepository.toKey;

@Service
@RequiredArgsConstructor
public class CachingService {

    public static final Map<String, Long> CACHE_MISS_MAPPINGS = new HashMap<>() {
        {
            put("totalMisses", 0L);
        }
    };
    public static final Map<String, KeyStats> KEY_STATS_MAP = new HashMap<>();
    public static Long counter = 0L;
    private final CacheRepository cacheRepository;
    private final KeyStatsCacheRepository keyStatsCacheRepository;
    private final EvictionService evictionService;
    private final CleanupService cleanupService;
    private final ExecutorService queryExecutor = Executors.newCachedThreadPool();
    private final ExecutorService commandExecutor = Executors.newCachedThreadPool();

    public void putM(String id, Map<String, Object> object) {
        KeyMetrics keyMetrics = cacheRepository.put(id, object);
        commandExecutor.execute(() -> {
            KeyStats keyStats = KEY_STATS_MAP.get(toKey(id));
            if(keyStats != null) {
                keyStats.setFrequency(keyStats.getFrequency());
                keyStats.setSize(keyMetrics.getSize());
                KEY_STATS_MAP.put(keyStats.getKey(), keyStats);
            } else {
                KEY_STATS_MAP.put(toKey(id), createKeyStats(keyMetrics));
            }
            if (CACHE_MISS_MAPPINGS.containsKey(id)) {
                cacheMiss(id);
            }
            evictionService.evict();
            cleanupService.cleanupM();
        });

    }

    public Map<String, Object> getM(String id) {
        Map<String, Object> object = cacheRepository.get(id);
        queryExecutor.execute(() -> {
            KeyStats keyStats = KEY_STATS_MAP.get(toKey(id));
            if(Objects.nonNull(keyStats)) {
                keyStats.setFrequency(keyStats.getFrequency() + 1);
                keyStats.setLastQueriedTime(System.currentTimeMillis());
                KEY_STATS_MAP.put(keyStats.getKey(), keyStats);
            }
            long cacheMissTime = System.currentTimeMillis();
            if (Objects.isNull(object)) {
                CACHE_MISS_MAPPINGS.put(id, cacheMissTime);
            }
        });
        return object;
    }

    public void deleteM(String id) {
        cacheRepository.delete(id);
        commandExecutor.execute(() -> {
            KEY_STATS_MAP.remove(toKey(id));
        });
    }

    private static KeyStats createKeyStats(KeyMetrics keyMetrics) {
        return new KeyStats(toKey(keyMetrics.getKey()), 0L, keyMetrics.getSize(), System.currentTimeMillis(), 0L,  0L, 0L, true);
    }

    public void put(String id, Map<String, Object> object) {
        KeyMetrics keyMetrics = cacheRepository.put(id, object);
        commandExecutor.execute(() -> {
            keyStatsCacheRepository.put(keyMetrics);
            if (CACHE_MISS_MAPPINGS.containsKey(id)) {
                cacheMiss(id);
            }
            evictionService.evict();
            cleanupService.cleanup();

        });

    }

    public Map<String, Object> get(String id) {
        Map<String, Object> object = cacheRepository.get(id);
        queryExecutor.execute(() -> {
            keyStatsCacheRepository.increment(id);
            long cacheMissTime = System.currentTimeMillis();
            if (Objects.isNull(object)) {
                CACHE_MISS_MAPPINGS.put(id, cacheMissTime);
            }
        });
        return object;
    }

    public void delete(String id) {
        cacheRepository.delete(id);
        commandExecutor.execute(() -> {
            keyStatsCacheRepository.delete(id);
        });
    }

    private void cacheMiss(String id) {
        long now = System.currentTimeMillis();
        long cacheMissTime = now - CACHE_MISS_MAPPINGS.get(id);
        CACHE_MISS_MAPPINGS.remove(id);
        counter = counter + 1;
        keyStatsCacheRepository.updateCacheMissTime(id, cacheMissTime);
    }

    public MemoryStats getMemoryStats() {
        return new MemoryStats(keyStatsCacheRepository.getMaxMemory(), keyStatsCacheRepository.getUsedMemory(), keyStatsCacheRepository.getMaxMemoryHuman(), keyStatsCacheRepository.getUsedMemoryHuman(), keyStatsCacheRepository.percentageOccupied(), keyStatsCacheRepository.dbSize());
    }

    public KeyStats getKeyStats(String id) {
        return keyStatsCacheRepository.getWithKey(id);
    }
}
