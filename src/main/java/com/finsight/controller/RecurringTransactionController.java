package com.finsight.controller;

import com.finsight.dto.RecurringTransactionRequest;
import com.finsight.dto.RecurringTransactionResponse;
import com.finsight.service.RecurringTransactionService;
import com.finsight.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring-transactions")
@Tag(name = "Recurring Transactions", description = "Manage recurring transaction schedules")
public class RecurringTransactionController {

    private final RecurringTransactionService service;
    private final SecurityUtil securityUtil;

    public RecurringTransactionController(RecurringTransactionService service, SecurityUtil securityUtil) {
        this.service = service;
        this.securityUtil = securityUtil;
    }

    @PostMapping
    @Operation(summary = "Create recurring transaction")
    public ResponseEntity<RecurringTransactionResponse> create(@Valid @RequestBody RecurringTransactionRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(userId, request));
    }

    @GetMapping
    @Operation(summary = "List recurring transactions")
    public ResponseEntity<List<RecurringTransactionResponse>> list() {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(service.getByUser(userId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate recurring transaction")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        Long userId = securityUtil.getCurrentUserId();
        service.deactivate(userId, id);
        return ResponseEntity.noContent().build();
    }
}
