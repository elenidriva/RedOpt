package com.example.opt.server;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

/**
 * Repository for Device Messages of type {@link KeyStats}.
 */
@Slf4j
@Repository
public class KeyStatsCacheRepository {

    private static final String METADATA_HASH = "metadata:";

    private final ValueOperations<String, Object> valueOperations;

    public KeyStatsCacheRepository(final RedisTemplate<String, Object> redisTemplate) {
        this.valueOperations = redisTemplate.opsForValue();
    }

    public Set<String> getKeys() {
        final Set<String> bucketKeys = valueOperations.getOperations().keys(toKey("*"));
        return bucketKeys == null ? emptySet() : bucketKeys.stream().map(KeyStatsCacheRepository::fromKey).collect(Collectors.toSet());
    }

    public void put(final KeyStats keyStats) {
        KeyStats retrievedKeyStats = (KeyStats) valueOperations.get(keyStats.getKey());
            keyStats.setQueryFrequency(retrievedKeyStats.getQueryFrequency());
            valueOperations.set(keyStats.getKey(), keyStats);
    }

    public void update(final KeyStats keyStats) {
        valueOperations.set(keyStats.getKey(), keyStats);
    }


    public KeyStats get(final String id) {
        return (KeyStats) valueOperations.get(toKey(id));
    }

    public KeyStats getWithKey(final String id) {
        return (KeyStats) valueOperations.get(toKey(id));
    }


    public KeyStats increment(final String id) {
        return (KeyStats) valueOperations.get(toKey(id));
    }

    public void increment(final KeyStats keyStats) {
        keyStats.setQueryFrequency(keyStats.getQueryFrequency() + 1);
        update(keyStats);
        KeyStats updatedStats = getWithKey(keyStats.getKey());
        log.info(String.format("KeyStats for [%s]: Size: [%s] and Frequency: [%s]", fromKey(keyStats.getKey()), updatedStats.getObjectSize(), updatedStats.getQueryFrequency()));
    }

    public void increment(final Long id) {
        KeyStats keyStats = (KeyStats) valueOperations.get(toKey(id));
        keyStats.setQueryFrequency(keyStats.getQueryFrequency() + 1);
        update(keyStats);
        KeyStats updatedStats = get(String.valueOf(id));
        log.info(String.format("KeyStats for [%s]: Size: [%s] and Frequency: [%s]", id, updatedStats.getObjectSize(), updatedStats.getQueryFrequency()));
        }

    public void delete(final String id) {
        valueOperations.getOperations().delete(toKey(id));
    }

    public void delete(final Long id) {
        valueOperations.getOperations().delete(toKey(id));
    }


    public static String toKey(String id) {
        return METADATA_HASH + id;
    }

    public static String toKey(Long id) {
        return METADATA_HASH + id;
    }


    private static String fromKey(final String value) {
        return value.split(METADATA_HASH)[1];
    }
}