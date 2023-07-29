package com.example.settlementnew.job;

import com.example.settlementnew.aop.SendStartMessage;
import com.example.settlementnew.entity.DailySettlement;
import com.example.settlementnew.entity.TransferHistory;
import com.example.settlementnew.processor.RetryTransferProcessor;
import com.example.settlementnew.processor.TransferProcessor;
import com.example.settlementnew.service.SettlementService;
import com.example.settlementnew.socket.SocketSender;
import com.example.settlementnew.step.MessageStep;
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


    private final MessageStep messageStep;
    private final SettlementService settlementService;
    private final TransferProcessor transferProcessor;
    private final RetryTransferProcessor retryTransferProcessor;
    private final SocketSender socketSender;

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
                .start(insertMockSettlementStep(null)) // mock 데이터 삽입
                .next(delayStep())

                .next(dailySettlementStep(null)) // 일일 정산
                .next(delayStep())

                .next(transferSettlementStep(null)) // 정산 이체
                .next(delayStep())

                .next(retryTransferSettlementStep(null)) // 정산 이체 실패시 재이체
                .next(delayStep())

                .next(messageStep.sendMessageStep(null)) // 정산 이체 결과 메시지 전송
                .next(finishStep())

                .build();
    }

    @Bean(name = "insertMockSettlementStep")
    @JobScope
    @SendStartMessage(title = "Step 1. Mock 데이터 삽입", detail = "Mock 데이터 삽입을 시작합니다.")
    public Step insertMockSettlementStep(@Value("#{jobParameters[mockSize]}") Integer mockSize) {
        return new StepBuilder("insertMockSettlementStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("==================== Step 1. Mock 데이터 삽입 시작 ====================");
                    settlementService.insertMockData(mockSize);
                    return RepeatStatus.FINISHED;
                }, ptm)
                .build();
    }


    @Bean(name = "dailySettlementStep")
    @JobScope
    @SendStartMessage(title = "Step 2. 일일 정산", detail = "해당 날짜의 금액의 Total을 구합니다.", img = "mysql.png")
    public Step dailySettlementStep(@Value("#{jobParameters[targetDate]}") String targetDate) {
        return new StepBuilder("dailySettlementStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("==================== Step 2. 데이터 정산 시작 ====================");
                    LocalDate target = LocalDate.parse(targetDate, DateTimeFormatter.ISO_DATE);
                    log.info("현재 날짜 : {}, 정산 날짜 : {}", LocalDate.now().format(DateTimeFormatter.ISO_DATE), targetDate);
                    settlementService.jdbcDailySettlement(target);
                    return RepeatStatus.FINISHED;
                }, ptm).build();
    }


    @Bean(name = "transferSettlementStep")
    @JobScope
    @SendStartMessage(title = "Step 3. 정산 이체", detail = "정산 이체를 시작합니다.(test API)", img = "bank.png")
    public Step transferSettlementStep(@Value("#{jobParameters[chunkSize]}") Integer chunkSize) {
        log.info("==================== Step 3. 송금 시작 ====================");
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
    @SendStartMessage(title = "Step 4. 재송금", detail = "실패한 정산 이체를 재송금합니다.")
    public Step retryTransferSettlementStep(@Value("#{jobParameters[chunkSize]}") Integer chunkSize) {
        log.info("==================== Step 4. 실패한 결과 재송금 시작 ====================");
        return new StepBuilder("retryTransferSettlementStep", jobRepository)
                .<TransferHistory, TransferHistory>chunk(chunkSize, ptm)
                .reader(retrySettlementReader(null))
                .processor(retryTransferProcessor)
                .writer(settlementWriter())
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



    public Step finishStep() {
        return new StepBuilder("finishStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("==================== Step 7. 정산 배치 종료 ====================");
                    socketSender.sendStatus("정산 배치가 종료되었습니다.", null, null);
                    return RepeatStatus.FINISHED;
                }, ptm).build();
    }


    @Bean
    public Step delayStep() {
        return new StepBuilder("delayStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Thread.sleep(1000);
                    return RepeatStatus.FINISHED;
                }, ptm).build();
    }
}
