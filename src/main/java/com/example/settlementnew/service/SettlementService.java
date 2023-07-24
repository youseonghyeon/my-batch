package com.example.settlementnew.service;

import com.example.settlementnew.dto.socket_message.SocketMessage;
import com.example.settlementnew.config.socket.WasWebSocketHandler;
import com.example.settlementnew.dto.socket_message.StatusMessage;
import com.example.settlementnew.entity.Settlement;
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

    private final JdbcTemplate jdbcTemplate;
    private final WasWebSocketHandler wasWebSocketHandler;

    @Transactional
    public void insertMockData(int size) {
        log.info("데이터 {}개 삽입 [jdbcBatchInsert]", size);
        List<Settlement> settlements = new ArrayList<>();
        for (int i = 1; i < size + 1; i++) {
            settlements.add(new Settlement("user" + i % 10000, i * 5438 % 10000));
            if (i % 10000 == 0) {
                insertSettlements(settlements);
                settlements.clear();
                SocketMessage socketMessage = new StatusMessage("Mock 데이터 삽입", i + "개 삽입 완료.");
                wasWebSocketHandler.sendMessage(socketMessage);
            }
        }
        if (!settlements.isEmpty()) {
            insertSettlements(settlements);
        }
    }

    @Transactional
    public int insertSettlements(List<Settlement> settlements) {
        String sql = "insert into settlement (username, price, created_at) values (?, ?, ?)";
        jdbcTemplate.batchUpdate(sql, settlements, 5000, (ps, arg) -> {
            ps.setString(1, arg.getUsername());
            ps.setInt(2, arg.getPrice());
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
