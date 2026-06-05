package com.finsight.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.File;

@Configuration
public class RagConfig {

    @Value("${spring.ai.vectorstore.simple.path:vector-store.json}")
    private String vectorStorePath;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        File file = new File(vectorStorePath);
        if (file.exists() && file.length() > 0) {
            try {
                simpleVectorStore.load(file);
            } catch (Exception e) {
                // Ignore load error for empty/corrupted file
            }
        }
        return simpleVectorStore;
    }
}
