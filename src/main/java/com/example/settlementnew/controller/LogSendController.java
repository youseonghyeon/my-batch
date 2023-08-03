package com.example.settlementnew.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

@RestController
public class LogSendController {

    @Value("${logging.file.path}")
    private String LOG_FILE;

    @GetMapping("/logging")
    public String readLogfile() {
        Queue<String> logLines = readLogLines(LOG_FILE);
        return concatenateLogLines(logLines);
    }

    private Queue<String> readLogLines(String filePath) {
        Queue<String> logLines = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = truncateLineIfNecessary(line);
                logLines.add(line);

                // 최대 150줄만 보냄
                if (logLines.size() > 150) {
                    logLines.poll();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return logLines;
    }

    private String truncateLineIfNecessary(String line) {
        if (line.length() > 500) {
            // 500자 이상은 500자로 잘라서 보냄
            return line.substring(0, 500);
        } else {
            return line;
        }
    }

    private String concatenateLogLines(Queue<String> logLines) {
        StringBuilder contentBuilder = new StringBuilder();

        for (String logLine : logLines) {
            contentBuilder.append(logLine).append("\n");
        }

        return contentBuilder.toString();
    }
}

