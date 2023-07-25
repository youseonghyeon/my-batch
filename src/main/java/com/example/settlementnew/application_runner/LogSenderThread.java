package com.example.settlementnew.application_runner;

import com.example.settlementnew.config.socket.WasWebSocketHandler;
import com.example.settlementnew.dto.socket_message.LogMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LogSenderThread extends Thread {

    private static final String LOG_FILE_PATH = "logs/application.log";
    private final WasWebSocketHandler wasWebSocketHandler;

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE_PATH))) {
            String line;
            while (true) {
                if ((line = reader.readLine()) != null) {
                    wasWebSocketHandler.sendMessage(new LogMessage(line));
                } else {
                    sleep(1000);
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
