package com.driva.client;

import lombok.Data;

@Data
public class StudentDTO {

    private Long id;
    private String name;
    private String surname;
    private String email;
    private String password;
    private String sex;
    private Integer age;
    private String favouriteTeam;
}
