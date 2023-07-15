package com.example.settlementnew.reader;

import com.example.settlementnew.entity.DailySettlement;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.item.database.JpaPagingItemReader;

public class SettlementReader extends JpaPagingItemReader<DailySettlement> {

    public SettlementReader(EntityManagerFactory emf, Integer chunkSize) {
        this.setQueryString("select ds from DailySettlement ds");
        this.setEntityManagerFactory(emf);
        this.setPageSize(chunkSize);
    }
}
