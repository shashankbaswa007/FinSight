package com.finsight.repository;

import com.finsight.model.Transaction;
import com.finsight.model.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Transaction entity with custom queries for analytics.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /** Find a transaction by id that belongs to a specific user. */
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    /** Paginated transactions for a user, ordered by date descending. */
    Page<Transaction> findByUserIdOrderByDateDesc(Long userId, Pageable pageable);

    /** Filter transactions by category. */
    Page<Transaction> findByUserIdAndCategoryIdOrderByDateDesc(Long userId, Long categoryId, Pageable pageable);

    /** Filter transactions by date range. */
    Page<Transaction> findByUserIdAndDateBetweenOrderByDateDesc(
            Long userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /** Filter transactions by category and date range. */
    Page<Transaction> findByUserIdAndCategoryIdAndDateBetweenOrderByDateDesc(
            Long userId, Long categoryId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    /** Flexible filter: optional type, category, and date range. */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (CAST(:startDate AS date) IS NULL OR t.date >= :startDate) " +
           "AND (CAST(:endDate AS date) IS NULL OR t.date <= :endDate) " +
           "ORDER BY t.date DESC")
    Page<Transaction> findFiltered(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /** Get all transactions of a specific type within a date range for a user. */
    List<Transaction> findByUserIdAndTypeAndDateBetween(
            Long userId, TransactionType type, LocalDate startDate, LocalDate endDate);

    /** Sum amounts by transaction type for a user in a date range. */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByUserAndTypeAndDateRange(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /** Sum spending for a user in a specific category and date range. */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.category.id = :categoryId " +
           "AND t.type = 'EXPENSE' AND t.date BETWEEN :startDate AND :endDate")
    BigDecimal sumExpenseByUserAndCategoryAndDateRange(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /** Top spending categories for a user in a date range. */
    @Query("SELECT t.category.name, SUM(t.amount), COUNT(t) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
           "AND t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY t.category.name ORDER BY SUM(t.amount) DESC")
    List<Object[]> findTopSpendingCategories(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /** Monthly spending trends: sum of income and expense by month. */
    @Query("SELECT MONTH(t.date), YEAR(t.date), t.type, SUM(t.amount) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(t.date), MONTH(t.date), t.type " +
           "ORDER BY YEAR(t.date), MONTH(t.date)")
    List<Object[]> findMonthlyTrends(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /** Fetch all expense transactions for a user (used in anomaly detection). */
    List<Transaction> findByUserIdAndType(Long userId, TransactionType type);

    /** Daily expense totals for a user within a date range. */
    @Query("SELECT t.date, SUM(t.amount) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
           "AND t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY t.date ORDER BY t.date")
    List<Object[]> findDailySpending(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /** Spending by category across months for a user. */
    @Query("SELECT MONTH(t.date), YEAR(t.date), t.category.name, SUM(t.amount) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
           "AND t.date BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(t.date), MONTH(t.date), t.category.name " +
           "ORDER BY YEAR(t.date), MONTH(t.date)")
    List<Object[]> findCategoryTrends(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /** Top descriptions/merchants by total expense amount. */
    @Query("SELECT t.description, SUM(t.amount), COUNT(t) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' " +
           "AND t.date BETWEEN :startDate AND :endDate " +
           "AND t.description IS NOT NULL AND t.description <> '' " +
           "GROUP BY t.description ORDER BY SUM(t.amount) DESC")
    List<Object[]> findTopDescriptions(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
