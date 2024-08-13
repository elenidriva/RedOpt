package com.driva.server;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CachingService {

    private final CacheRepository cacheRepository;
    private final KeyStatsCacheRepository keyStatsCacheRepository;
    private final StudentRepository studentRepository;
    Counter cacheMissCounter;

    public CachingService(CacheRepository cacheRepository,
                          KeyStatsCacheRepository keyStatsCacheRepository,
                          StudentRepository studentRepository,
                          CompositeMeterRegistry registry) {
        this.cacheRepository = cacheRepository;
        this.keyStatsCacheRepository = keyStatsCacheRepository;
        this.studentRepository = studentRepository;
        cacheMissCounter = Counter
                .builder("RedisClassic")
                .description("indicates instance count of the object")
                .tags("redisClassic", "cache.miss")
                .register(registry);
    }


    public Student createStudent(StudentDTO studentDTO) {
        log.info("Saving student.");
        Student student = new Student(studentDTO.getName(),
                studentDTO.getSurname(),
                studentDTO.getEmail(),
                studentDTO.getPassword(),
                studentDTO.getSex(),
                studentDTO.getAge(),
                studentDTO.getFavouriteTeam());
        Student st = studentRepository.save(student);
        cacheRepository.putM(String.valueOf(st.getId()), student);
        return st;
    }

    public Student getM(Long id) {
        log.info(String.format("Attempting to retrieve Student with id: [%s].", id));
        Student student = cacheRepository.get(String.valueOf(id));
        if (student == null) {
            student = studentRepository.findById(id).orElseThrow(() -> new RuntimeException("The student does not exist."));
            cacheMissCounter.increment();
            log.info(String.format("Cache Miss retrieving Student with id: [%s].", id));
            cacheRepository.putM(String.valueOf(student.getId()), student);
        }
        return student;
    }

    public void deleteM(Long id) {
        studentRepository.deleteById(id);
        cacheRepository.delete(String.valueOf(id));
    }


    public MemoryStats getMemoryStats() {
        return new MemoryStats(keyStatsCacheRepository.getMaxMemory(), keyStatsCacheRepository.getUsedMemory(), keyStatsCacheRepository.getMaxMemoryHuman(), keyStatsCacheRepository.getUsedMemoryHuman(), keyStatsCacheRepository.percentageOccupied(), keyStatsCacheRepository.dbSize());
    }

    public KeyStats getKeyStats(String id) {
        return keyStatsCacheRepository.getWithKey(id);
    }
}
