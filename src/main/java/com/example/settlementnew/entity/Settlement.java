package com.example.settlementnew.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private int price;

    private LocalDateTime createdAt;

    public Settlement(String username, int price) {
        this.username = username;
        this.price = price;
        this.createdAt = LocalDateTime.now();
    }

    public Settlement(Long id, String username, int price, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.price = price;
        this.createdAt = createdAt;
    }

    public Settlement(Long id, String username, int price) {
        this.id = id;
        this.username = username;
        this.price = price;
        this.createdAt = LocalDateTime.now();
    }
}
