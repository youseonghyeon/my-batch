package com.example.settlementnew.job;

import com.example.settlementnew.api.MessageApi;
import com.example.settlementnew.entity.DailySettlement;
import com.example.settlementnew.entity.TransferHistory;
import com.example.settlementnew.entity.TransferStatus;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ReportGenerationJob {

    @Value("${chunkSize:1000}")
    private int chunkSize;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager ptm;
    private final EntityManagerFactory emf;
    private final MessageApi messageApi;


    @Bean("reportJob")
    public Job reportJob() {
        return new JobBuilder("reportJob", jobRepository)
                .start(reportStep())
                .build();
    }

    @Bean("reportStep")
    public Step reportStep() {
        return new StepBuilder("reportStep", jobRepository)
                .<DailySettlement, DailySettlement>chunk(chunkSize, ptm)
                .reader(dailySettlementReader(null))
                .writer(sendMail(null))
                .build();
    }

    @Bean("dailySettlementReader")
    public JpaPagingItemReader<DailySettlement> dailySettlementReader(@Value("#{jobParameters[targetDate]}") String targetDate) {
        JpaPagingItemReader<DailySettlement> reader = new JpaPagingItemReader<>();
        reader.setQueryString("select ds from DailySettlement ds join fetch ds.transferHistory where ds.createdAt = :targetDate");
        reader.setParameterValues(Collections.singletonMap("targetDate", targetDate)); // string -> timestamp 자동으로 바뀌나?
        reader.setEntityManagerFactory(emf);
        return reader;
    }


    @Bean
    public ItemWriter<DailySettlement> sendMail(@Value("#{jobParameters[sendTime]}") String MailSendTime) {
        return chunk -> {
            for (DailySettlement item : chunk) {
                List<TransferHistory> historyList = item.getTransferHistory();
                List<TransferHistory> successCase = historyList.stream()
                        .filter(transfer -> transfer.getStatus().equals(TransferStatus.COMPLETED))
                        .toList();
                String username = item.getUsername();


                if (isMultifulTranfer(successCase)) {
                    // 중복 송금 (관리자에게 확인 메일 전송)
                    messageApi.send("admin", "중복 송금이 발생했습니다." + historyList);

//                    new Report(item.getTotalPrice(), 12, item.getCreatedAt(), LocalDateTime.parse(MailSendTime));

                } else if (successCase.size() == 1) {
                    // 정상 송금 / 재송금 성공 시 (메일 전송 처리)
                    TransferHistory history = successCase.get(0);
                    int amount = history.getAmount();
                    LocalDateTime sendTime = history.getCreatedAt();
                    messageApi.send(username, "송금이 완료되었습니다. 송금 금액: " + amount + ", 송금 시간: " + sendTime);

//                    new Report(item.getTotalPrice(), 12, item.getCreatedAt(), LocalDateTime.parse(MailSendTime));

                } else {
                    // 송금 실패 (정산 실패 메시지 전송 처리 & 관리자에게 확인 메일 작성)
                    messageApi.send(username, "송금이 실패했습니다...");
                    messageApi.send("admin", "송금이 실패했습니다." + historyList);

//                    new Report(item.getTotalPrice(), 12, item.getCreatedAt(), LocalDateTime.parse(MailSendTime));
                }
            }
        };
    }

    private static boolean isMultifulTranfer(List<TransferHistory> successCase) {
        return successCase.size() > 1;
    }

}
