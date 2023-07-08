package com.example.settlementnew.step;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementStep {


    private final JobRepository jobRepository;
    private final PlatformTransactionManager ptm;
    private final SettlementService settlementService;


    @Bean(name = "dailySettlementStep")
    @JobScope
    public Step dailySettlementStep(@Value("#{jobParameters[targetDate]}") String targetDate) {
        return new StepBuilder("dailySettlementStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("==================== 2단계 데이터 정산 시작 ====================");
                    LocalDate target = LocalDate.parse(targetDate, DateTimeFormatter.ISO_DATE);
                    log.info("현재 날짜 : {}, 정산 날짜 : {}", LocalDate.now().format(DateTimeFormatter.ISO_DATE), targetDate);
                    settlementService.jdbcDailySettlement(target);
                    return RepeatStatus.FINISHED;
                }, ptm).build();
    }

}
