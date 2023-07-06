package com.example.settlementnew.entity;

import lombok.Data;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class PeekTime {

    private String username;
    private String totalPrice;
    private Map<LocalTime, Integer> salesByTime = new HashMap<>();
}
