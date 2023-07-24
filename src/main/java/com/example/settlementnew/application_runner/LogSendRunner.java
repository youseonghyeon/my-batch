package com.example.settlementnew.application_runner;

import com.example.settlementnew.config.socket.LogSenderThread;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LogSendRunner implements ApplicationRunner {

    private final LogSenderThread logSenderThread;

    /**
     * Socket을 이용하여 로그 전송
     * @param args incoming application arguments
     */
    @Override
    public void run(ApplicationArguments args) {
        logSenderThread.start();
    }
}
