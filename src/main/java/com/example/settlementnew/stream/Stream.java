package com.example.settlementnew.stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class Stream {

    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "batch-queue")
    public void receiveMessage(String message) throws JsonProcessingException {
        BatchConfiguration config = objectMapper.readValue(message, BatchConfiguration.class);
        //TODO Batch 실행 시켜 주세요
        log.info("config={}", config);
    }


}
