package com.finsight.service;

import com.finsight.dto.*;
import com.finsight.exception.ResourceNotFoundException;
import com.finsight.model.Category;
import com.finsight.model.Transaction;
import com.finsight.model.TransactionType;
import com.finsight.model.User;
import com.finsight.repository.TransactionRepository;
import com.finsight.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
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

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository,
                              CategoryService categoryService, TransactionAuditService auditService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.auditService = auditService;
    }

    /** Create a new transaction linked to the authenticated user. */
    @Transactional
    public TransactionResponse createTransaction(Long userId, TransactionRequest request) {
        User user = findUserById(userId);
        Category category = categoryService.findById(request.getCategoryId());

        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(request.getAmount())
                .type(request.getType())
                .category(category)
                .description(request.getDescription())
                .date(request.getDate())
                .build();

        transaction = transactionRepository.save(transaction);
        auditService.logCreate(transaction, user.getEmail());
        return mapToResponse(transaction);
    }

    /** Update an existing transaction owned by the user. */
    @Transactional
    public TransactionResponse updateTransaction(Long userId, Long transactionId, TransactionRequest request) {
        Transaction transaction = findByIdAndUser(transactionId, userId);

        // Capture old values before mutation
        BigDecimal oldAmount = transaction.getAmount();
        String oldType = transaction.getType().name();
        String oldCategory = transaction.getCategory().getName();
        String oldDescription = transaction.getDescription();

        Category category = categoryService.findById(request.getCategoryId());

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(category);
        transaction.setDescription(request.getDescription());
        transaction.setDate(request.getDate());

        transaction = transactionRepository.save(transaction);
        auditService.logUpdate(transaction, oldAmount, oldType, oldCategory, oldDescription,
                transaction, transaction.getUser().getEmail());
        return mapToResponse(transaction);
    }

    /** Delete a transaction owned by the user. */
    @Transactional
    public void deleteTransaction(Long userId, Long transactionId) {
        Transaction transaction = findByIdAndUser(transactionId, userId);
        auditService.logDelete(transaction, transaction.getUser().getEmail());
        transactionRepository.delete(transaction);
    }

    /**
     * Retrieve transactions for a user with optional filtering by type, category and date range.
     * Results are paginated.
     */
    @Transactional(readOnly = true)
    public PagedResponse<TransactionResponse> getTransactions(
            Long userId, TransactionType type, Long categoryId, LocalDate startDate, LocalDate endDate,
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
    public MonthlyTransactionSummary getMonthlyTransactionSummary(Long userId, int month, int year) {
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

    private Transaction findByIdAndUser(Long transactionId, Long userId) {
        return transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", "id", transactionId));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
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
}
