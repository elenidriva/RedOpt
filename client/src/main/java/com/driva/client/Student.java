package com.driva.client;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;


@Data
@Entity
@Table(name="student")
public class Student implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(name="name", length=15000, nullable=false)
    private String name;

    @Column(name="surname", length=15000, nullable=false)
    private String surname;

    @Column(name="email", length=15000, nullable=false)
    private String email;

    @Column(name="password", length=15000, nullable=false)
    private String password;

    @Column(name="sex", length=15000, nullable=false)
    private String sex;

    @Column(name="age", length=3, nullable=false)
    private Integer age;

    @Column(name="favouriteTeam", length=25, nullable=false)
    private String favouriteTeam;


    public Student() {
    }

    public Student(String name, String surname, String email, String password, String sex, Integer age, String favouriteTeam) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.sex = sex;
        this.age = age;
        this.favouriteTeam = favouriteTeam;
    }
}
