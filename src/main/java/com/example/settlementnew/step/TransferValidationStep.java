package com.example.settlementnew.step;

import com.example.settlementnew.entity.DailySettlement;
import com.example.settlementnew.entity.TransferHistory;
import com.example.settlementnew.entity.TransferStatus;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TransferValidationStep {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager ptm;
    private final EntityManagerFactory emf;

//    @Bean(name = "validationStep")
    public Step validationStep() {
        log.info("==================== 6단계 전송 결과 검증 ====================");
        return new StepBuilder("validationStep", jobRepository)
                .<DailySettlement, DailySettlement>chunk(100, ptm)
                .reader(reader(null))
                .writer(dsList -> {
                    for (DailySettlement ds : dsList) {
                        List<TransferHistory> thList = ds.getTransferHistory();

                        long successCount = thList.stream().filter(th -> TransferStatus.COMPLETED.equals(th.getStatus())).count();

                        if (successCount > 1) {
                            log.error("중복 전송");
                        } else if (successCount < 1) {
                            log.warn("전송 실패");
                        }

                        // TODO 전송 결과에 대한 처리
                    }
                })
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<DailySettlement> reader(@Value("#{jobParameters[targetDate]}") String targetDate) {
        JpaPagingItemReader<DailySettlement> reader = new JpaPagingItemReader<>();
        reader.setQueryString("select ds from DailySettlement ds");
        reader.setPageSize(100);
        reader.setEntityManagerFactory(emf);
        return reader;
    }
}
