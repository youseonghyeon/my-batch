package com.example.settlementnew.api;

public interface BankApi {

    History transfer(String fromUsername, String toUsername, int price) throws Exception;
}
