package com.example.settlementnew.processor;

import com.example.settlementnew.api.BankApi;
import com.example.settlementnew.api.History;
import com.example.settlementnew.entity.DailySettlement;
import com.example.settlementnew.entity.TransferHistory;
import com.example.settlementnew.entity.TransferStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class TransferProcessorTest {

    @Autowired
    private TransferProcessor transferProcessor;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public BankApi bankApi() {
            return (fromUsername, toUsername, price) -> new History(true, fromUsername, toUsername, price);
        }
    }

    @Test
    void process() {

        //given
        DailySettlement dailySettlement = new DailySettlement(1L, "user1", 1000, LocalDate.now(), LocalDateTime.now(), Collections.emptyList());

        //when
        TransferHistory transferHistory = transferProcessor.process(dailySettlement);

        //then
        assertNotNull(transferHistory);
        assertEquals(TransferStatus.COMPLETED, transferHistory.getStatus());
        assertEquals("myJob", transferHistory.getFromUsername());
        assertEquals("user1", transferHistory.getToUsername());
        assertEquals(1000, transferHistory.getAmount());

    }
}
