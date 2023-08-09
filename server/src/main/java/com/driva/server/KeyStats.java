package com.driva.server;

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

    private Long cacheMissFrequency;
    private Long cacheMissDurationTime;

    private Double weight;

}
