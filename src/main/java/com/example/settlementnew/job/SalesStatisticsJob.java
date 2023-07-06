package com.example.settlementnew.job;

import com.example.settlementnew.api.MessageApi;
import com.example.settlementnew.repository.TransferHistoryRepository;
import com.example.settlementnew.service.SettlementService;
import com.example.settlementnew.service.TransferService;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
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
public class SalesStatisticsJob {


    private final JobRepository jobRepository;
    private final PlatformTransactionManager ptm;
    private final SettlementService settlementService;
    private final TransferService transferService;
    private final EntityManagerFactory emf;
    private final TransferHistoryRepository transferHistoryRepository;
    private final MessageApi messageApi;

    @Bean(name = "statisticsJob")
    public Job statisticsJob() {
     return new JobBuilder("statisticsJob", jobRepository)
             .start(statisticsStep(null))
             .build();
    }

    private Step statisticsStep(@Value("#{jobParameters[requestDate]") String requestDate) {
        return new StepBuilder("statisticsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 판매자 매출 데이터를 db에 삽입

                    return RepeatStatus.FINISHED;
                }, ptm)
                .build();


    }


}