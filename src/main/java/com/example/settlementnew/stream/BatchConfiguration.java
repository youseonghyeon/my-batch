package com.example.settlementnew.stream;

import lombok.Data;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import java.io.Serializable;
import java.util.UUID;

@Data
public class BatchConfiguration implements Serializable {
    private String messageType;
    private int mockSize;
    private int chunkSize;
    private String targetDate;

    public JobParameters getJobParameters() {
        return new JobParametersBuilder()
                .addString("random", UUID.randomUUID().toString())
                .addString("mockSize", String.valueOf(mockSize))
                .addString("chunkSize", String.valueOf(chunkSize))
                .addString("targetDate", targetDate)
                .toJobParameters();
    }
}

