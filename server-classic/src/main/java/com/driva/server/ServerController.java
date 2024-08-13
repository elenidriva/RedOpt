package com.driva.server;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ServerController {

    private final CachingService cachingService;

    @Timed(value = "RedisClassicServer - getStudent")
    @GetMapping("/cache/{id}")
    public Student get(@PathVariable Long id) {
        return cachingService.getM(id);
    }

    @Timed(value = "RedisClassicServer - putStudent")
    @PostMapping("/cache/")
    public Student save(@RequestBody StudentDTO studentDTO) {
        return cachingService.createStudent(studentDTO);
    }

    @Timed(value = "RedisClassicServer - deleteStudent")
    @DeleteMapping("/cache/{id}")
    public void delete(@PathVariable Long id) {
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

