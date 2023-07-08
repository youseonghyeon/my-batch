package com.example.settlementnew.reader;

import com.example.settlementnew.entity.DailySettlement;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public class SettlementReader extends JpaPagingItemReader<DailySettlement> {

    public SettlementReader(EntityManagerFactory emf) {
        this.setQueryString("select ds from DailySettlement ds");
        this.setEntityManagerFactory(emf);
        this.setPageSize(100);
    }
}
