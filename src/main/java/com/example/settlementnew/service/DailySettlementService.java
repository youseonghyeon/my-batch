package com.example.settlementnew.service;

import com.example.settlementnew.entity.DailySettlement;
import com.example.settlementnew.repository.DailySettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailySettlementService {

    private final DailySettlementRepository dailySettlementRepository;

    public Optional<DailySettlement> findByUsernameAndCreatedAt(String username, LocalDate targetDate) {
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay();
        return dailySettlementRepository.findByUsernameAndCreatedAtBetween(username, start, end);
    }




}
