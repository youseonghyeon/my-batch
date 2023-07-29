package com.example.settlementnew.service;

import com.example.settlementnew.socket.SocketSender;
import com.example.settlementnew.entity.Settlement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

    private final JdbcTemplate jdbcTemplate;
    private final SocketSender wasWebSocketHandler;
    private static final Random RANDOM = new Random();
    private NumberFormat numberFormat = NumberFormat.getInstance();

    private final int BATCH_SIZE = 1000;


    @Transactional
    public void insertMockData(int size) {
        log.info("데이터 {}개 삽입 [jdbcBatchInsert]", size);
        List<Settlement> settlements = new ArrayList<>();
        for (int i = 1; i < size + 1; i++) {

            settlements.add(new Settlement("user" + i % 10000, 5000 + RANDOM.nextInt(151) * 100));
            if (i % BATCH_SIZE == 0) {
                insertSettlements(settlements);
                settlements.clear();
                wasWebSocketHandler.sendStatus("Mock 데이터 삽입", numberFormat.format(i) + " 개 Insert 완료.");
            }
        }
        if (!settlements.isEmpty()) {
            insertSettlements(settlements);
        }
    }

    @Transactional
    public int insertSettlements(List<Settlement> settlements) {
        String sql = "insert into settlement (username, price, created_at) values (?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, settlements, BATCH_SIZE, (ps, arg) -> {
            ps.setString(1, arg.getUsername());
            ps.setLong(2, arg.getPrice());
            ps.setTimestamp(3, Timestamp.valueOf(arg.getCreatedAt()));
        });
        return settlements.size();
    }

    public void jdbcDailySettlement(LocalDate targetDate) {
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay();
        String sql = "insert into daily_settlement (username, total_price, created_at, target_date) " +
                "select username, sum(price), CURRENT_TIMESTAMP(), ?  from settlement where created_at between ? and ? group by username";
        jdbcTemplate.update(sql, targetDate, start, end);
    }
}
