package com.example.settlementnew.config;

import com.example.settlementnew.api.BankApi;
import com.example.settlementnew.api.DefaultBankApi;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDateTime.class, jsonSerializer());
        objectMapper.registerModule(module);
        return objectMapper;
    }

    @Bean
    public JsonSerializer<LocalDateTime> jsonSerializer() {
        return new JsonSerializer<>() {
            @Override
            public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                String formattedDateTime = value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                gen.writeString(formattedDateTime);
            }
        };
    }

    @Bean
    public BankApi bankApi() {
        return new DefaultBankApi();
    }
}
