package com.example.opt.client;


import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

//@RedisHash("Student")
@Data
@Entity
@Table(name="student")
public class Student implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(name="student_name", length=50, nullable=false, unique=false)
    private String name;

    public Student(String name) {
        this.name = name;
    }

    public Student() {

    }
    // ...
}