package com.example.opt.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KeyStats implements Serializable {

    private String key;
    private Long frequency;
    private Long size;
    private Long insertedTime;
    private Long lastQueriedTime;
    private Double weight;

}
