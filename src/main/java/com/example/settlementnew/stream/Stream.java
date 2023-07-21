package com.example.settlementnew.stream;

import com.example.settlementnew.service.DbResetService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
public class Stream {

    private final Job dailyJob;
    private final JobLauncher jobLauncher;
    private final ObjectMapper objectMapper;
    private final DbResetService dbResetService;

    @RabbitListener(queues = "batch-queue")
    public void receiveMessage(String message) throws JsonProcessingException {
        try {
            log.info("message={}", message);
            BatchConfiguration config = objectMapper.readValue(message, BatchConfiguration.class);
            switch (config.getMessageType()) {
                case "reset" -> dbResetService.reset();
                case "batch" -> startBatch(config);
            }
        } catch (Exception e) {
            log.error("Error occurred while processing message", e);
        }
    }

    private void startBatch(BatchConfiguration config) {
        try {
            log.info("config={}", config);
            JobParameters jobParameters = config.getJobParameters();


            // Check if the job is already running
            jobLauncher.run(dailyJob, jobParameters);
        } catch (JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException | JobRestartException e) {
            log.error("Error occurred while processing message", e);
        } catch (JobExecutionAlreadyRunningException e) {
            log.error("Job is already running", e);
        }
    }

    private void resetDataBase() {

    }


}
