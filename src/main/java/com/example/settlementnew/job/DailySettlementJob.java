package com.example.settlementnew.job;

import com.example.settlementnew.entity.DailySettlement;
import com.example.settlementnew.entity.TransferHistory;
import com.example.settlementnew.processor.RetryTransferProcessor;
import com.example.settlementnew.processor.TransferProcessor;
import com.example.settlementnew.service.SettlementService;
import com.example.settlementnew.step.MessageStep;
import com.example.settlementnew.step.MockDataInsertStep;
import com.example.settlementnew.step.TransferValidationStep;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
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
public class DailySettlementJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager ptm;
    private final EntityManagerFactory emf;

    private final MockDataInsertStep mockDataInsertStep;

    private final MessageStep messageStep;
    private final SettlementService settlementService;

    private final TransferProcessor transferProcessor;
    private final RetryTransferProcessor retryTransferProcessor;

    private final TransferValidationStep transferValidationStep;

    private static final String SELECT_FAILED_TRANSFER = "SELECT th FROM TransferHistory th WHERE th.status = 'FAILED'";
    private static final String SELECT_DAILY_SETTLEMENT = "SELECT ds FROM DailySettlement ds";

    /**
     * 일일 정산 배치
     * 1. mock 데이터 삽입
     * 2. 일일 정산
     * 3. 정산 이체
     * 4. 정산 이체 실패시 재이체
     * 5. 정산 이체 결과 메시지 전송
     * 6. 정산 이체 결과 검증
     *
     * @JobParameter : mockSize, targetDate, chunkSize
     */
    @Bean(name = "dailyJob")
    public Job dailyJob() {
        return new JobBuilder("dailyJob", jobRepository)
                .start(mockDataInsertStep.insertMockSettlementStep(null)) // mock 데이터 삽입
                .next(dailySettlementStep(null)) // 일일 정산
                .next(transferSettlementStep(null)) // 정산 이체
                .next(retryTransferSettlementStep(null)) // 정산 이체 실패시 재이체
                .next(messageStep.sendMessageStep(null)) // 정산 이체 결과 메시지 전송
//                .next(transferValidationStep.validationStep()) // 정산 이체 결과 검증
                .build();
    }


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


    @Bean(name = "transferSettlementStep")
    @JobScope
    public Step transferSettlementStep(@Value("#{jobParameters[chunkSize]}") Integer chunkSize) {
        log.info("==================== 3단계 송금 시작 ====================");
        return new StepBuilder("transferSettlementStep", jobRepository)
                .<DailySettlement, TransferHistory>chunk(chunkSize, ptm)
                .reader(dailySettlementReader(null))
                .processor(transferProcessor)
                .writer(settlementWriter())
                .build();

    }

    @Bean
    @StepScope
    public JpaPagingItemReader<DailySettlement> dailySettlementReader(@Value("#{jobParameters[chunkSize]}") Integer chunkSize) {
        JpaPagingItemReader<DailySettlement> reader = new JpaPagingItemReader<>();
        reader.setQueryString(SELECT_DAILY_SETTLEMENT);
        reader.setEntityManagerFactory(emf);
        reader.setPageSize(chunkSize);
        return reader;
    }

    @Bean
    public JpaItemWriter<TransferHistory> settlementWriter() {
        JpaItemWriter<TransferHistory> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        return writer;
    }


    @Bean(name = "retryTransferSettlementStep")
    @JobScope
    public Step retryTransferSettlementStep(@Value("#{jobParameters[chunkSize]}") Integer chunkSize) {
        log.info("==================== 4단계 실패한 결과 재송금 시작 ====================");
        return new StepBuilder("retryTransferSettlementStep", jobRepository)
                .<TransferHistory, TransferHistory>chunk(chunkSize, ptm)
                .reader(retrySettlementReader(null))
                .processor(retryTransferProcessor)
                .writer(retrySettlementWriter())
                .build();

    }

    @Bean
    @StepScope
    public JpaPagingItemReader<TransferHistory> retrySettlementReader(@Value("#{jobParameters[chunkSize]}") Integer chunkSize) {
        JpaPagingItemReader<TransferHistory> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(emf);
        reader.setQueryString(SELECT_FAILED_TRANSFER);
        reader.setPageSize(chunkSize);
        return reader;
    }

    @Bean
    public JpaItemWriter<TransferHistory> retrySettlementWriter() {
        JpaItemWriter<TransferHistory> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        return writer;
    }
}
