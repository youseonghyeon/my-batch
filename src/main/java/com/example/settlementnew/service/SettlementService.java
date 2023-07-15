package com.example.settlementnew.service;

import com.example.settlementnew.entity.Settlement;
import com.example.settlementnew.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final SettlementRepository settlementRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void insertMockData(int size) {
        if (size < 5000) {
            jpaBatchInsert(size);
        } else {
            jdbcBatchInsert(size);
        }
    }

    private void jdbcBatchInsert(int size) {
        log.info("데이터 {}개 삽입 [jdbcBatchInsert]", size);
        List<Settlement> settlements = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            settlements.add(new Settlement((long) i, "user" + i % 1000, i * 5438 % 10000));
        }
        String sql = "insert into settlement (id, username, price, created_at) values (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, settlements, 5000 , (ps, arg) -> {
            ps.setLong(1, arg.getId());
            ps.setString(2, arg.getUsername());
            ps.setInt(3, arg.getPrice());
            ps.setTimestamp(4, Timestamp.valueOf(arg.getCreatedAt()));
        });
    }

    private void jpaBatchInsert(int size) {
        log.info("데이터 {}개 삽입 [jpaBatchInsert]", size);
        List<Settlement> settlements = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            settlements.add(new Settlement("user" + i % 1000, i * 5438 % 10000));
        }
        settlementRepository.saveAll(settlements);
    }


    public void jdbcDailySettlement(LocalDate targetDate) {
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay();
        String sql = "insert into daily_settlement (id, username, total_price, created_at) " +
                "select sum(id), username, sum(price), CURRENT_TIMESTAMP()  from settlement where created_at between ? and ? group by username";
        jdbcTemplate.update(sql, start, end);
    }
}
