package com.finsight.repository;

import com.finsight.model.TransactionMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionMatchRepository extends JpaRepository<TransactionMatch, Long> {
    
    List<TransactionMatch> findByReconciliationBatchId(Long batchId);
    
    List<TransactionMatch> findByReconciliationBatchIdAndMatchStatus(
        Long batchId, 
        TransactionMatch.MatchStatus matchStatus
    );
    
    @Query("SELECT tm FROM TransactionMatch tm WHERE tm.reconciliationBatch.id = :batchId " +
           "AND tm.matchStatus = :status")
    List<TransactionMatch> findMatchesByBatchAndStatus(
        @Param("batchId") Long batchId,
        @Param("status") TransactionMatch.MatchStatus status
    );
    
    long countByReconciliationBatchIdAndMatchStatus(
        Long batchId, 
        TransactionMatch.MatchStatus matchStatus
    );
}
