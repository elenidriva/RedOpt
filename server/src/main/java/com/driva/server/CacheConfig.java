package com.driva.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Cache Configuration. Uses lettuce as default cache provider.
 */
@Configuration
@EnableRedisRepositories
public class CacheConfig {

    @Bean
    public JdkSerializationRedisSerializer jdkSerializationRedisSerializer(ResourceLoader resourceLoader) {
        return new JdkSerializationRedisSerializer(resourceLoader.getClassLoader());
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                                                       JdkSerializationRedisSerializer jdkSerializationRedisSerializer) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jdkSerializationRedisSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        RedisConnection redisCon = connectionFactory.getConnection();
        redisCon.setConfig("maxmemory-policy", "allkeys-lfu");
        redisTemplate.setHashValueSerializer(jdkSerializationRedisSerializer);
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }
}