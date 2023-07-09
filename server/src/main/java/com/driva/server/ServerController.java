package com.driva.server;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ServerController {

    private final CachingService cachingService;

    @GetMapping("/cache/get/{id}")
    public Map<String, Object> get(@PathVariable String id) {
        return cachingService.get(id);
    }

    @PostMapping("/cache/put/{id}")
    public void put(@PathVariable String id, @RequestBody Map<String, Object> object) {
        cachingService.put(id, object);
    }

    @DeleteMapping("/cache/delete/{id}")
    public void delete(@PathVariable String id) {
        cachingService.delete(id);
    }

}

