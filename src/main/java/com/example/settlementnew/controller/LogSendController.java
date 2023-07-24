package com.example.settlementnew.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

@RestController
public class LogSendController {

    private static final String LOG_FILE = "logs/application.log";

    @GetMapping("/logging")
    public String readLogfile() {
        return readLog(LOG_FILE);
    }

    private String readLog(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        Queue<String> Q = new LinkedList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() > 500) {
                    // 500자 이상은 500자로 잘라서 보냄
                    line = line.substring(0, 500);
                }
                Q.add(line);
                // 최대 50줄만 보냄
                if (Q.size() > 150) {
                    Q.poll();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String s : Q) {
            contentBuilder.append(s)
                    .append("\n");
        }

        return contentBuilder.toString();
    }
}
