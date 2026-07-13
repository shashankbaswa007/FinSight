package com.finsight.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserVectorStoreManager {

    private static final Logger log = LoggerFactory.getLogger(UserVectorStoreManager.class);
    private final EmbeddingModel embeddingModel;
    private String baseDir = "./data/vector-stores";
    private final Map<Long, VectorStore> storeCache = new ConcurrentHashMap<>();

    public UserVectorStoreManager(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        // Ensure directory exists
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Gets the vector store for a specific user. Lazily loads from disk if it exists.
     */
    public VectorStore getVectorStore(Long userId) {
        return storeCache.computeIfAbsent(userId, id -> {
            log.info("Initializing vector store for user {}", id);
            SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();
            File file = new File(baseDir, "user-" + id + "-vector-store.json");
            if (file.exists() && file.length() > 0) {
                try {
                    store.load(file);
                    log.info("Loaded existing vector store for user {}", id);
                } catch (Exception e) {
                    log.warn("Failed to load vector store for user {}: {}", id, e.getMessage());
                }
            }
            return store;
        });
    }

    /**
     * Saves the user's vector store to disk.
     */
    public void save(Long userId) {
        VectorStore store = storeCache.get(userId);
        if (store instanceof SimpleVectorStore) {
            File file = new File(baseDir, "user-" + userId + "-vector-store.json");
            ((SimpleVectorStore) store).save(file);
            log.info("Persisted vector store for user {} to {}", userId, file.getAbsolutePath());
        }
    }
}
