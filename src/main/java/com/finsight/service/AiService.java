package com.finsight.service;

import com.finsight.model.Category;
import com.finsight.repository.CategoryRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final CategoryRepository categoryRepository;

    public AiService(ChatClient.Builder chatClientBuilder, VectorStore vectorStore, CategoryRepository categoryRepository) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Auto-categorizes a transaction based on its description.
     * Cached to avoid redundant LLM calls for the same description.
     * Protected by Resilience4j circuit breaker to prevent cascade failures if Ollama is down.
     */
    @Cacheable(value = "ai_categories", key = "#description.toLowerCase().trim()", unless = "#result == null")
    @CircuitBreaker(name = "ollamaAi", fallbackMethod = "fallbackCategorize")
    public Category autoCategorize(String description) {
        List<Category> allCategories = categoryRepository.findAll();
        if (allCategories.isEmpty()) return null;

        String categoryNames = allCategories.stream()
                .map(Category::getName)
                .collect(Collectors.joining(", "));

        String prompt = String.format("You are a financial categorization assistant. " +
                "Given the transaction description '%s', choose the most appropriate category " +
                "from this exact list: [%s]. " +
                "Reply with ONLY the exact category name and nothing else.", 
                description, categoryNames);

        try {
            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            if (aiResponse == null) return null;
            
            String suggestedCategory = aiResponse.trim().replaceAll("[\"'.]", ""); // Clean up punctuation
            
            return allCategories.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(suggestedCategory))
                    .findFirst()
                    .orElse(getFallbackCategory(allCategories));
        } catch (Exception e) {
            log.error("Failed to auto-categorize transaction: {}", e.getMessage());
            throw e; // Triggers circuit breaker
        }
    }

    /**
     * Fallback method used if the circuit breaker is OPEN (Ollama is down or timing out).
     */
    public Category fallbackCategorize(String description, Throwable t) {
        log.warn("Circuit Breaker OPEN or LLM Failed. Using fallback rule-based categorization for '{}'. Cause: {}", 
                 description, t.getMessage());
        List<Category> allCategories = categoryRepository.findAll();
        
        String lowerDesc = description.toLowerCase();
        String matchedName = "Other";
        
        if (lowerDesc.contains("uber") || lowerDesc.contains("lyft")) matchedName = "Transport";
        else if (lowerDesc.contains("netflix") || lowerDesc.contains("spotify")) matchedName = "Entertainment";
        else if (lowerDesc.contains("walmart") || lowerDesc.contains("target")) matchedName = "Shopping";
        else if (lowerDesc.contains("salary") || lowerDesc.contains("payroll")) matchedName = "Salary";
        
        String finalMatchedName = matchedName;
        return allCategories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(finalMatchedName))
                .findFirst()
                .orElse(getFallbackCategory(allCategories));
    }

    private Category getFallbackCategory(List<Category> allCategories) {
        return allCategories.stream()
                .filter(c -> c.getName().equalsIgnoreCase("Other") || c.getName().equalsIgnoreCase("Uncategorized"))
                .findFirst()
                .orElse(allCategories.isEmpty() ? null : allCategories.get(0));
    }

    /**
     * RAG-based Financial Advisor chat response.
     */
    @CircuitBreaker(name = "ollamaAi", fallbackMethod = "fallbackFinancialAdvice")
    public String getFinancialAdvice(Long userId, String userMessage, String contextData) {
        // RAG Retrieval Phase
        List<Document> similarDocuments = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(userMessage)
                .topK(5)
                .filterExpression("userId == '" + userId + "'")
                .build()
        );
        
        String retrievedContext = similarDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n- ", "- ", ""));

        String systemPrompt = "You are FinSight's AI Financial Advisor. Be concise, helpful, and professional. " +
                "You have access to the user's current month summary and historical transaction context retrieved from their database. " +
                "Answer the user's questions strictly based on the provided context. If the context doesn't contain the answer, say you don't know.\n\n" +
                "=== CURRENT MONTH SUMMARY ===\n" + contextData + "\n\n" +
                "=== HISTORICAL TRANSACTION CONTEXT ===\n" + retrievedContext;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }

    public String fallbackFinancialAdvice(Long userId, String userMessage, String contextData, Throwable t) {
        return "I'm having trouble connecting to my AI brain (Ollama) right now. Please make sure the local Ollama service is running. " +
               "(Error: " + t.getMessage() + ")";
    }
}
