package com.finsight.controller;

import java.time.LocalDate;
import java.util.Objects;

import org.springframework.lang.NonNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finsight.dto.MonthlyTransactionSummary;
import com.finsight.dto.PagedResponse;
import com.finsight.dto.TransactionRequest;
import com.finsight.dto.TransactionResponse;
import com.finsight.model.TransactionType;
import com.finsight.service.TransactionService;
import com.finsight.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for managing financial transactions.
 * Supports CRUD operations, filtering, pagination, and monthly summaries.
 */
@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Manage income and expense transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final SecurityUtil securityUtil;

    public TransactionController(TransactionService transactionService, SecurityUtil securityUtil) {
        this.transactionService = transactionService;
        this.securityUtil = securityUtil;
    }

    @PostMapping
    @Operation(summary = "Create a transaction", description = "Records a new income or expense transaction")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request) {
        Long userId = getUserId();
        TransactionResponse response = transactionService.createTransaction(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a transaction", description = "Updates an existing transaction")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody TransactionRequest request) {
        Long userId = getUserId();
        TransactionResponse response = transactionService.updateTransaction(userId, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a transaction", description = "Deletes a transaction by ID")
    public ResponseEntity<Void> deleteTransaction(@PathVariable @NonNull Long id) {
        Long userId = getUserId();
        transactionService.deleteTransaction(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "List transactions", description = "Paginated list with optional type, category and date filters")
    public ResponseEntity<PagedResponse<TransactionResponse>> getTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId();
        TransactionType txType = null;
        if (type != null) {
            try {
                txType = TransactionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new com.finsight.exception.BadRequestException("Invalid transaction type: " + type);
            }
        }
        int cappedSize = Math.min(size, 100);
        PagedResponse<TransactionResponse> response =
                transactionService.getTransactions(userId, txType, categoryId, startDate, endDate, page, cappedSize);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/monthly")
    @Operation(summary = "Monthly transaction summary", description = "Income vs expense summary for a month")
    public ResponseEntity<MonthlyTransactionSummary> getMonthlyTransactionSummary(
            @RequestParam int month,
            @RequestParam int year) {
        Long userId = getUserId();
        MonthlyTransactionSummary summary = transactionService.getMonthlyTransactionSummary(userId, month, year);
        return ResponseEntity.ok(summary);
    }

    private @NonNull Long getUserId() {
        return Objects.requireNonNull(securityUtil.getCurrentUserId(), "userId");
    }
}
