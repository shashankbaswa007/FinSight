package com.finsight.service;

import com.finsight.worker.AiCategorizationWorker;
import org.springframework.stereotype.Service;

@Service
public class AsyncCategorizationEventPublisher implements CategorizationEventPublisher {

    private final AiCategorizationWorker worker;

    public AsyncCategorizationEventPublisher(AiCategorizationWorker worker) {
        this.worker = worker;
    }

    @Override
    public void publishCategorizationEvent(Long transactionId, String description) {
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        worker.categorizeTransactionAsync(transactionId, description);
                    }
                }
            );
        } else {
            // Fallback if not running within an active transaction
            worker.categorizeTransactionAsync(transactionId, description);
        }
    }
}
