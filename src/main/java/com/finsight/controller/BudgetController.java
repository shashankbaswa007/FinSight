package com.finsight.controller;

import java.util.List;

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

import com.finsight.dto.BudgetRequest;
import com.finsight.dto.BudgetResponse;
import com.finsight.dto.BudgetStatusResponse;
import com.finsight.service.BudgetService;
import com.finsight.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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

    @PutMapping("/{id}")
    @Operation(summary = "Update a budget", description = "Updates the monthly limit of an existing budget")
    public ResponseEntity<BudgetResponse> updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {
        Long userId = securityUtil.getCurrentUserId();
        return ResponseEntity.ok(budgetService.updateBudget(userId, id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget", description = "Removes a budget entry")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long id) {
        Long userId = securityUtil.getCurrentUserId();
        budgetService.deleteBudget(userId, id);
        return ResponseEntity.noContent().build();
    }
}
