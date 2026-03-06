package com.finsight.repository;

import com.finsight.model.TransactionAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionAuditLogRepository extends JpaRepository<TransactionAuditLog, Long> {

    List<TransactionAuditLog> findByTransactionIdOrderByPerformedAtDesc(Long transactionId);

    List<TransactionAuditLog> findByUserIdOrderByPerformedAtDesc(Long userId);
}
