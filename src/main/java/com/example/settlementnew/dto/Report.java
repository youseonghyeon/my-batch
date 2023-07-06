package com.example.settlementnew.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Report {

    // 총 매출
    private int totalSales;
    // 총 입금 내역
    private int totalDeposit;
    // Report 작성 일자, 시간
    private LocalDateTime createdAt;
    // 메일 예약 전송 시간
    private LocalDateTime sendAt;


    public Report(int totalSales, int totalDeposit, LocalDateTime createdAt, LocalDateTime sendAt) {
        this.totalSales = totalSales;
        this.totalDeposit = totalDeposit;
        this.createdAt = createdAt;
        this.sendAt = sendAt;
    }
}
