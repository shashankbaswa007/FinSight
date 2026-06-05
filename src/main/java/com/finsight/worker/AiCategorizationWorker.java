package com.finsight.worker;

import com.finsight.model.Category;
import com.finsight.model.Transaction;
import com.finsight.repository.TransactionRepository;
import com.finsight.service.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiCategorizationWorker {

    private static final Logger log = LoggerFactory.getLogger(AiCategorizationWorker.class);

    private final AiService aiService;
    private final TransactionRepository transactionRepository;

    public AiCategorizationWorker(AiService aiService, TransactionRepository transactionRepository) {
        this.aiService = aiService;
        this.transactionRepository = transactionRepository;
    }

    @Async("aiTaskExecutor")
    @Transactional
    public void categorizeTransactionAsync(Long transactionId, String description) {
        try {
            log.info("Starting background categorization for transaction {} with description '{}'", transactionId, description);
            Category category = aiService.autoCategorize(description);
            if (category != null) {
                transactionRepository.findById(transactionId).ifPresent(tx -> {
                    tx.setCategory(category);
                    transactionRepository.save(tx);
                    log.info("Successfully categorized transaction {} as {}", transactionId, category.getName());
                });
            } else {
                log.warn("AI failed to determine category for transaction {}", transactionId);
            }
        } catch (Exception e) {
            log.error("Error during background categorization of transaction {}", transactionId, e);
        }
    }
}
