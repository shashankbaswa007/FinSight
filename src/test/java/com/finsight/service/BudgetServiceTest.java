package com.finsight.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.finsight.dto.BudgetRequest;
import com.finsight.dto.BudgetResponse;
import com.finsight.dto.BudgetStatusResponse;
import com.finsight.exception.BadRequestException;
import com.finsight.model.Budget;
import com.finsight.model.Category;
import com.finsight.model.TransactionType;
import com.finsight.model.User;
import com.finsight.repository.BudgetRepository;
import com.finsight.repository.TransactionRepository;
import com.finsight.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryService categoryService;
    @Mock private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User user;
    private Category category;
    private Budget budget;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test").email("test@test.com").password("pass").build();
        category = Category.builder().id(1L).name("Food").type(TransactionType.EXPENSE).build();
        budget = Budget.builder()
                .id(1L).user(user).category(category)
                .monthlyLimit(BigDecimal.valueOf(5000)).month(3).year(2026).build();
    }

    @Test
    void createBudget_success() {
        BudgetRequest request = new BudgetRequest();
        request.setCategoryId(1L);
        request.setMonthlyLimit(BigDecimal.valueOf(5000));
        request.setMonth(3);
        request.setYear(2026);

        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(1L, 1L, 3, 2026))
                .thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryService.findById(1L)).thenReturn(category);
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        BudgetResponse response = budgetService.createBudget(1L, request);

        assertThat(response.getCategoryName()).isEqualTo("Food");
        assertThat(response.getMonthlyLimit()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void createBudget_duplicateThrows() {
        BudgetRequest request = new BudgetRequest();
        request.setCategoryId(1L);
        request.setMonth(3);
        request.setYear(2026);

        when(budgetRepository.findByUserIdAndCategoryIdAndMonthAndYear(1L, 1L, 3, 2026))
                .thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> budgetService.createBudget(1L, request))
                .isInstanceOf(BadRequestException.class);
        verify(budgetRepository, never()).save(any());
    }

    @Test
    void getBudgets_returnsList() {
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(budget));

        List<BudgetResponse> result = budgetService.getBudgets(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void getBudgetStatus_calculatesCorrectly() {
        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 3, 2026)).thenReturn(List.of(budget));
        when(transactionRepository.sumExpenseByUserAndCategoryAndDateRange(any(), any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(3000));

        List<BudgetStatusResponse> statuses = budgetService.getBudgetStatus(1L, 3, 2026);

        assertThat(statuses).hasSize(1);
        assertThat(statuses.get(0).getAmountSpent()).isEqualByComparingTo(BigDecimal.valueOf(3000));
        assertThat(statuses.get(0).getRemaining()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(statuses.get(0).isExceeded()).isFalse();
    }

    @Test
    void getBudgetStatus_exceeded() {
        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 3, 2026)).thenReturn(List.of(budget));
        when(transactionRepository.sumExpenseByUserAndCategoryAndDateRange(any(), any(), any(), any()))
                .thenReturn(BigDecimal.valueOf(6000));

        List<BudgetStatusResponse> statuses = budgetService.getBudgetStatus(1L, 3, 2026);

        assertThat(statuses.get(0).isExceeded()).isTrue();
        assertThat(statuses.get(0).getRemaining()).isNegative();
    }
}
