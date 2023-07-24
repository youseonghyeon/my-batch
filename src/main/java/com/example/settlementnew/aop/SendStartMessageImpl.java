package com.example.settlementnew.aop;

import com.example.settlementnew.dto.socket_message.SocketMessage;
import com.example.settlementnew.config.WasWebSocketHandler;
import com.example.settlementnew.dto.socket_message.StatusMessage;
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

        SocketMessage socketMessage = new StatusMessage(subject, detail);

        wasWebSocketHandler.sendMessage(socketMessage);
        try {
            return pjp.proceed();
        } catch (Exception e) {
            SocketMessage errorMessage = new StatusMessage(subject, "중간에 ERROR가 발생했습니다.");
            wasWebSocketHandler.sendMessage(errorMessage);
            throw e;
        }
    }


}
