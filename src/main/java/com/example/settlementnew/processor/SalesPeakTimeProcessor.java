package com.example.settlementnew.processor;

import com.example.settlementnew.entity.Settlement;
import org.springframework.batch.item.ItemProcessor;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class SalesPeakTimeProcessor implements ItemProcessor<Settlement, Map<LocalTime, Integer>> {


    /**
     * SELECT DATE_FORMAT(sales_datetime, '%H:%i') AS sales_time, userId, SUM(sales_amount) AS total_sales
     * FROM sales_table
     * GROUP BY DATE_FORMAT(sales_datetime, '%H:%i'), userId;
     */
    @Override
    public Map<LocalTime, Integer> process(Settlement settlement) {

        String sql = "SELECT DATE_FORMAT(sales_datetime, '%H:%i') AS sales_time, userId, SUM(sales_amount) AS total_sales FROM sales_table " +
                "GROUP BY DATE_FORMAT(sales_datetime, '%H:%i'), userId";

        // 시간대별 판매량을 저장할 맵 초기화
        Map<LocalTime, Integer> salesByTime = new HashMap<>();

        // 판매 일시에서 시간대 추출
        LocalTime salesTime = settlement.getCreatedAt().toLocalTime();

        // 시간대별 판매량 계산
        salesByTime.put(salesTime, salesByTime.getOrDefault(salesTime, 0) + 1);

        return salesByTime;
    }
}
