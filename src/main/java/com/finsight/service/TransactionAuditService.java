package com.finsight.service;

import com.finsight.model.AuditAction;
import com.finsight.model.Transaction;
import com.finsight.model.TransactionAuditLog;
import com.finsight.repository.TransactionAuditLogRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service for recording an immutable audit trail of all transaction changes.
 * Every CREATE, UPDATE, and DELETE is logged with old/new values and the performer.
 */
@Service
public class TransactionAuditService {

    private final TransactionAuditLogRepository auditRepo;

    public TransactionAuditService(TransactionAuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    public void logCreate(Transaction tx, String performedBy) {
        TransactionAuditLog log = new TransactionAuditLog();
        log.setTransactionId(tx.getId());
        log.setUserId(tx.getUser().getId());
        log.setAction(AuditAction.CREATE);
        log.setNewAmount(tx.getAmount());
        log.setNewType(tx.getType().name());
        log.setNewCategory(tx.getCategory().getName());
        log.setNewDescription(tx.getDescription());
        log.setPerformedBy(performedBy);
        auditRepo.save(log);
    }

    public void logUpdate(Transaction oldTx, BigDecimal oldAmount, String oldType,
                           String oldCategory, String oldDescription,
                           Transaction newTx, String performedBy) {
        TransactionAuditLog log = new TransactionAuditLog();
        log.setTransactionId(newTx.getId());
        log.setUserId(newTx.getUser().getId());
        log.setAction(AuditAction.UPDATE);
        log.setOldAmount(oldAmount);
        log.setNewAmount(newTx.getAmount());
        log.setOldType(oldType);
        log.setNewType(newTx.getType().name());
        log.setOldCategory(oldCategory);
        log.setNewCategory(newTx.getCategory().getName());
        log.setOldDescription(oldDescription);
        log.setNewDescription(newTx.getDescription());
        log.setPerformedBy(performedBy);
        auditRepo.save(log);
    }

    public void logDelete(Transaction tx, String performedBy) {
        TransactionAuditLog log = new TransactionAuditLog();
        log.setTransactionId(tx.getId());
        log.setUserId(tx.getUser().getId());
        log.setAction(AuditAction.DELETE);
        log.setOldAmount(tx.getAmount());
        log.setOldType(tx.getType().name());
        log.setOldCategory(tx.getCategory().getName());
        log.setOldDescription(tx.getDescription());
        log.setPerformedBy(performedBy);
        auditRepo.save(log);
    }
}
