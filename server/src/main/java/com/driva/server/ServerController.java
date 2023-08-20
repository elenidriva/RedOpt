package com.driva.server;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ServerController {

    private final CachingService cachingService;

    @Timed(value = "RedisOptisServer - getStudent")
    @GetMapping("/cache/get/{id}")
    public Map<String, Object> get(@PathVariable String id) {
        return cachingService.getM(id);
    }

    @Timed(value = "RedisOptisServer - putStudent")
    @PostMapping("/cache/put/{id}")
    public void put(@PathVariable String id, @RequestBody Map<String, Object> object) {
        cachingService.putM(id, object);
    }

    @Timed(value = "RedisOptisServer - deleteStudent")
    @DeleteMapping("/cache/delete/{id}")
    public void delete(@PathVariable String id) {
        cachingService.deleteM(id);
    }

    @GetMapping("/cache/memory-stats")
    public MemoryStats getMemoryStats() {
       return cachingService.getMemoryStats();
    }

    @GetMapping("/cache/key-stats/{id}")
    public KeyStats getKeyStats(@PathVariable String id) {
        return cachingService.getKeyStats(id);
    }

}

