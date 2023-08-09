package com.driva.server;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CachingService {

    public static final Map<String, Long> CACHE_MISS_MAPPINGS = new HashMap<>();

    private final CacheRepository cacheRepository;
    private final KeyStatsCacheRepository keyStatsCacheRepository;


    public void put(String id, Map<String, Object> object) {
        KeyMetrics keyMetrics = cacheRepository.put(id, object);
        keyStatsCacheRepository.put(keyMetrics);
        if (CACHE_MISS_MAPPINGS.containsKey(id)) {
            long now = System.currentTimeMillis();
            long cacheMissTime = now - CACHE_MISS_MAPPINGS.get(id);
            CACHE_MISS_MAPPINGS.remove(id);
            keyStatsCacheRepository.updateCacheMissTime(id, cacheMissTime);
        }

    }

    public Map<String, Object> get(String id) {
        Map<String, Object> object = cacheRepository.get(id);
        keyStatsCacheRepository.increment(id);
        if (Objects.isNull(object)) {
            CACHE_MISS_MAPPINGS.put(id, System.currentTimeMillis());
        }
        return object;
    }

    public void delete(String id) {
        cacheRepository.delete(id);
        keyStatsCacheRepository.delete(id);
    }

    public MemoryStats getMemoryStats() {
        return new MemoryStats(keyStatsCacheRepository.getMaxMemory(), keyStatsCacheRepository.getUsedMemory(), keyStatsCacheRepository.getMaxMemoryHuman(), keyStatsCacheRepository.getUsedMemoryHuman(), keyStatsCacheRepository.percentageOccupied(), keyStatsCacheRepository.dbSize());
    }

    public KeyStats getKeyStats(String id) {
        return keyStatsCacheRepository.getWithKey(id);
    }
}
