package com.example.settlementnew.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SalesStatisticsJob {


    private final JobRepository jobRepository;
    private final PlatformTransactionManager ptm;

    @Bean(name = "statisticsJob")
    public Job statisticsJob() {
        return new JobBuilder("statisticsJob", jobRepository)
                .start(statisticsStep())
                .build();
    }

    private Step statisticsStep() {
        return new StepBuilder("statisticsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 판매자 매출 데이터를 db에 삽입

                    return RepeatStatus.FINISHED;
                }, ptm)
                .build();


    }


}
