package com.finsight.service;

import com.finsight.model.Category;
import com.finsight.model.TransactionType;
import com.finsight.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Regression test suite for the AI pipeline.
 * Tests prompt construction, categorization logic, RAG retrieval pipeline,
 * circuit breaker fallbacks, and response formatting — all without requiring
 * a running Ollama instance.
 */
@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock private ChatClient.Builder chatClientBuilder;
    @Mock private UserVectorStoreManager userVectorStoreManager;
    @Mock private CategoryRepository categoryRepository;
    @Mock private VectorStore vectorStore;

    private ChatClient chatClient;

    private AiService aiService;

    private List<Category> testCategories;

    @BeforeEach
    void setUp() {
        chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        when(chatClientBuilder.build()).thenReturn(chatClient);
        aiService = new AiService(chatClientBuilder, userVectorStoreManager, categoryRepository);

        // Set up test categories
        Category food = new Category();
        food.setId(1L);
        food.setName("Food");
        food.setType(TransactionType.EXPENSE);

        Category transport = new Category();
        transport.setId(2L);
        transport.setName("Transport");
        transport.setType(TransactionType.EXPENSE);

        Category salary = new Category();
        salary.setId(3L);
        salary.setName("Salary");
        salary.setType(TransactionType.INCOME);

        Category other = new Category();
        other.setId(4L);
        other.setName("Other");
        other.setType(TransactionType.EXPENSE);

        testCategories = List.of(food, transport, salary, other);
    }

    @Nested
    @DisplayName("Auto-Categorization")
    class AutoCategorization {

        @Test
        @DisplayName("Should return matching category when LLM responds with valid name")
        void shouldMatchValidCategory() {
            when(categoryRepository.findAll()).thenReturn(testCategories);
            when(chatClient.prompt().user(anyString()).call().content()).thenReturn("Food");

            Category result = aiService.autoCategorize("Grocery store purchase");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Food");
        }

        @Test
        @DisplayName("Should fall back to 'Other' when LLM returns unrecognized category")
        void shouldFallbackOnUnrecognized() {
            when(categoryRepository.findAll()).thenReturn(testCategories);
            when(chatClient.prompt().user(anyString()).call().content()).thenReturn("Groceries");  // Not in category list

            Category result = aiService.autoCategorize("Whole Foods purchase");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Other");
        }

        @Test
        @DisplayName("Should handle null LLM response gracefully")
        void shouldHandleNullResponse() {
            when(categoryRepository.findAll()).thenReturn(testCategories);
            when(chatClient.prompt().user(anyString()).call().content()).thenReturn(null);

            Category result = aiService.autoCategorize("Unknown item");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should return null when no categories exist")
        void shouldReturnNullWhenNoCategoriesExist() {
            when(categoryRepository.findAll()).thenReturn(List.of());

            Category result = aiService.autoCategorize("Some purchase");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should clean up punctuation from LLM response")
        void shouldCleanupPunctuation() {
            when(categoryRepository.findAll()).thenReturn(testCategories);
            when(chatClient.prompt().user(anyString()).call().content()).thenReturn("\"Transport.\"");  // Quotes + period

            Category result = aiService.autoCategorize("Uber ride");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Transport");
        }
    }

    @Nested
    @DisplayName("Circuit Breaker Fallback")
    class CircuitBreakerFallback {

        @Test
        @DisplayName("Should use rule-based fallback when circuit breaker opens")
        void shouldFallbackToRuleBased() {
            when(categoryRepository.findAll()).thenReturn(testCategories);

            Category result = aiService.fallbackCategorize("uber ride downtown", new RuntimeException("Connection refused"));

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Transport");
        }

        @Test
        @DisplayName("Should categorize Netflix as Entertainment in fallback")
        void shouldMatchNetflixToEntertainment() {
            Category entertainment = new Category();
            entertainment.setId(5L);
            entertainment.setName("Entertainment");
            entertainment.setType(TransactionType.EXPENSE);
            testCategories = List.of(testCategories.get(0), entertainment, testCategories.get(3));

            when(categoryRepository.findAll()).thenReturn(testCategories);

            Category result = aiService.fallbackCategorize("netflix subscription", new RuntimeException("Timeout"));

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Entertainment");
        }

        @Test
        @DisplayName("Should return 'Other' for unmatched descriptions in fallback")
        void shouldReturnOtherForUnmatched() {
            when(categoryRepository.findAll()).thenReturn(testCategories);

            Category result = aiService.fallbackCategorize("random purchase xyz", new RuntimeException("Error"));

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Other");
        }

        @Test
        @DisplayName("Financial advice fallback should return user-friendly error message")
        void shouldReturnFriendlyError() {
            String result = aiService.fallbackFinancialAdvice(1L, "What are my expenses?", "context", new RuntimeException("Ollama down"));

            assertThat(result).contains("trouble connecting");
            assertThat(result).contains("Ollama");
        }
    }

    @Nested
    @DisplayName("System Prompt Quality")
    class SystemPromptQuality {

        @Test
        @DisplayName("Should construct system prompt containing all critical formatting rules")
        void shouldContainAllCriticalRules() {
            when(userVectorStoreManager.getVectorStore(1L)).thenReturn(vectorStore);
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

            // Since ChatClient is mocked with RETURNS_DEEP_STUBS, we cannot easily capture the intermediate method call arguments
            // in a deeply chained mock without capturing at the exact level. Let's mock the specific chain to capture it:
            ChatClient.ChatClientRequestSpec mockSpec = mock(ChatClient.ChatClientRequestSpec.class, RETURNS_DEEP_STUBS);
            when(chatClient.prompt()).thenReturn(mockSpec);
            
            ArgumentCaptor<String> systemCaptor = ArgumentCaptor.forClass(String.class);
            when(mockSpec.system(systemCaptor.capture())).thenReturn(mockSpec);

            aiService.getFinancialAdvice(1L, "test question", "Test context data");

            String systemPrompt = systemCaptor.getValue();

            // Verify all critical elements are present in the prompt
            assertThat(systemPrompt).contains("FinSight's AI Financial Advisor");
            assertThat(systemPrompt).contains("CRITICAL RULES");
            assertThat(systemPrompt).contains("DO NOT simulate a conversation");
            assertThat(systemPrompt).contains("UNDER 3 SENTENCES");
            assertThat(systemPrompt).contains("bullet points");
            assertThat(systemPrompt).contains("bold text");
            assertThat(systemPrompt).contains("CURRENT MONTH SUMMARY");
            assertThat(systemPrompt).contains("HISTORICAL TRANSACTION CONTEXT");
            assertThat(systemPrompt).contains("Test context data");
        }

        @Test
        @DisplayName("Should include RAG-retrieved documents in system prompt")
        void shouldIncludeRagDocuments() {
            Document doc1 = new Document("Transaction on 2026-01-15: Spent 500 on Food", Map.of("userId", "1"));
            Document doc2 = new Document("Transaction on 2026-01-20: Spent 200 on Transport", Map.of("userId", "1"));

            when(userVectorStoreManager.getVectorStore(1L)).thenReturn(vectorStore);
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc1, doc2));
            
            ChatClient.ChatClientRequestSpec mockSpec = mock(ChatClient.ChatClientRequestSpec.class, RETURNS_DEEP_STUBS);
            when(chatClient.prompt()).thenReturn(mockSpec);
            
            ArgumentCaptor<String> systemCaptor = ArgumentCaptor.forClass(String.class);
            when(mockSpec.system(systemCaptor.capture())).thenReturn(mockSpec);

            aiService.getFinancialAdvice(1L, "what did I spend on food?", "summary data");

            String prompt = systemCaptor.getValue();

            assertThat(prompt).contains("Spent 500 on Food");
            assertThat(prompt).contains("Spent 200 on Transport");
        }

        @Test
        @DisplayName("Should filter RAG retrieval by userId for privacy isolation")
        void shouldFilterByUserId() {
            when(userVectorStoreManager.getVectorStore(42L)).thenReturn(vectorStore);
            when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

            aiService.getFinancialAdvice(42L, "question", "context");

            verify(userVectorStoreManager).getVectorStore(42L);
        }
    }
}
