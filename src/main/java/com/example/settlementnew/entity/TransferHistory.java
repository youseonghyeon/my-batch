package com.example.settlementnew.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class TransferHistory {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    private String fromUsername;

    private String toUsername;

    private int amount;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "daily_settlement_id")
    private DailySettlement dailySettlement;

    public TransferHistory(TransferStatus status, String fromUsername, String toUsername, int amount) {
        this.status = status;
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }
}