package com.example.opt.client;


import com.example.opt.server.CachingService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
   // private final KeyStatsCacheRepository keyStatsRepository;

    private final CachingService cachingService;

    public Student getStudent(Long id) {
      // studentRepository.save()
        Student student = cachingService.get(id);
       if (student == null) {
           return studentRepository.findById(id).orElseThrow(() -> new RuntimeException("The student does not exist."));
       }

        return student;
    }

    public Student createStudent(StudentDTO studentDTO) {
        log.info("Saving student.");
        Student student = new Student(studentDTO.getName());
        Student st = studentRepository.save(student);
        cachingService.put(student);
        return st;
    }


    public void delete(Long id) {
        log.info("Deleting student.");
        studentRepository.deleteById(id);
        cachingService.delete(id);
        log.info("Deleted student.");

    }

    public void updateStudent(Long id, StudentDTO studentDTO) {
        log.info("Updating student.");
        Optional<Student> student = studentRepository.findById(id);
        student.ifPresent(student1 -> {
            log.info("Student found.");
            student1.setName(studentDTO.getName());
            studentRepository.save(student1);
            cachingService.put(student1);
        });
    }



//  ( (total_memory - used_memory) / total_memory ) > 80%
//  Kathe key pou mpainei i ginetai query prepei na kratame poses fores egine query - to size otan mpainei stin cache
// i redis kanei persist ta kleidia p mpainoun - den eisxuei to idio ama kanw restat
    // prepei na ta krataw k auta sitn cache


    //1. me store listass mporw na parw oli ti lista kai na petaksw o,ti thelw
    //2. me to na vazw se didaforetika keys gia ta stats pws mporw na ta querarw ola gia na dw stats.
    //3.








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
