package com.finsight.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * GDPR Data Export DTO - contains all user data in a portable format.
 * 
 * Used for:
 * - GDPR Article 20: Right to data portability
 * - User data exports
 * - Data retention verification
 * 
 * Format: Nested JSON structure with all user information
 */
public class GdprExportResponse {

    public static class ExportData {
        public UserData user;
        public List<TransactionData> transactions;
        public List<BudgetData> budgets;
        public List<RecurringTransactionData> recurringTransactions;
        public List<CategoryData> categories;
        public ExportMetadata metadata;

        public ExportData() {}

        public ExportData(UserData user, List<TransactionData> transactions,
                         List<BudgetData> budgets, List<RecurringTransactionData> recurringTransactions,
                         List<CategoryData> categories, ExportMetadata metadata) {
            this.user = user;
            this.transactions = transactions;
            this.budgets = budgets;
            this.recurringTransactions = recurringTransactions;
            this.categories = categories;
            this.metadata = metadata;
        }
    }

    public static class UserData {
        public Long userId;
        public String name;
        public String email;
        public String role;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        public LocalDateTime createdAt;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        public LocalDateTime lastLogin;

        public UserData() {}

        public UserData(Long userId, String name, String email, String role,
                       LocalDateTime createdAt, LocalDateTime lastLogin) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.role = role;
            this.createdAt = createdAt;
            this.lastLogin = lastLogin;
        }
    }

    public static class TransactionData {
        public Long id;
        public BigDecimal amount;
        public String type; // INCOME, EXPENSE
        public String category;
        public String description;
        public LocalDate date;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        public LocalDateTime createdAt;

        public TransactionData() {}

        public TransactionData(Long id, BigDecimal amount, String type, String category,
                              String description, LocalDate date, LocalDateTime createdAt) {
            this.id = id;
            this.amount = amount;
            this.type = type;
            this.category = category;
            this.description = description;
            this.date = date;
            this.createdAt = createdAt;
        }
    }

    public static class BudgetData {
        public Long id;
        public String category;
        public BigDecimal monthlyLimit;
        public int month;
        public int year;

        public BudgetData() {}

        public BudgetData(Long id, String category, BigDecimal monthlyLimit, int month, int year) {
            this.id = id;
            this.category = category;
            this.monthlyLimit = monthlyLimit;
            this.month = month;
            this.year = year;
        }
    }

    public static class RecurringTransactionData {
        public Long id;
        public BigDecimal amount;
        public String type;
        public String category;
        public String description;
        public String frequency;
        public LocalDate startDate;
        public LocalDate endDate;
        public LocalDate nextOccurrence;
        public boolean active;

        public RecurringTransactionData() {}

        public RecurringTransactionData(Long id, BigDecimal amount, String type, String category,
                                       String description, String frequency, LocalDate startDate,
                                       LocalDate endDate, LocalDate nextOccurrence, boolean active) {
            this.id = id;
            this.amount = amount;
            this.type = type;
            this.category = category;
            this.description = description;
            this.frequency = frequency;
            this.startDate = startDate;
            this.endDate = endDate;
            this.nextOccurrence = nextOccurrence;
            this.active = active;
        }
    }

    public static class CategoryData {
        public Long id;
        public String name;
        public String type;

        public CategoryData() {}

        public CategoryData(Long id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }
    }

    public static class ExportMetadata {
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        public LocalDateTime exportedAt;
        public String dataFormat = "JSON";
        public String finsightVersion;
        public long transactionCount;
        public long budgetCount;
        public long recurringTransactionCount;
        public BigDecimal totalIncome;
        public BigDecimal totalExpenses;

        public ExportMetadata() {
            this.exportedAt = LocalDateTime.now();
        }

        public ExportMetadata(String finsightVersion, long transactionCount, long budgetCount,
                            long recurringTransactionCount, BigDecimal totalIncome, BigDecimal totalExpenses) {
            this();
            this.finsightVersion = finsightVersion;
            this.transactionCount = transactionCount;
            this.budgetCount = budgetCount;
            this.recurringTransactionCount = recurringTransactionCount;
            this.totalIncome = totalIncome;
            this.totalExpenses = totalExpenses;
        }
    }

    public ExportData data;

    public GdprExportResponse() {}

    public GdprExportResponse(ExportData data) {
        this.data = data;
    }
}
