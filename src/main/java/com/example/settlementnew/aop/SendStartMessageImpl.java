package com.example.settlementnew.aop;

import com.example.settlementnew.config.WasWebSocketHandler;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class SendStartMessageImpl {

    private final WasWebSocketHandler wasWebSocketHandler;


    @Around("@annotation(com.example.settlementnew.aop.SendStartMessage)")
    public Object beforeJobTest(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        SendStartMessage sendStartMessage = methodSignature.getMethod().getAnnotation(SendStartMessage.class);
        String subject = sendStartMessage.title();
        String detail = sendStartMessage.detail();

        BatchStatusMessage batchStatusMessage = new BatchStatusMessage(subject, detail);

        wasWebSocketHandler.sendMessage(batchStatusMessage);
        return pjp.proceed();
    }

    @Data
    private static class BatchStatusMessage {
        private String type = "BATCH_STATUS";
        private String subject;
        private String detail;

        public BatchStatusMessage(String subject, String detail) {
            this.subject = subject;
            this.detail = detail;
        }
    }

}
