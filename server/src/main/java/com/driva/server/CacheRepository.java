package com.driva.server;


import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

/**
 * Repository for Device Messages of type {@link KeyStats}.
 */
@Repository
public class CacheRepository {

    private static final String DATA_HASH = "data:";

    private final ValueOperations<String, Object> valueOperations;
    private final RedisTemplate redisTemplate;

    private final ExecutorService queryExecutor = Executors.newCachedThreadPool();

    public CacheRepository(final RedisTemplate<String, Object> redisTemplate) {
        this.valueOperations = redisTemplate.opsForValue();
        this.redisTemplate = redisTemplate;
    }


    public KeyMetrics put(final String id, final Map<String, Object> object) {
        valueOperations.set(toKey(id), object);
        return new KeyMetrics(id, valueOperations.size(toKey(id)));
    }

    public void putM(final String id, final Map<String, Object> object) {
        valueOperations.set(toKey(id), object);
    }

    public Map<String, Object> get(final String id) {
        return (Map<String, Object>) valueOperations.get(toKey(id));
    }

    public void delete(final String id) {
        valueOperations.getOperations().delete(toKey(id));
    }

    public Long getSize(final String id) {
        return valueOperations.size(toKey(id));
    }


    public Set<String> getKeys() {
        final Set<String> bucketKeys = valueOperations.getOperations().keys(toKey("*"));
        return bucketKeys == null ? emptySet() : bucketKeys.stream().map(CacheRepository::fromKey).collect(Collectors.toSet());
    }

    private static String toKey(String id) {
        return DATA_HASH + id;
    }

    private static String fromKey(final String value) {
        return value.split(DATA_HASH)[1];
    }

}