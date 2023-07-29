package com.example.settlementnew.application_runner;

import com.example.settlementnew.socket.SocketSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LogSenderThread extends Thread {

    private static final String LOG_FILE_PATH = "logs/application.log";
    private final SocketSender socketSender;

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE_PATH))) {
            String line;
            while (true) {
                if ((line = reader.readLine()) != null) {
                    socketSender.sendLogMessage(line);
                } else {
                    sleep(1000);
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
