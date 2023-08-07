package com.driva.server;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ServerController {

    private final CachingService cachingService;

    @Timed(value = "RedisOptis - getStudent")
    @GetMapping("/cache/get/{id}")
    public Map<String, Object> get(@PathVariable String id) {
        return cachingService.get(id);
    }

    @Timed(value = "RedisOptis - putStudent")
    @PostMapping("/cache/put/{id}")
    public void put(@PathVariable String id, @RequestBody Map<String, Object> object) {
        cachingService.put(id, object);
    }

    @Timed(value = "RedisOptis - deleteStudent")
    @DeleteMapping("/cache/delete/{id}")
    public void delete(@PathVariable String id) {
        cachingService.delete(id);
    }

}

