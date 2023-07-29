package com.example.settlementnew.aop;

import com.example.settlementnew.socket.SocketSender;
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

    private final SocketSender socketSender;


    @Around("@annotation(com.example.settlementnew.aop.SendStartMessage)")
    public Object beforeJobTest(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        SendStartMessage sendStartMessage = methodSignature.getMethod().getAnnotation(SendStartMessage.class);
        String subject = sendStartMessage.title();
        String detail = sendStartMessage.detail();
        String img = sendStartMessage.img();

        socketSender.sendStatus(subject, detail, img);
        try {
            return pjp.proceed();
        } catch (Exception e) {
            socketSender.sendStatus(subject, "중간에 ERROR가 발생했습니다.");
            throw e;
        }
    }


}
