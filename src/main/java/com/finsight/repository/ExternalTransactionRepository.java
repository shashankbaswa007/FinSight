package com.finsight.repository;

import com.finsight.model.ExternalTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ExternalTransactionRepository extends JpaRepository<ExternalTransaction, Long> {
    
    Optional<ExternalTransaction> findByExternalIdAndSourceAndUserId(
        String externalId, 
        String source, 
        Long userId
    );
    
    Page<ExternalTransaction> findByUserIdAndTransactionDateOrderByTransactionDateDesc(
        Long userId, 
        LocalDate date, 
        Pageable pageable
    );
    
    @Query("SELECT et FROM ExternalTransaction et WHERE et.user.id = :userId " +
           "AND et.transactionDate BETWEEN :startDate AND :endDate " +
           "AND et.source = :source ORDER BY et.transactionDate DESC")
    Page<ExternalTransaction> findByUserIdAndDateRangeAndSource(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("source") String source,
        Pageable pageable
    );
}
