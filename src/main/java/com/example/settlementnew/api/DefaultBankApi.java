package com.example.settlementnew.api;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class DefaultBankApi implements BankApi {

    private final double ERROR_RATE = 0.001;
    private final double CRITICAL_ERROR_RATE = 0.0002;

    @Override
    public History transfer(String fromUsername, String toUsername, int price) throws Exception {
        double random = Math.random();
        if (random < CRITICAL_ERROR_RATE) {
            fatalError(toUsername, price);
        } else if (random < ERROR_RATE) {
            return reasonableError(fromUsername, toUsername, price);
        }
        return new History(true, fromUsername, toUsername, price, LocalDateTime.now());
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
