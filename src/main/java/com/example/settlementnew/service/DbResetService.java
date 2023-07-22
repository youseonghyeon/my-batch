package com.example.settlementnew.service;

import com.example.settlementnew.repository.DailySettlementRepository;
import com.example.settlementnew.repository.SettlementRepository;
import com.example.settlementnew.repository.TransferHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DbResetService {

    private final DailySettlementRepository dailySettlementRepository;
    private final SettlementRepository settlementRepository;
    private final TransferHistoryRepository transferHistoryRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void reset() {
        log.info("data reset");
        transferHistoryRepository.deleteAll();
        dailySettlementRepository.deleteAll();
        settlementRepository.deleteAll();

    }





}
