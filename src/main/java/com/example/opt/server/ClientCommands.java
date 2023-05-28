package com.example.opt.server;

import io.lettuce.core.dynamic.Commands;
import org.springframework.data.redis.connection.ReactiveRedisConnection;

public interface ClientCommands extends Commands {
    int getCacheSize();
}
