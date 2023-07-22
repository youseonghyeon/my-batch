package com.example.settlementnew.service;

import com.example.settlementnew.entity.DailySettlement;
import com.example.settlementnew.entity.Settlement;
import com.example.settlementnew.repository.DailySettlementRepository;
import com.example.settlementnew.repository.SettlementRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
class SettlementServiceTest {

    @Autowired
    private SettlementService settlementService;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private DailySettlementRepository dailySettlementRepository;

    @Test
    void insertMockDataTest() {
        int size = 9876;
        settlementService.insertMockData(size);
        assertEquals(size, settlementRepository.count());
    }

    @Test
    @Transactional
    void jdbcDailySettlementTest() {
        // given
        LocalDateTime dataCreateTime = LocalDateTime.of(2021, 1, 1, 0, 0, 0);
        List<Settlement> settlementList = Arrays.asList(
                new Settlement(6L, "user1", 1000, dataCreateTime),
                new Settlement(5L, "user1", 500, dataCreateTime),
                new Settlement(4L, "user2", 800, dataCreateTime));
        settlementService.insertSettlements(settlementList);

        //when
        LocalDate targetDate = dataCreateTime.toLocalDate();
        settlementService.jdbcDailySettlement(targetDate);

        //then
        List<DailySettlement> findDailySettlements = dailySettlementRepository.findAllByTargetDate(targetDate);
        assertEquals(2, findDailySettlements.size());

        assertEquals(1500, getTotalPrice(findDailySettlements, "user1"));
        assertEquals(800, getTotalPrice(findDailySettlements, "user2"));

    }

    private int getTotalPrice(List<DailySettlement> allByTargetDate, String user) {
        List<DailySettlement> collect = allByTargetDate.stream().filter(ds -> ds.getUsername().equals(user)).toList();
        return collect.get(0).getTotalPrice();
    }
}
