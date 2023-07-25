package com.example.settlementnew.processor;

import com.example.settlementnew.api.BankApi;
import com.example.settlementnew.api.History;
import com.example.settlementnew.entity.TransferHistory;
import com.example.settlementnew.entity.TransferStatus;
import com.example.settlementnew.repository.TransferHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RetryTransferProcessorTest {

    @Autowired
    private RetryTransferProcessor retryTransferProcessor;
    @Autowired
    private TransferHistoryRepository transferHistoryRepository;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public BankApi bankApi() {
            return (fromUsername, toUsername, price) -> new History(true, fromUsername, toUsername, price);
        }
    }

    @Test
    @Transactional
    void processTest() {
        //given
        TransferHistory failCase = createFailCase(TransferStatus.FAILED, "user1", "user2", 1000);
        transferHistoryRepository.save(failCase);

        //when
        TransferHistory processed = retryTransferProcessor.process(failCase);

        //then
        TransferHistory reattemptHistory = transferHistoryRepository.findById(failCase.getId()).get();
        assertEquals(TransferStatus.REATTEMPT, reattemptHistory.getStatus());

        assertEquals(TransferStatus.COMPLETED, processed.getStatus());

    }

    private TransferHistory createFailCase(TransferStatus status, String fromUsername, String toUsername, long amount) {
        return new TransferHistory(status, fromUsername, toUsername, amount, null);
    }


}
