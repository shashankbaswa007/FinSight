package com.finsight.service;

import com.finsight.dto.BudgetRequest;
import com.finsight.dto.BudgetResponse;
import com.finsight.dto.BudgetStatusResponse;
import com.finsight.exception.BadRequestException;
import com.finsight.exception.ResourceNotFoundException;
import com.finsight.model.Budget;
import com.finsight.model.Category;
import com.finsight.model.User;
import com.finsight.repository.BudgetRepository;
import com.finsight.repository.TransactionRepository;
import com.finsight.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing monthly budgets and checking budget status.
 * Compares actual spending against the defined limit for each category.
 */
@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final TransactionRepository transactionRepository;

    public BudgetService(BudgetRepository budgetRepository, UserRepository userRepository,
                         CategoryService categoryService, TransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.transactionRepository = transactionRepository;
    }

    /** Create a new budget entry for a user, category, and month. */
    @Transactional
    public BudgetResponse createBudget(Long userId, BudgetRequest request) {
        // Prevent duplicate budget for same category/month/year
        budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(
                userId, request.getCategoryId(), request.getMonth(), request.getYear()
        ).ifPresent(b -> {
            throw new BadRequestException("Budget already exists for this category/month/year");
        });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Category category = categoryService.findById(request.getCategoryId());

        Budget budget = Budget.builder()
                .user(user)
                .category(category)
                .monthlyLimit(request.getMonthlyLimit())
                .month(request.getMonth())
                .year(request.getYear())
                .build();

        budget = budgetRepository.save(budget);
        return mapToResponse(budget);
    }

    /** Retrieve all budgets for a user. */
    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(Long userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get budget status for a user in a given month/year.
     * Calculates actual spending per category and compares against the limit.
     */
    @Transactional(readOnly = true)
    public List<BudgetStatusResponse> getBudgetStatus(Long userId, int month, int year) {
        List<Budget> budgets = budgetRepository.findByUserIdAndMonthAndYear(userId, month, year);

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return budgets.stream().map(budget -> {
            BigDecimal spent = transactionRepository.sumExpenseByUserAndCategoryAndDateRange(
                    userId, budget.getCategory().getId(), startDate, endDate);

            BigDecimal remaining = budget.getMonthlyLimit().subtract(spent);

            return BudgetStatusResponse.builder()
                    .budgetId(budget.getId())
                    .categoryName(budget.getCategory().getName())
                    .monthlyLimit(budget.getMonthlyLimit())
                    .amountSpent(spent)
                    .remaining(remaining)
                    .exceeded(remaining.compareTo(BigDecimal.ZERO) < 0)
                    .month(month)
                    .year(year)
                    .build();
        }).collect(Collectors.toList());
    }

    private BudgetResponse mapToResponse(Budget budget) {
        return BudgetResponse.builder()
                .id(budget.getId())
                .categoryId(budget.getCategory().getId())
                .categoryName(budget.getCategory().getName())
                .monthlyLimit(budget.getMonthlyLimit())
                .month(budget.getMonth())
                .year(budget.getYear())
                .build();
    }
}
