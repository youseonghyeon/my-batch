package com.example.settlementnew.entity;

public enum TransferStatus {
    PENDING("Pending"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    REATTEMPT("Reattempt");

    private final String status;

    TransferStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
