package com.example.settlementnew.repository;

import com.example.settlementnew.entity.DailySettlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface DailySettlementRepository extends JpaRepository<DailySettlement, Long> {

    @Query("select ds from DailySettlement ds join fetch ds.transferHistory th where ds.createdAt = :createdAt")
    List<DailySettlement> findAllByCreatedAt(LocalDateTime createdAt);
}
