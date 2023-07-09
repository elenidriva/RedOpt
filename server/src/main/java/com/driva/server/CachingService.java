package com.driva.server;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CachingService {

    private final CacheRepository cacheRepository;
    private final KeyStatsCacheRepository keyStatsCacheRepository;


    public void put(String id, Map<String, Object> object) {
       KeyMetrics keyMetrics =  cacheRepository.put(id, object);
       keyStatsCacheRepository.put(keyMetrics);
    }

    public Map<String, Object> get(String id) {
        Map<String, Object> object = cacheRepository.get(id);
        if (object != null) {
            keyStatsCacheRepository.increment(id);
        }
        return object;
    }

    public void delete(String id) {
        cacheRepository.delete(id);
        keyStatsCacheRepository.delete(id);
    }
}
