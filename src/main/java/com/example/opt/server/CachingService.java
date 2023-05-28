package com.example.opt.server;

import com.example.opt.client.Student;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CachingService {

    private final StudentCacheRepository studentCacheRepository;
    private final KeyStatsCacheRepository keyStatsCacheRepository;


    public Student put(Student student) {
       KeyStats keyStats =  studentCacheRepository.put(student);
        keyStatsCacheRepository.put(keyStats);
        return student;
    }

    public Student get(Long id) {
        Student student = studentCacheRepository.get(id);
        if (student !=null) {
            keyStatsCacheRepository.increment(id);
        }
        return student;
    }

    public void delete(Long id) {
        studentCacheRepository.delete(id);
        keyStatsCacheRepository.delete(id);
    }
}
