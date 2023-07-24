package com.example.settlementnew.api;

import com.example.settlementnew.dto.MessageMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageBroker {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;


    public void sendMessage(Object message, MessageMetadata metadata) {
        try {
            rabbitTemplate.convertAndSend("message-handler", "#", objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


}
