package com.example.settlementnew.service;

import com.example.settlementnew.api.BankApi;
import com.example.settlementnew.api.History;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final BankApi bankApi;


    public History transfer(String to, long price) {
        try {
            return bankApi.transfer("myJob", to, price);
        } catch (Exception e) {
            return new History(false, "myJob", to, price);
        }
    }


}
