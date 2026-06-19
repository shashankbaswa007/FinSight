package com.finsight.service;

import com.finsight.model.Transaction;
import com.finsight.repository.TransactionRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagDocumentIngestionService {

    private final VectorStore vectorStore;
    private final TransactionRepository transactionRepository;

    @Value("${spring.ai.vectorstore.simple.path:vector-store.json}")
    private String vectorStorePath;

    public RagDocumentIngestionService(VectorStore vectorStore, TransactionRepository transactionRepository) {
        this.vectorStore = vectorStore;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Rebuilds the RAG context for a user by embedding their recent transactions.
     * In a production system, this would be incremental (event-driven).
     */
    @Async("aiTaskExecutor")
    @Transactional(readOnly = true)
    public void ingestUserData(Long userId) {
        // Fetch recent transactions
        List<Transaction> recentTransactions = transactionRepository.findTop100ByUserIdOrderByDateDesc(userId);
        
        if (recentTransactions.isEmpty()) return;

        List<Document> documents = recentTransactions.stream().map(tx -> {
            String content = String.format("Transaction on %s: Spent %s on %s (Category: %s). Description: %s",
                    tx.getDate(), tx.getAmount(), tx.getCategory() != null ? tx.getCategory().getName() : "Uncategorized",
                    tx.getCategory() != null ? tx.getCategory().getType().name() : "N/A",
                    tx.getDescription());
                    
            return new Document(content, Map.of(
                    "userId", userId.toString(),
                    "transactionId", tx.getId().toString(),
                    "type", "transaction"
            ));
        }).collect(Collectors.toList());

        // Add to vector store
        vectorStore.accept(documents);
        
        // Persist to disk if using SimpleVectorStore
        if (vectorStore instanceof SimpleVectorStore) {
            ((SimpleVectorStore) vectorStore).save(new File(vectorStorePath));
        }
    }
}
