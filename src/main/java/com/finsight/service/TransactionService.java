package com.finsight.service;

import com.finsight.dto.*;
import com.finsight.exception.ResourceNotFoundException;
import com.finsight.model.Category;
import com.finsight.model.Transaction;
import com.finsight.model.TransactionType;
import com.finsight.model.User;
import com.finsight.repository.TransactionRepository;
import com.finsight.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for creating, updating, deleting, and querying financial transactions.
 * Supports pagination, filtering by category/date, and monthly summaries.
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final TransactionAuditService auditService;
    private final CategorizationEventPublisher categorizationEventPublisher;
    private final RagDocumentIngestionService ragDocumentIngestionService;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository,
                              CategoryService categoryService, TransactionAuditService auditService,
                              CategorizationEventPublisher categorizationEventPublisher,
                              RagDocumentIngestionService ragDocumentIngestionService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.auditService = auditService;
        this.categorizationEventPublisher = categorizationEventPublisher;
        this.ragDocumentIngestionService = ragDocumentIngestionService;
    }

    /** Create a new transaction linked to the authenticated user. */
    @Transactional
    public TransactionResponse createTransaction(@NonNull Long userId, TransactionRequest request) {
        User user = findUserById(userId);
        
        Category category;
        boolean requiresAsyncCategorization = false;
        
        if (request.getCategoryId() != null) {
            category = categoryService.findById(request.getCategoryId());
        } else {
            // Provide a default category temporarily and queue async AI job
            category = categoryService.getOrCreateDefaultCategory();
            requiresAsyncCategorization = true;
        }

        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(request.getAmount())
                .type(request.getType())
                .category(category)
                .description(request.getDescription())
                .date(request.getDate())
                .build();

        transaction = transactionRepository.save(Objects.requireNonNull(transaction, "transaction"));
        auditService.logCreate(transaction, user.getEmail());
        
        if (requiresAsyncCategorization) {
            categorizationEventPublisher.publishCategorizationEvent(transaction.getId(), request.getDescription());
        }
        
        triggerRagIngestion(userId);
        
        return mapToResponse(transaction);
    }

    /** Update an existing transaction owned by the user. */
    @Transactional
    public TransactionResponse updateTransaction(@NonNull Long userId, @NonNull Long transactionId, TransactionRequest request) {
        Transaction transaction = findByIdAndUser(transactionId, userId);

        // Capture old values before mutation
        BigDecimal oldAmount = transaction.getAmount();
        String oldType = transaction.getType().name();
        String oldCategory = transaction.getCategory().getName();
        String oldDescription = transaction.getDescription();

        Category category;
        boolean requiresAsyncCategorization = false;
        
        if (request.getCategoryId() != null) {
            category = categoryService.findById(request.getCategoryId());
        } else {
            category = transaction.getCategory(); // Fallback to existing category
            requiresAsyncCategorization = true;
        }

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(category);
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());

        transaction = transactionRepository.save(transaction);
        auditService.logUpdate(transaction, oldAmount, oldType, oldCategory, oldDescription,
                transaction, transaction.getUser().getEmail());
                
        if (requiresAsyncCategorization) {
            categorizationEventPublisher.publishCategorizationEvent(transaction.getId(), request.getDescription());
        }
                
        triggerRagIngestion(userId);
        
        return mapToResponse(transaction);
    }

    /** Delete a transaction owned by the user. */
    @Transactional
    public void deleteTransaction(@NonNull Long userId, @NonNull Long transactionId) {
        Transaction transaction = findByIdAndUser(transactionId, userId);
        auditService.logDelete(transaction, transaction.getUser().getEmail());
        transactionRepository.delete(transaction);
        
        triggerRagIngestion(userId);
    }

    /**
     * Retrieve transactions for a user with optional filtering by type, category and date range.
     * Results are paginated.
     */
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getTransactions(
            @NonNull Long userId, TransactionType type, Long categoryId, LocalDate startDate, LocalDate endDate,
            int page, int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionPage = transactionRepository.findFiltered(
                userId, type, categoryId, startDate, endDate, pageable);

        List<TransactionResponse> content = transactionPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<TransactionResponse>builder()
                .content(content)
                .page(transactionPage.getNumber())
                .size(transactionPage.getSize())
                .totalElements(transactionPage.getTotalElements())
                .totalPages(transactionPage.getTotalPages())
                .last(transactionPage.isLast())
                .build();
    }

    /** Get a monthly transaction summary (income vs. expense) for a given month/year. */
    @Transactional(readOnly = true)
    public MonthlyTransactionSummary getMonthlyTransactionSummary(@NonNull Long userId, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        BigDecimal totalIncome = transactionRepository.sumAmountByUserAndTypeAndDateRange(
                userId, TransactionType.INCOME, startDate, endDate);
        BigDecimal totalExpense = transactionRepository.sumAmountByUserAndTypeAndDateRange(
                userId, TransactionType.EXPENSE, startDate, endDate);

        long count = transactionRepository
                .findByUserIdAndDateBetweenOrderByDateDesc(userId, startDate, endDate, Pageable.unpaged())
                .getTotalElements();

        return MonthlyTransactionSummary.builder()
                .month(month)
                .year(year)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .transactionCount(count)
                .build();
    }

    // ──────── Helpers ────────

    private @NonNull Transaction findByIdAndUser(@NonNull Long transactionId, @NonNull Long userId) {
        return Objects.requireNonNull(
                transactionRepository.findByIdAndUserId(transactionId, userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId)),
                "transaction"
        );
    }

    private @NonNull User findUserById(@NonNull Long userId) {
        return Objects.requireNonNull(
                userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId)),
                "user"
        );
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .categoryId(t.getCategory().getId())
                .categoryName(t.getCategory().getName())
                .description(t.getDescription())
                .date(t.getDate())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private void triggerRagIngestion(Long userId) {
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        ragDocumentIngestionService.ingestUserData(userId);
                    }
                }
            );
        } else {
            ragDocumentIngestionService.ingestUserData(userId);
        }
    }
}
