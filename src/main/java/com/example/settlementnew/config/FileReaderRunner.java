package com.example.settlementnew.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileReaderRunner implements ApplicationRunner {

    private final FileLineReader fileLineReader;
    @Override
    public void run(ApplicationArguments args) {
        fileLineReader.start();
    }
}
