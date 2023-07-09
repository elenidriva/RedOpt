package com.driva;

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

    @PostMapping("/cache/put")
    public void put(@RequestBody Map<String, Object> object) {
        cachingService.put(object);
    }

    @DeleteMapping("/cache/delete/{id}")
    public void delete(@PathVariable String id) {
        cachingService.delete(id);
    }

}

