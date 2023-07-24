package com.example.settlementnew.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(exclude = "transferHistory")
@NoArgsConstructor
@AllArgsConstructor
public class DailySettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private int totalPrice;

    private LocalDate targetDate;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "dailySettlement", fetch = FetchType.LAZY)
    private List<TransferHistory> transferHistory;
}
