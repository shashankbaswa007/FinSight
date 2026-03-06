package com.finsight.service;

import com.finsight.dto.*;
import com.finsight.model.Category;
import com.finsight.model.Transaction;
import com.finsight.model.TransactionType;
import com.finsight.model.User;
import com.finsight.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @InjectMocks private AnalyticsService analyticsService;

    private User user;
    private Category foodCategory;
    private Category salaryCategory;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test").email("test@test.com").password("pass").build();
        foodCategory = Category.builder().id(1L).name("Food").type(TransactionType.EXPENSE).build();
        salaryCategory = Category.builder().id(2L).name("Salary").type(TransactionType.INCOME).build();
    }

    @Test
    void getMonthlySummary_returnsCorrectTotals() {
        when(transactionRepository.sumAmountByUserAndTypeAndDateRange(
                eq(1L), eq(TransactionType.INCOME), any(), any()))
                .thenReturn(BigDecimal.valueOf(5000));
        when(transactionRepository.sumAmountByUserAndTypeAndDateRange(
                eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(BigDecimal.valueOf(3000));

        MonthlySummaryResponse result = analyticsService.getMonthlySummary(1L, 1, 2024);

        assertThat(result.getTotalIncome()).isEqualByComparingTo("5000");
        assertThat(result.getTotalExpense()).isEqualByComparingTo("3000");
        assertThat(result.getNetSavings()).isEqualByComparingTo("2000");
        assertThat(result.getMonth()).isEqualTo(1);
        assertThat(result.getYear()).isEqualTo(2024);
    }

    @Test
    void getMonthlySummary_zeroExpense_ratioIsZero() {
        when(transactionRepository.sumAmountByUserAndTypeAndDateRange(
                eq(1L), eq(TransactionType.INCOME), any(), any()))
                .thenReturn(BigDecimal.valueOf(5000));
        when(transactionRepository.sumAmountByUserAndTypeAndDateRange(
                eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(BigDecimal.ZERO);

        MonthlySummaryResponse result = analyticsService.getMonthlySummary(1L, 1, 2024);

        assertThat(result.getIncomeExpenseRatio()).isEqualByComparingTo("0");
    }

    @Test
    void getTopCategories_returnsCategoriesOrderedByAmount() {
        List<Object[]> mockData = List.of(
                new Object[]{"Food", BigDecimal.valueOf(2000), 10L},
                new Object[]{"Transport", BigDecimal.valueOf(1000), 5L}
        );
        when(transactionRepository.findTopSpendingCategories(eq(1L), any(), any()))
                .thenReturn(mockData);

        List<TopCategoryResponse> result = analyticsService.getTopCategories(1L, 1, 2024);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategoryName()).isEqualTo("Food");
        assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo("2000");
        assertThat(result.get(1).getCategoryName()).isEqualTo("Transport");
    }

    @Test
    void getSpendingTrends_aggregatesIncomeAndExpense() {
        List<Object[]> mockData = List.of(
                new Object[]{1, 2024, TransactionType.EXPENSE, BigDecimal.valueOf(3000)},
                new Object[]{1, 2024, TransactionType.INCOME, BigDecimal.valueOf(5000)}
        );
        when(transactionRepository.findMonthlyTrends(eq(1L), any(), any()))
                .thenReturn(mockData);

        List<SpendingTrendResponse> result = analyticsService.getSpendingTrends(1L, 6);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTotalSpending()).isEqualByComparingTo("3000");
        assertThat(result.get(0).getTotalIncome()).isEqualByComparingTo("5000");
    }

    @Test
    void detectAnomalies_lessThanThreeTransactions_returnsEmpty() {
        List<Transaction> expenses = List.of(
                Transaction.builder().id(1L).user(user).amount(BigDecimal.valueOf(100))
                        .type(TransactionType.EXPENSE).category(foodCategory).date(LocalDate.now()).build()
        );
        when(transactionRepository.findByUserIdAndType(1L, TransactionType.EXPENSE))
                .thenReturn(expenses);

        List<AnomalyResponse> result = analyticsService.detectAnomalies(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void detectAnomalies_flagsHighSpending() {
        // Create normal transactions plus one extreme outlier
        List<Transaction> expenses = List.of(
                buildExpense(1L, 100),
                buildExpense(2L, 110),
                buildExpense(3L, 105),
                buildExpense(4L, 95),
                buildExpense(5L, 100),
                buildExpense(6L, 105),
                buildExpense(7L, 95),
                buildExpense(8L, 100),
                buildExpense(9L, 110),
                buildExpense(10L, 5000) // extreme outlier
        );
        when(transactionRepository.findByUserIdAndType(1L, TransactionType.EXPENSE))
                .thenReturn(expenses);

        List<AnomalyResponse> result = analyticsService.detectAnomalies(1L);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("5000");
    }

    @Test
    void detectAnomalies_allSameAmount_returnsEmpty() {
        List<Transaction> expenses = List.of(
                buildExpense(1L, 100),
                buildExpense(2L, 100),
                buildExpense(3L, 100)
        );
        when(transactionRepository.findByUserIdAndType(1L, TransactionType.EXPENSE))
                .thenReturn(expenses);

        List<AnomalyResponse> result = analyticsService.detectAnomalies(1L);

        assertThat(result).isEmpty(); // stddev=0, no anomalies
    }

    @Test
    void getDailySpending_returnsMappedData() {
        List<Object[]> mockData = List.of(
                new Object[]{LocalDate.of(2024, 1, 5), BigDecimal.valueOf(200)},
                new Object[]{LocalDate.of(2024, 1, 10), BigDecimal.valueOf(350)}
        );
        when(transactionRepository.findDailySpending(eq(1L), any(), any()))
                .thenReturn(mockData);

        List<DailySpendingResponse> result = analyticsService.getDailySpending(1L, 1, 2024);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDate()).isEqualTo("2024-01-05");
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("200");
    }

    @Test
    void getMonthOverMonth_calculatesChanges() {
        // Current month
        when(transactionRepository.sumAmountByUserAndTypeAndDateRange(
                eq(1L), eq(TransactionType.INCOME),
                eq(LocalDate.of(2024, 2, 1)), eq(LocalDate.of(2024, 2, 29))))
                .thenReturn(BigDecimal.valueOf(6000));
        when(transactionRepository.sumAmountByUserAndTypeAndDateRange(
                eq(1L), eq(TransactionType.EXPENSE),
                eq(LocalDate.of(2024, 2, 1)), eq(LocalDate.of(2024, 2, 29))))
                .thenReturn(BigDecimal.valueOf(4000));
        // Previous month
        when(transactionRepository.sumAmountByUserAndTypeAndDateRange(
                eq(1L), eq(TransactionType.INCOME),
                eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 1, 31))))
                .thenReturn(BigDecimal.valueOf(5000));
        when(transactionRepository.sumAmountByUserAndTypeAndDateRange(
                eq(1L), eq(TransactionType.EXPENSE),
                eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 1, 31))))
                .thenReturn(BigDecimal.valueOf(3000));

        MonthOverMonthResponse result = analyticsService.getMonthOverMonth(1L, 2, 2024);

        assertThat(result.getCurrentIncome()).isEqualByComparingTo("6000");
        assertThat(result.getIncomeChange()).isEqualByComparingTo("1000");
        assertThat(result.getExpenseChange()).isEqualByComparingTo("1000");
    }

    @Test
    void getExpenseDistribution_bucketizesCorrectly() {
        List<Transaction> expenses = List.of(
                buildExpense(1L, 200),   // ₹0-500
                buildExpense(2L, 300),   // ₹0-500
                buildExpense(3L, 1500),  // ₹500-2K
                buildExpense(4L, 8000)   // ₹5K-10K
        );
        when(transactionRepository.findByUserIdAndTypeAndDateBetween(
                eq(1L), eq(TransactionType.EXPENSE), any(), any()))
                .thenReturn(expenses);

        List<ExpenseDistributionResponse> result = analyticsService.getExpenseDistribution(1L, 1, 2024);

        assertThat(result).hasSize(6);
        // First bucket (0-500) should have 2 transactions
        assertThat(result.get(0).getCount()).isEqualTo(2);
        assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo("500");
    }

    @Test
    void getTopDescriptions_returnsLimitedResults() {
        List<Object[]> mockData = List.of(
                new Object[]{"Zomato", BigDecimal.valueOf(3000), 15L},
                new Object[]{"Swiggy", BigDecimal.valueOf(2000), 10L}
        );
        when(transactionRepository.findTopDescriptions(eq(1L), any(), any()))
                .thenReturn(mockData);

        List<TopDescriptionResponse> result = analyticsService.getTopDescriptions(1L, 1, 2024, 5);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDescription()).isEqualTo("Zomato");
        assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo("3000");
    }

    @Test
    void getCategoryTrends_returnsMappedData() {
        List<Object[]> mockData = List.of(
                new Object[]{1, 2024, "Food", BigDecimal.valueOf(2000)},
                new Object[]{2, 2024, "Food", BigDecimal.valueOf(2500)}
        );
        when(transactionRepository.findCategoryTrends(eq(1L), any(), any()))
                .thenReturn(mockData);

        List<CategoryTrendResponse> result = analyticsService.getCategoryTrends(1L, 6);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategoryName()).isEqualTo("Food");
    }

    // ──── Helpers ────

    private Transaction buildExpense(Long id, double amount) {
        return Transaction.builder()
                .id(id).user(user)
                .amount(BigDecimal.valueOf(amount))
                .type(TransactionType.EXPENSE)
                .category(foodCategory)
                .description("Test")
                .date(LocalDate.now())
                .build();
    }
}
