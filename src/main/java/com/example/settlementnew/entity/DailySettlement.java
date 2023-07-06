package com.example.settlementnew.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class DailySettlement {

    @Id
    @GeneratedValue
    private Long id;

    private String username;

    private int totalPrice;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "dailySettlement", fetch = FetchType.LAZY)
    private List<TransferHistory> transferHistory;
}
