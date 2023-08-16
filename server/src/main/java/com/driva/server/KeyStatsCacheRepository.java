package com.driva.server;


import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;
import static java.util.Collections.emptySet;

/**
 * Repository for Device Messages of type {@link KeyStats}.
 */
@Slf4j
@Repository
public class KeyStatsCacheRepository {

    private static final String METADATA_HASH = "metadata:";

    private final ValueOperations<String, Object> valueOperations;

    private final RedisTemplate redisTemplate;

    EvictionProperties evictionProperties;

    public KeyStatsCacheRepository(final RedisTemplate<String, Object> redisTemplate, EvictionProperties evictionProperties) {
        this.valueOperations = redisTemplate.opsForValue();
        this.redisTemplate = redisTemplate;
        this.evictionProperties = evictionProperties;
    }

    //TODO: Extract these elsewhere as they are not related to KeyStats Context.
    public String getMaxMemory() {
        return redisTemplate.getConnectionFactory().getConnection().serverCommands().info("memory").getProperty("maxmemory");
    }

    public String getUsedMemory() {
        return redisTemplate.getConnectionFactory().getConnection().serverCommands().info("memory").getProperty("used_memory");
    }

    public Double percentageOccupied() {
        return (double) ((parseLong(getUsedMemory())) * 100) / parseLong(getMaxMemory());
    }

    public String getMaxMemoryHuman() {
        return redisTemplate.getConnectionFactory().getConnection().serverCommands().info("memory").getProperty("maxmemory_human");
    }

    public String getUsedMemoryHuman() {
        return redisTemplate.getConnectionFactory().getConnection().serverCommands().info("memory").getProperty("used_memory_human");
    }

    public Long dbSize() {
        return redisTemplate.getConnectionFactory().getConnection().serverCommands().dbSize();
    }

    public Set<String> getKeys() {
        final Set<String> bucketKeys = valueOperations.getOperations().keys(toKey("*"));
        return bucketKeys == null ? emptySet() : bucketKeys.stream().map(KeyStatsCacheRepository::fromKey).collect(Collectors.toSet());
    }

    public List<KeyStats> getAllMetadata() {
        return (List<KeyStats>) (Object) valueOperations.multiGet(valueOperations.getOperations().keys(toKey("*")));
    }

    public void put(final KeyMetrics keyMetrics) {
        KeyStats retrievedKeyStats = (KeyStats) valueOperations.get(toKey(keyMetrics.getKey()));
        if (retrievedKeyStats != null) {
            retrievedKeyStats.setFrequency(retrievedKeyStats.getFrequency());
            retrievedKeyStats.setSize(keyMetrics.getSize());
            valueOperations.set(retrievedKeyStats.getKey(), retrievedKeyStats);
        } else {
            valueOperations.set(toKey(keyMetrics.getKey()), createKeyStats(keyMetrics));
        }
        log.info(String.format("Memory occupied [%s]", percentageOccupied()));
    }

    private static KeyStats createKeyStats(KeyMetrics keyMetrics) {
        return new KeyStats(toKey(keyMetrics.getKey()), 0L, keyMetrics.getSize(), System.currentTimeMillis(), 0L,  0L, 0L, true);
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
        KeyStats keyStats = (KeyStats) valueOperations.get(toKey(id));
        if(Objects.nonNull(keyStats)) {
            keyStats.setFrequency(keyStats.getFrequency() + 1);
            keyStats.setLastQueriedTime(System.currentTimeMillis());
            update(keyStats);
            KeyStats updatedStats = get(String.valueOf(id));
            log.info(String.format("KeyStats for [%s]: Size: [%s] and Frequency: [%s] and LastQueriedTime: [%s]",
                    id, updatedStats.getSize(), updatedStats.getFrequency(), updatedStats.getLastQueriedTime()));
            return updatedStats;
        }
        return null;
    }

    public void increment(final KeyStats keyStats) {
        keyStats.setFrequency(keyStats.getFrequency() + 1);
        update(keyStats);
        KeyStats updatedStats = getWithKey(keyStats.getKey());
        log.info(String.format("KeyStats for [%s]: Size: [%s] and Frequency: [%s]", fromKey(keyStats.getKey()), updatedStats.getSize(), updatedStats.getFrequency()));
    }

    public void delete(final String id) {
        valueOperations.getOperations().delete(toKey(id));
    }

    public void updateCacheMissTime(final String id, final Long cacheMissTime) {
        KeyStats keyStats = get(id);
        keyStats.setCacheMissDurationTime(cacheMissTime);
        keyStats.setCacheMissFrequency(keyStats.getCacheMissFrequency() + 1);
        update(keyStats);
    }

    public static String toKey(String id) {
        return METADATA_HASH + id;
    }

    public static String fromKey(final String value) {
        return value.split(METADATA_HASH)[1];
    }
}