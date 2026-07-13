package com.finsight.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for per-user vector store isolation.
 * Validates that each user gets their own store, stores are cached,
 * and user data cannot leak across boundaries.
 */
@ExtendWith(MockitoExtension.class)
class UserVectorStoreManagerTest {

    @Mock private EmbeddingModel embeddingModel;

    private UserVectorStoreManager manager;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        manager = new UserVectorStoreManager(embeddingModel);
        // Point the store directory to a temp location for test isolation
        ReflectionTestUtils.setField(manager, "baseDir", tempDir.getAbsolutePath());
    }

    @Test
    @DisplayName("Should create unique vector store per user")
    void shouldCreateUniqueStorePerUser() {
        VectorStore store1 = manager.getVectorStore(1L);
        VectorStore store2 = manager.getVectorStore(2L);

        assertThat(store1).isNotNull();
        assertThat(store2).isNotNull();
        assertThat(store1).isNotSameAs(store2);
    }

    @Test
    @DisplayName("Should return cached store on subsequent calls for same user")
    void shouldReturnCachedStore() {
        VectorStore first = manager.getVectorStore(1L);
        VectorStore second = manager.getVectorStore(1L);

        assertThat(first).isSameAs(second);
    }

    @Test
    @DisplayName("Should persist store file to disk on save")
    void shouldPersistToDisk() {
        manager.getVectorStore(99L);
        manager.save(99L);

        File expected = new File(tempDir, "user-99-vector-store.json");
        assertThat(expected).exists();
        assertThat(expected.length()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should handle save gracefully when store does not exist")
    void shouldHandleSaveForMissingStore() {
        // save() for a userId never loaded should not throw
        manager.save(999L);
        // No exception = pass
    }

    @Test
    @DisplayName("Different users should have isolated stores (privacy check)")
    void shouldIsolateUserStores() {
        VectorStore userA = manager.getVectorStore(100L);
        VectorStore userB = manager.getVectorStore(200L);

        // Stores must be completely separate objects
        assertThat(userA).isNotSameAs(userB);

        // After saving user A, user B's file should not exist
        manager.save(100L);
        File userAFile = new File(tempDir, "user-100-vector-store.json");
        File userBFile = new File(tempDir, "user-200-vector-store.json");

        assertThat(userAFile).exists();
        assertThat(userBFile).doesNotExist();
    }
}
