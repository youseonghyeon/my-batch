package com.example.settlementnew.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class BankApi {

    private final double ERROR_RATE = 0.1;
    private final double CRITICAL_ERROR_RATE = 0.02;

    Map<String, String> map = new HashMap<>();

    public History transfer(String fromUsername, String toUsername, int price) throws Exception {
        double random = Math.random();
        if (random < CRITICAL_ERROR_RATE) {
            fatalError(toUsername, price);
        } else if (random < ERROR_RATE) {
            return reasonableError(fromUsername, toUsername, price);
        }
        duplicatedTransferCheck(toUsername);
        return new History(true, fromUsername, toUsername, price, LocalDateTime.now());
    }

    private void duplicatedTransferCheck(String to) {
        String s = map.get(to);
        if (s != null) {
            log.error("송금 중 중복 발생 to: {}", to);
        } else {
            map.put(to, "");
        }
    }

    private static History reasonableError(String from, String to, int price) {
        log.warn("송금 중 에러 발생 to: {}, amount: {}", to, price);
        return new History(false, from, to, price);
    }

    private static void fatalError(String to, int price) throws Exception {
        log.error("송금 중 치명적 에러 발생 to: {}, amount: {}", to, price);
        throw new Exception("치명적 에러 발생");
    }
}
