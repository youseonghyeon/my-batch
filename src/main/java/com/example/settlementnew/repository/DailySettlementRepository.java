package com.example.settlementnew.repository;

import com.example.settlementnew.entity.DailySettlement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailySettlementRepository extends JpaRepository<DailySettlement, Long> {
}
