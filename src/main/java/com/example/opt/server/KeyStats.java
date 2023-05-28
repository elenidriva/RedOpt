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
    private Long queryFrequency;
    private Long objectSize;

}
