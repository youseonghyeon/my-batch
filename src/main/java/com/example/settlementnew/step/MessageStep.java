package com.example.settlementnew.step;

import com.example.settlementnew.aop.SendStartMessage;
import com.example.settlementnew.dto.DefaultMessageMetadata;
import com.example.settlementnew.entity.DailySettlement;
import com.example.settlementnew.entity.TransferHistory;
import com.example.settlementnew.entity.TransferStatus;
import com.example.settlementnew.api.MessageBroker;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MessageStep {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager ptm;
    private final EntityManagerFactory emf;
    private final MessageBroker messageBroker;

    private static final String SELECT_TRANSFER_HISTORY =
            "SELECT th FROM TransferHistory th " +
                    "JOIN FETCH th.dailySettlement ds " +
                    "WHERE th.status IN ('COMPLETED', 'FAILED')";

    @Bean("sendMessageStep")
    @JobScope
    @SendStartMessage(title = "메일 전송", detail = "메일 전송을 시작합니다.")
    public Step sendMessageStep(@Value("#{jobParameters[chunkSize]}") Integer chunkSize) {
        log.info("==================== 5단계 결과 메일 전송 ====================");
        return new StepBuilder("sendMessageStep", jobRepository)
                .<TransferHistory, TransferHistory>chunk(chunkSize, ptm)
                .reader(transferHistoryReader(chunkSize))
                .writer(sendMail())
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<TransferHistory> transferHistoryReader(@Value("#{jobParameters[chunkSize]}") Integer chunkSize) {
        JpaPagingItemReader<TransferHistory> reader = new JpaPagingItemReader<>();
        reader.setQueryString(SELECT_TRANSFER_HISTORY);
        reader.setEntityManagerFactory(emf);
        reader.setPageSize(chunkSize);
        return reader;
    }


    @Bean
    public ItemWriter<TransferHistory> sendMail() {
        return chunk -> {
            for (TransferHistory history : chunk) {
                DailySettlement dailySettlement = history.getDailySettlement();

                String username = dailySettlement.getUsername();
                DefaultMessageMetadata metadata = new DefaultMessageMetadata("admin", Collections.singletonList(username));

                if (TransferStatus.COMPLETED.equals(history.getStatus())) {
                    messageBroker.sendMessage("송금 완료.", metadata);
                } else if (TransferStatus.FAILED.equals(history.getStatus())) {
                    messageBroker.sendMessage("송금 실패.", metadata);
                }
            }
        };
    }

}
