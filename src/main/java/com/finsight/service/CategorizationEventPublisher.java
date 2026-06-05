package com.finsight.service;

/**
 * Abstraction for publishing categorization events.
 * Currently uses Spring @Async, but can be swapped with a Kafka implementation later.
 */
public interface CategorizationEventPublisher {
    void publishCategorizationEvent(Long transactionId, String description);
}
