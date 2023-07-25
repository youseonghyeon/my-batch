package com.example.settlementnew.api;

public interface BankApi {

    History transfer(String fromUsername, String toUsername, long price) throws Exception;
}
