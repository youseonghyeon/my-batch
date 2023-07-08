package com.example.settlementnew.job;

import com.example.settlementnew.api.History;
import com.example.settlementnew.api.MessageApi;
import com.example.settlementnew.entity.DailySettlement;
import com.example.settlementnew.entity.TransferHistory;
import com.example.settlementnew.entity.TransferStatus;
import com.example.settlementnew.processor.ReTransferProcessor;
import com.example.settlementnew.processor.TransferProcessor;
import com.example.settlementnew.reader.SettlementReader;
import com.example.settlementnew.repository.TransferHistoryRepository;
import com.example.settlementnew.service.SettlementService;
import com.example.settlementnew.service.TransferService;
import com.example.settlementnew.step.MessageStep;
import com.example.settlementnew.step.MockDataInsertStep;
import com.example.settlementnew.step.SettlementStep;
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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DailySettlementJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager ptm;
    private final EntityManagerFactory emf;


    private final TransferHistoryRepository transferHistoryRepository;
    private final MessageStep messageStep;
    private final SettlementStep settlementStep;
    private final MockDataInsertStep mockDataInsertStep;
    private final TransferProcessor transferProcessor;
    private final ReTransferProcessor reTransferProcessor;


    @Bean(name = "dailyJob")
    public Job dailyJob() {
        return new JobBuilder("dailyJob", jobRepository)
                .start(mockDataInsertStep.insertMockSettlementStep(null)) // mock 데이터 삽입
                .next(settlementStep.dailySettlementStep(null)) // 일일 정산
                .next(transferSettlementStep(null)) // 정산 이체
                .next(reTransferSettlementStep(null)) // 정산 이체 실패시 재이체
                .next(messageStep.sendMessageStep(null)) // 정산 이체 결과 메시지 전송
                .build();
    }


    @Bean(name = "transferSettlementStep")
    @JobScope
    public Step transferSettlementStep(@Value("#{jobParameters[chunkSize]}") Integer chunkSize) {
        log.info("==================== 3단계 송금 시작 ====================");
        return new StepBuilder("transferSettlementStep", jobRepository)
                .<DailySettlement, TransferHistory>chunk(chunkSize, ptm)
                .reader(new SettlementReader(emf))
                .processor(transferProcessor)
                .writer(settlementWriter())
                .build();

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
                .processor(reTransferProcessor)
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
    public JpaItemWriter<TransferHistory> reSettlementWriter() {
        JpaItemWriter<TransferHistory> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(emf);
        return writer;
    }
}
