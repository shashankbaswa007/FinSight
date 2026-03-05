package com.finsight.controller;

import com.finsight.dto.BudgetRequest;
import com.finsight.dto.BudgetResponse;
import com.finsight.dto.BudgetStatusResponse;
import com.finsight.service.BudgetService;
import com.finsight.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing monthly budgets.
 * Allows users to set limits per category and check spending status.
 */
@RestController
@RequestMapping("/api/budgets")
@Tag(name = "Budgets", description = "Set and track monthly budgets per category")
public class BudgetController {

    private final BudgetService budgetService;
    private final SecurityUtil securityUtil;

    public BudgetController(BudgetService budgetService, SecurityUtil securityUtil) {
        this.budgetService = budgetService;
        this.securityUtil = securityUtil;
    }

    @PostMapping
    @Operation(summary = "Create a budget", description = "Sets a monthly spending limit for a category")
    public ResponseEntity<BudgetResponse> createBudget(@Valid @RequestBody BudgetRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        BudgetResponse response = budgetService.createBudget(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List budgets", description = "Retrieves all budgets for the authenticated user")
    public ResponseEntity<List<BudgetResponse>> getBudgets() {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(budgetService.getBudgets(userId));
    }

    @GetMapping("/status")
    @Operation(summary = "Budget status", description = "Check spending vs budget limit for a given month")
    public ResponseEntity<List<BudgetStatusResponse>> getBudgetStatus(
            @RequestParam int month,
            @RequestParam int year) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(budgetService.getBudgetStatus(userId, month, year));
    }
}
