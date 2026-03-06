package com.finsight.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.finsight.dto.TransactionRequest;
import com.finsight.dto.TransactionResponse;
import com.finsight.exception.ResourceNotFoundException;
import com.finsight.model.Category;
import com.finsight.model.Transaction;
import com.finsight.model.TransactionType;
import com.finsight.model.User;
import com.finsight.repository.TransactionRepository;
import com.finsight.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryService categoryService;
    @Mock private TransactionAuditService auditService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Category category;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test").email("test@test.com").password("pass").build();
        category = Category.builder().id(1L).name("Food").type(TransactionType.EXPENSE).build();
        transaction = Transaction.builder()
                .id(1L).user(user).amount(BigDecimal.valueOf(100))
                .type(TransactionType.EXPENSE).category(category)
                .description("Lunch").date(LocalDate.now()).build();
    }

    @Test
    void createTransaction_success() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(BigDecimal.valueOf(100));
        request.setType(TransactionType.EXPENSE);
        request.setCategoryId(1L);
        request.setDate(LocalDate.now());
        request.setDescription("Lunch");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryService.findById(1L)).thenReturn(category);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.createTransaction(1L, request);

        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(response.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(response.getCategoryName()).isEqualTo("Food");
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_userNotFoundThrows() {
        TransactionRequest request = new TransactionRequest();
        request.setCategoryId(1L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateTransaction_success() {
        TransactionRequest request = new TransactionRequest();
        request.setAmount(BigDecimal.valueOf(200));
        request.setType(TransactionType.EXPENSE);
        request.setCategoryId(1L);
        request.setDate(LocalDate.now());

        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(transaction));
        when(categoryService.findById(1L)).thenReturn(category);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

        TransactionResponse response = transactionService.updateTransaction(1L, 1L, request);
        assertThat(response).isNotNull();
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void deleteTransaction_success() {
        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(transaction));

        transactionService.deleteTransaction(1L, 1L);

        verify(transactionRepository).delete(transaction);
    }

    @Test
    void deleteTransaction_notFoundThrows() {
        when(transactionRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.deleteTransaction(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
