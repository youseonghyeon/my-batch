package com.example.settlementnew.step;

import com.example.settlementnew.aop.SendStartMessage;
import com.example.settlementnew.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MockDataInsertStep {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager ptm;
    private final SettlementService settlementService;

    @Bean(name = "insertMockSettlementStep")
    @JobScope
    @SendStartMessage(title = "Mock 데이터 삽입", detail = "Mock 데이터 삽입을 시작합니다.\n" +
            "jdbcBatch Insert를 사용하여 데이터를 삽입합니다.")
    public Step insertMockSettlementStep(@Value("#{jobParameters[mockSize]}") Integer mockSize) {
        return new StepBuilder("insertMockSettlementStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("==================== 1단계 Mock 데이터 삽입 시작 ====================");
                    settlementService.insertMockData(mockSize);
                    return RepeatStatus.FINISHED;
                }, ptm)
                .build();
    }
}
