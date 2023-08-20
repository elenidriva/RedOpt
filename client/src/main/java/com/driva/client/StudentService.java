package com.driva.client;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;


@Slf4j
@Service
//@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final RestTemplate restTemplate;

    Counter cacheMissCounter;

    public StudentService(StudentRepository studentRepository, RestTemplate restTemplate, CompositeMeterRegistry registry) {
        this.studentRepository = studentRepository;
        this.restTemplate = restTemplate;
        cacheMissCounter = Counter
                .builder("RedisOptis")
                .description("indicates instance count of the object")
                .tags("redisOptis", "cache.miss")
                .register(registry);
    }

    public Student getStudent(Long id) {
        log.info(String.format("Attempting to retrieve Student with id: [%s].", id));
        Student student = restTemplate
                .getForObject(UriComponentsBuilder.fromHttpUrl("http://localhost:8080/cache/get/{id}").buildAndExpand(id.toString()).toUri(), Student.class);
        if (student == null) {
            student = studentRepository.findById(id).orElseThrow(() -> new RuntimeException("The student does not exist."));
            cacheMissCounter.increment();
            log.info(String.format("Cache Miss retrieving Student with id: [%s].", id));
            restTemplate.postForObject(UriComponentsBuilder.fromHttpUrl("http://localhost:8080/cache/put/{id}")
                    .buildAndExpand(student.getId().toString())
                    .toUri(), student, Void.class);
        }

        return student;
    }


    public List<Student> getStudents() {
        log.info(String.format("Attempting to retrieve Students"));
        List<Student> students = restTemplate
                .getForObject(UriComponentsBuilder.fromHttpUrl("http://localhost:8080/cache/get/").build().toUri(), List.class);
        return studentRepository.findAll();

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
        log.info(String.format("Saved student with id [%s].", st.getId()));
        restTemplate.postForObject(UriComponentsBuilder.fromHttpUrl("http://localhost:8080/cache/put/{id}")
                .buildAndExpand(st.getId().toString())
                .toUri(), student, Void.class);
        return st;
    }


    public void delete(Long id) {
        log.info(String.format("Deleting student with id: [%s]", id));
        studentRepository.deleteById(id);
        restTemplate.delete(UriComponentsBuilder.fromHttpUrl("http://localhost:8080/cache/delete/{id}")
                .buildAndExpand(id.toString())
                .toUri());
        log.info("Deleted student.");

    }

    public void updateStudent(Long id, StudentDTO studentDTO) {
        log.info(String.format("Updating student with id: [%s]", id));
        Optional<Student> student = studentRepository.findById(id);
        student.ifPresent(student1 -> {
            log.info("Student found.");
            student1.setName(studentDTO.getName());
            student1.setSurname(studentDTO.getSurname());
            student1.setEmail(studentDTO.getEmail());
            student1.setPassword(studentDTO.getPassword());
            student1.setSex(studentDTO.getSex());
            student1.setAge(studentDTO.getAge());
            student1.setFavouriteTeam(studentDTO.getFavouriteTeam());

            studentRepository.save(student1);
            restTemplate.postForObject(UriComponentsBuilder.fromHttpUrl("http://localhost:8080/cache/put/{id}")
                    .buildAndExpand(student1.getId().toString())
                    .toUri(), student, Void.class);
        });
    }

// List has element limitation The max length of a Redis list is 2^32 - 1 (4,294,967,295) elements.

//    # Memory
//    used_memory:897384 -----------------------------------------------------------
//    used_memory_human:876.35K -----------------------------------------------
//    used_memory_rss:11739136
//    used_memory_rss_human:11.20M
//    used_memory_peak:976416
//    used_memory_peak_human:953.53K
//    used_memory_peak_perc:91.91%
//    used_memory_overhead:853224
//    used_memory_startup:812080
//    used_memory_dataset:44160
//    used_memory_dataset_perc:51.77%
//    allocator_allocated:1080896
//    allocator_active:1380352
//    allocator_resident:4239360
//    total_system_memory:16735203328 ---------------------------------------------
//    total_system_memory_human:15.59G ----------------------------------------
//    used_memory_lua:30720
//    used_memory_lua_human:30.00K
//    used_memory_scripts:0
//    used_memory_scripts_human:0B
//    number_of_cached_scripts:0
//    maxmemory:0
//    maxmemory_human:0B
//    maxmemory_policy:noeviction
//    allocator_frag_ratio:1.28
//    allocator_frag_bytes:299456
//    allocator_rss_ratio:3.07
//    allocator_rss_bytes:2859008
//    rss_overhead_ratio:2.77
//    rss_overhead_bytes:7499776
//    mem_fragmentation_ratio:13.74
//    mem_fragmentation_bytes:10884520
//    mem_not_counted_for_evict:0
//    mem_replication_backlog:0
//    mem_clients_slaves:0
//    mem_clients_normal:41000
//    mem_aof_buffer:0
//    mem_allocator:jemalloc-5.1.0
//    active_defrag_running:0
//    lazyfree_pending_objects:0
//    lazyfreed_objects:0
}
