package com.example.settlementnew.api;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class History {

    private boolean status;

    private String from;

    private String to;

    private long amount;

    private LocalDateTime createdAt;

    public History(boolean status, String from, String to, long amount) {
        this.status = status;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }

    public History(boolean status, String from, String to, long amount, LocalDateTime createdAt) {
        this.status = status;
        this.from = from;
        this.to = to;
        this.amount = amount;
        this.createdAt = createdAt;
    }
}
