package com.example.settlementnew.job;

import com.example.settlementnew.api.History;
import com.example.settlementnew.api.MessageApi;
import com.example.settlementnew.entity.DailySettlement;
import com.example.settlementnew.entity.TransferHistory;
import com.example.settlementnew.entity.TransferStatus;
import com.example.settlementnew.repository.TransferHistoryRepository;
import com.example.settlementnew.service.SettlementService;
import com.example.settlementnew.service.TransferService;
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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DailySettlementJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager ptm;
    private final SettlementService settlementService;
    private final TransferService transferService;
    private final EntityManagerFactory emf;
    private final TransferHistoryRepository transferHistoryRepository;
    private final MessageApi messageApi;



    @Bean(name = "dailyJob")
    public Job dailyJob() {
        return new JobBuilder("dailyJob", jobRepository)
                .start(insertMockSettlementStep(null))
                .next(dailySettlementStep(null))
                .next(transferSettlementStep(null))
                .next(printResultStep())
                .next(reTransferSettlementStep(null))
                .next(printResultStep())
                .build();
    }


    @Bean(name = "insertMockSettlementStep")
    @JobScope
    public Step insertMockSettlementStep(@Value("#{jobParameters[mockSize]}") Integer mockSize) {
        return new StepBuilder("insertMockSettlementStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("==================== 1단계 Mock 데이터 삽입 시작 ====================");
                    settlementService.insertMockData(mockSize);
                    return RepeatStatus.FINISHED;
                }, ptm)
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
                .reader(settlementReader(null))
                .processor(transferProcessor())
                .writer(settlementWriter())
                .build();

    }

    @Bean
    @StepScope
    public JpaPagingItemReader<DailySettlement> settlementReader(@Value("#{jobParameters[chunkSize]}") Integer chunkSize) {
        JpaPagingItemReader<DailySettlement> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(emf);
        reader.setQueryString("select ds from DailySettlement ds");
        reader.setPageSize(chunkSize);
        return reader;
    }

    @Bean
    public ItemProcessor<DailySettlement, TransferHistory> transferProcessor() {
        return dailySettlement -> {
            History history = transferService.transfer(dailySettlement.getUsername(), dailySettlement.getTotalPrice());
            TransferStatus transferStatus = history.isStatus() ? TransferStatus.COMPLETED : TransferStatus.FAILED;

            if (transferStatus == TransferStatus.COMPLETED) {
                messageApi.send(dailySettlement.getUsername(), "송금이 완료되었습니다.");
            }
            return new TransferHistory(transferStatus, history.getFrom(), history.getTo(), history.getAmount());
        };
    }

    @Bean
    public JpaItemWriter<TransferHistory> settlementWriter() {
        JpaItemWriter<TransferHistory> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        return writer;
    }

    @Bean
    public Step printResultStep() {
        return new StepBuilder("printErrorStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    int size = transferHistoryRepository.findAllByStatus(TransferStatus.FAILED).size();
                    log.warn("송금에 실패한 건수 : {}", size);
                    return RepeatStatus.FINISHED;
                }, ptm).build();
    }

    @Bean(name = "reTransferSettlementStep")
    @JobScope
    public Step reTransferSettlementStep(@Value("#{jobParameters[chunkSize]}") Integer chunkSize) {
        log.info("==================== 4단계 실패한 결과 재송금 시작 ====================");
        return new StepBuilder("reTransferSettlementStep", jobRepository)
                .<TransferHistory, TransferHistory>chunk(chunkSize, ptm)
                .reader(reSettlementReader(null))
                .processor(reTransferProcessor())
                .writer(reSettlementWriter())
                .build();

    }

    @Bean
    @StepScope
    public JpaPagingItemReader<TransferHistory> reSettlementReader(@Value("#{jobParameters[chunkSize]}") Integer chunkSize) {
        JpaPagingItemReader<TransferHistory> reader = new JpaPagingItemReader<>();
        reader.setEntityManagerFactory(emf);
        reader.setQueryString("select th from TransferHistory th where th.status = 'FAILED'");
        reader.setPageSize(chunkSize);
        return reader;
    }

    @Bean
    @Transactional
    public ItemProcessor<TransferHistory, TransferHistory> reTransferProcessor() {
        return transferHistory -> {

            // 재송금 한 로그는 REATTEMPT 상태로 변경
            Long id = transferHistory.getId();
            TransferHistory his = transferHistoryRepository.findById(id).orElseThrow(IllegalArgumentException::new);
            his.setStatus(TransferStatus.REATTEMPT);

            History history = transferService.transfer(transferHistory.getToUsername(), transferHistory.getAmount());
            TransferStatus transferStatus = history.isStatus() ? TransferStatus.COMPLETED : TransferStatus.FAILED;
            if (transferStatus == TransferStatus.COMPLETED) {
                messageApi.send(transferHistory.getToUsername(), "송금이 완료되었습니다.");
            }
            return new TransferHistory(transferStatus, history.getFrom(), history.getTo(), history.getAmount());
        };
    }

    @Bean
    public JpaItemWriter<TransferHistory> reSettlementWriter() {
        JpaItemWriter<TransferHistory> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        return writer;
    }
}
