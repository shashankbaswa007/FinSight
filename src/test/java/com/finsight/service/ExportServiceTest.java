package com.finsight.service;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @InjectMocks private ExportService exportService;

    private User user;
    private Category foodCategory;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test").email("test@test.com").password("pass").build();
        foodCategory = Category.builder().id(1L).name("Food").type(TransactionType.EXPENSE).build();
    }

    @Test
    void exportCsv_withDateRange_returnsCsvWithHeader() {
        Transaction tx = Transaction.builder()
                .id(1L).user(user).amount(BigDecimal.valueOf(500))
                .type(TransactionType.EXPENSE).category(foodCategory)
                .description("Groceries").date(LocalDate.of(2024, 1, 15)).build();

        Page<Transaction> page = new PageImpl<>(List.of(tx));
        when(transactionRepository.findByUserIdAndDateBetweenOrderByDateDesc(
                eq(1L), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        byte[] csv = exportService.exportTransactionsCsv(1L,
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));

        String content = new String(csv);
        assertThat(content).contains("Date,Type,Category,Amount,Description");
        assertThat(content).contains("2024-01-15");
        assertThat(content).contains("EXPENSE");
        assertThat(content).contains("Food");
        assertThat(content).contains("500");
        assertThat(content).contains("Groceries");
    }

    @Test
    void exportCsv_withoutDateRange_exportsAll() {
        Transaction tx1 = Transaction.builder()
                .id(1L).user(user).amount(BigDecimal.valueOf(200))
                .type(TransactionType.EXPENSE).category(foodCategory)
                .description("Lunch").date(LocalDate.of(2024, 1, 5)).build();
        Transaction tx2 = Transaction.builder()
                .id(2L).user(user).amount(BigDecimal.valueOf(50000))
                .type(TransactionType.INCOME)
                .category(Category.builder().id(2L).name("Salary").type(TransactionType.INCOME).build())
                .description("Monthly Salary").date(LocalDate.of(2024, 1, 1)).build();

        Page<Transaction> page = new PageImpl<>(List.of(tx1, tx2));
        when(transactionRepository.findByUserIdOrderByDateDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        byte[] csv = exportService.exportTransactionsCsv(1L, null, null);

        String content = new String(csv);
        assertThat(content).contains("Lunch");
        assertThat(content).contains("Monthly Salary");
        assertThat(content.split("\n")).hasSize(3); // header + 2 rows
    }

    @Test
    void exportCsv_emptyTransactions_returnsHeaderOnly() {
        Page<Transaction> emptyPage = new PageImpl<>(List.of());
        when(transactionRepository.findByUserIdOrderByDateDesc(eq(1L), any(Pageable.class)))
                .thenReturn(emptyPage);

        byte[] csv = exportService.exportTransactionsCsv(1L, null, null);

        String content = new String(csv);
        assertThat(content.trim()).isEqualTo("Date,Type,Category,Amount,Description");
    }

    @Test
    void exportCsv_descriptionWithQuotes_escapesCorrectly() {
        Transaction tx = Transaction.builder()
                .id(1L).user(user).amount(BigDecimal.valueOf(100))
                .type(TransactionType.EXPENSE).category(foodCategory)
                .description("Pizza \"Special\"").date(LocalDate.of(2024, 1, 10)).build();

        Page<Transaction> page = new PageImpl<>(List.of(tx));
        when(transactionRepository.findByUserIdOrderByDateDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        byte[] csv = exportService.exportTransactionsCsv(1L, null, null);

        String content = new String(csv);
        // Escaped double quotes inside the CSV field
        assertThat(content).contains("Pizza \"\"Special\"\"");
    }

    @Test
    void exportCsv_nullDescription_handledGracefully() {
        Transaction tx = Transaction.builder()
                .id(1L).user(user).amount(BigDecimal.valueOf(100))
                .type(TransactionType.EXPENSE).category(foodCategory)
                .description(null).date(LocalDate.of(2024, 1, 10)).build();

        Page<Transaction> page = new PageImpl<>(List.of(tx));
        when(transactionRepository.findByUserIdOrderByDateDesc(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        byte[] csv = exportService.exportTransactionsCsv(1L, null, null);

        String content = new String(csv);
        assertThat(content).contains("Food");
        assertThat(content.split("\n")).hasSize(2);
    }
}
