package com.example.settlementnew.step;

import com.example.settlementnew.entity.DailySettlement;
import com.example.settlementnew.entity.TransferHistory;
import com.example.settlementnew.entity.TransferStatus;
import com.example.settlementnew.reader.SettlementReader;
import com.example.settlementnew.service.MessageBroker;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MessageStep {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager ptm;
    private final EntityManagerFactory emf;
    private final MessageBroker messageBroker;

    @Bean("sendMessageStep")
    @JobScope
    public Step sendMessageStep(@Value("#{jobParameters[chunkSize]}") Integer chunkSize) {
        return new StepBuilder("sendMessageStep", jobRepository)
                .<DailySettlement, DailySettlement>chunk(chunkSize, ptm)
                .reader(new SettlementReader(emf))
                .writer(sendMail())
                .build();
    }



    @Bean
    public ItemWriter<DailySettlement> sendMail() {
        return chunk -> {
            for (DailySettlement item : chunk) {
                List<TransferHistory> historyList = item.getTransferHistory();
                List<TransferHistory> successCase = historyList.stream()
                        .filter(transfer -> transfer.getStatus().equals(TransferStatus.COMPLETED))
                        .toList();
                String username = item.getUsername();
                messageBroker.sendMessage(item);

//                if (isMultifulTranfer(successCase)) {
//                    // 중복 송금 (관리자에게 확인 메일 전송)
//                    sendMessage(historyList);
////                    messageApi.send("admin", "중복 송금이 발생했습니다." + historyList);
//
//                } else if (successCase.size() == 1) {
//                    // 정상 송금 / 재송금 성공 시 (메일 전송 처리)
//                    TransferHistory history = successCase.get(0);
//                    int amount = history.getAmount();
//                    LocalDateTime sendTime = history.getCreatedAt();
//                    sendMessage(historyList);
////                    messageApi.send(username, "송금이 완료되었습니다. 송금 금액: " + amount + ", 송금 시간: " + sendTime);
//
////                    new Report(item.getTotalPrice(), 12, item.getCreatedAt(), LocalDateTime.parse(MailSendTime));
//
//                } else {
//                    // 송금 실패 (정산 실패 메시지 전송 처리 & 관리자에게 확인 메일 작성)
//                    sendMessage(historyList);
////                    messageApi.send(username, "송금이 실패했습니다...");
////                    messageApi.send("admin", "송금이 실패했습니다." + historyList);
//
////                    new Report(item.getTotalPrice(), 12, item.getCreatedAt(), LocalDateTime.parse(MailSendTime));
//                }
            }
        };
    }

    private static boolean isMultifulTranfer(List<TransferHistory> successCase) {
        return successCase.size() > 1;
    }

}
