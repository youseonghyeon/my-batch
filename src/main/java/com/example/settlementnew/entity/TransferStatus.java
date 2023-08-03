package com.example.settlementnew.entity;

import lombok.Getter;

@Getter
public enum TransferStatus {
    PENDING("Pending"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    REATTEMPT("Reattempt");

    private final String status;

    TransferStatus(String status) {
        this.status = status;
    }

}
