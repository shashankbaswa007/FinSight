package com.finsight.service;

import com.finsight.dto.*;
import com.finsight.model.Transaction;
import com.finsight.model.TransactionType;
import com.finsight.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for financial analytics:
 * - Monthly income/expense summary
 * - Top spending categories
 * - Spending trends over time
 * - Anomaly detection using z-score analysis
 */
@Service
public class AnalyticsService {

    private final TransactionRepository transactionRepository;

    public AnalyticsService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Monthly summary: total income, total expenses, net savings,
     * and income-to-expense ratio for a given month.
     */
    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(Long userId, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        BigDecimal totalIncome = transactionRepository.sumAmountByUserAndTypeAndDateRange(
                userId, TransactionType.INCOME, start, end);
        BigDecimal totalExpense = transactionRepository.sumAmountByUserAndTypeAndDateRange(
                userId, TransactionType.EXPENSE, start, end);

        BigDecimal netSavings = totalIncome.subtract(totalExpense);

        // Ratio: income / expense (avoid division by zero)
        BigDecimal ratio = BigDecimal.ZERO;
        if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
            ratio = totalIncome.divide(totalExpense, 2, RoundingMode.HALF_UP);
        }

        return MonthlySummaryResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netSavings(netSavings)
                .incomeExpenseRatio(ratio)
                .build();
    }

    /**
     * Top spending categories for a given month ranked by total amount.
     */
    @Transactional(readOnly = true)
    public List<TopCategoryResponse> getTopCategories(Long userId, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Object[]> results = transactionRepository.findTopSpendingCategories(userId, start, end);

        return results.stream().map(row -> TopCategoryResponse.builder()
                .categoryName((String) row[0])
                .totalAmount((BigDecimal) row[1])
                .transactionCount((Long) row[2])
                .build()
        ).collect(Collectors.toList());
    }

    /**
     * Spending trends over the last N months (default 6).
     * Shows monthly totals for income and expenses.
     */
    @Transactional(readOnly = true)
    public List<SpendingTrendResponse> getSpendingTrends(Long userId, int months) {
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        LocalDate startDate = endDate.minusMonths(months - 1).withDayOfMonth(1);

        List<Object[]> results = transactionRepository.findMonthlyTrends(userId, startDate, endDate);

        // Aggregate results into a map keyed by "year-month"
        Map<String, SpendingTrendResponse> trendMap = new LinkedHashMap<>();

        for (Object[] row : results) {
            int m = (Integer) row[0];
            int y = (Integer) row[1];
            TransactionType type = (TransactionType) row[2];
            BigDecimal amount = (BigDecimal) row[3];

            String key = y + "-" + m;
            SpendingTrendResponse trend = trendMap.computeIfAbsent(key, k ->
                    SpendingTrendResponse.builder()
                            .month(m).year(y)
                            .totalSpending(BigDecimal.ZERO)
                            .totalIncome(BigDecimal.ZERO)
                            .build());

            if (type == TransactionType.EXPENSE) {
                trend.setTotalSpending(trend.getTotalSpending().add(amount));
            } else {
                trend.setTotalIncome(trend.getTotalIncome().add(amount));
            }
        }

        return new ArrayList<>(trendMap.values());
    }

    /**
     * Detect abnormal spending transactions using a z-score method.
     * A transaction is flagged if its z-score exceeds 2.0 (configurable threshold).
     * Z-score = (amount – mean) / stddev
     */
    @Transactional(readOnly = true)
    public List<AnomalyResponse> detectAnomalies(Long userId) {
        List<Transaction> expenses = transactionRepository
                .findByUserIdAndType(userId, TransactionType.EXPENSE);

        if (expenses.size() < 3) {
            // Not enough data to compute meaningful statistics
            return Collections.emptyList();
        }

        // Calculate mean
        BigDecimal sum = expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mean = sum.divide(BigDecimal.valueOf(expenses.size()), 4, RoundingMode.HALF_UP);

        // Calculate standard deviation
        BigDecimal varianceSum = expenses.stream()
                .map(t -> t.getAmount().subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        double stddev = Math.sqrt(
                varianceSum.divide(BigDecimal.valueOf(expenses.size()), 4, RoundingMode.HALF_UP).doubleValue());

        if (stddev == 0) {
            return Collections.emptyList();
        }

        // Flag transactions with z-score > 2.0
        double threshold = 2.0;

        return expenses.stream()
                .map(t -> {
                    double zScore = (t.getAmount().doubleValue() - mean.doubleValue()) / stddev;
                    return Map.entry(t, zScore);
                })
                .filter(entry -> entry.getValue() > threshold)
                .map(entry -> {
                    Transaction t = entry.getKey();
                    double zScore = entry.getValue();
                    String severity;
                    if (zScore > 3.5) {
                        severity = "HIGH";
                    } else if (zScore > 2.5) {
                        severity = "MEDIUM";
                    } else {
                        severity = "LOW";
                    }

                    return AnomalyResponse.builder()
                            .transactionId(t.getId())
                            .amount(t.getAmount())
                            .categoryName(t.getCategory().getName())
                            .description(t.getDescription())
                            .date(t.getDate())
                            .zScore(Math.round(zScore * 100.0) / 100.0)
                            .severity(severity)
                            .build();
                })
                .sorted(Comparator.comparingDouble(AnomalyResponse::getZScore).reversed())
                .collect(Collectors.toList());
    }
}
