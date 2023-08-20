package com.driva.client;

import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @Timed("RedisOptisClient - getStudent")
    @GetMapping("/students/{id}/")
    public Student getStudent(@PathVariable Long id) {
        return studentService.getStudent(id);
    }

    @Timed("RedisOptisClient - createStudent")
    @PostMapping("/students/")
    public Student createStudent(@RequestBody StudentDTO studentDTO) {
        return studentService.createStudent(studentDTO);
    }

    @Timed("RedisOptisClient - deleteStudent")
    @DeleteMapping("/students/{id}/")
    public void deleteStudent(@PathVariable Long id) {
        studentService.delete(id);
    }

    @Timed("RedisOptisClient - putStudent")
    @PutMapping("/students/{id}/")
    public void updateStudent(@PathVariable Long id, @RequestBody StudentDTO studentDTO) {
        studentService.updateStudent(id, studentDTO);
    }
}

