package com.example.opt.server;


import com.example.opt.client.Student;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;

/**
 * Repository for Device Messages of type {@link KeyStats}.
 */
@Repository
public class StudentCacheRepository {

    private static final String DATA_HASH = "data:";

    private final ValueOperations<String, Object> valueOperations;
    private final RedisTemplate redisTemplate;

    public StudentCacheRepository(final RedisTemplate<String, Object> redisTemplate) {
        this.valueOperations = redisTemplate.opsForValue();
        this.redisTemplate = redisTemplate;
    }

    public Set<String> getKeys() {
        final Set<String> bucketKeys = valueOperations.getOperations().keys(toKey(Long.valueOf("*")));
        return bucketKeys == null ? emptySet() : bucketKeys.stream().map(StudentCacheRepository::fromKey).collect(Collectors.toSet());
    }

    public KeyStats put(final Student student) {
        valueOperations.set(toKey(student.getId()), student);
        // Size of DB:
        // redisTemplate.getConnectionFactory().getConnection().serverCommands().dbSize();
        // Memory properties:
        // Properties properties = redisTemplate.getConnectionFactory().getConnection().serverCommands().info("memory").get()

       return new KeyStats(KeyStatsCacheRepository.toKey(student.getId()), 0L, valueOperations.size(toKey(student.getId())));
    }

    public Student get(final String id) {
        return (Student) valueOperations.get(toKey(Long.valueOf(id)));
    }

    public Student get(final Long id) {
        return (Student) valueOperations.get(toKey(id));
    }

    public void delete(final String id) {
        valueOperations.getOperations().delete(toKey(Long.valueOf(id)));
    }

    public void delete(final Long id) {
        valueOperations.getOperations().delete(toKey(id));
    }

    private static String toKey(Long id) {
        return DATA_HASH + id;
    }

    private static String fromKey(final String value) {
        return value.split(DATA_HASH)[1];
    }
}