package com.example.settlementnew.listener;

import com.example.settlementnew.dto.RabbitMessage;
import com.example.settlementnew.service.DbResetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchListener {

    private final Job dailyJob;
    private final JobLauncher jobLauncher;
    private final ObjectMapper objectMapper;
    private final DbResetService dbResetService;

    @RabbitListener(queues = "batch-queue")
    public void receiveMessage(String message) {
        try {
            log.info("Message Listen : {}", message);
            RabbitMessage rabbitMessage = objectMapper.readValue(message, RabbitMessage.class);
            switch (rabbitMessage.getMessageType()) {

                case "batch" -> startBatch(rabbitMessage);

                case "reset" -> dbResetService.reset();

            }
        } catch (Exception e) {
            log.error("Error occurred while processing message", e);
        }
    }

    private void startBatch(RabbitMessage rabbitMessage) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        JobParameters jobParameters = rabbitMessage.convertJobParameters();
        jobLauncher.run(dailyJob, jobParameters);
    }


}
