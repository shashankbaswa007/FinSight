package com.finsight.repository;

import com.finsight.model.ReconciliationBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReconciliationBatchRepository extends JpaRepository<ReconciliationBatch, Long> {
    
    Optional<ReconciliationBatch> findByUserIdAndBatchDate(Long userId, LocalDate batchDate);
    
    Page<ReconciliationBatch> findByUserIdOrderByBatchDateDesc(Long userId, Pageable pageable);
    
    Page<ReconciliationBatch> findByUserIdAndStatusOrderByBatchDateDesc(
        Long userId, 
        ReconciliationBatch.ReconciliationStatus status, 
        Pageable pageable
    );
    
    @Query("SELECT rb FROM ReconciliationBatch rb WHERE rb.user.id = :userId " +
           "AND rb.batchDate BETWEEN :startDate AND :endDate " +
           "ORDER BY rb.batchDate DESC")
    Page<ReconciliationBatch> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    List<ReconciliationBatch> findByUserIdAndBatchDateBetweenOrderByBatchDateAsc(
            Long userId,
            LocalDate startDate,
            LocalDate endDate
    );
}
