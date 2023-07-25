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
@ToString(exclude = "dailySettlement")
@NoArgsConstructor
public class TransferHistory {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransferStatus status;

    private String fromUsername;

    private String toUsername;

    private long amount;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "daily_settlement_id")
    private DailySettlement dailySettlement;

    public TransferHistory(TransferStatus status, String fromUsername, String toUsername, long amount) {
        this.status = status;
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }

    public TransferHistory(TransferStatus status, String fromUsername, String toUsername, long amount, DailySettlement dailySettlement) {
        this.status = status;
        this.fromUsername = fromUsername;
        this.toUsername = toUsername;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.dailySettlement = dailySettlement;
    }
}
