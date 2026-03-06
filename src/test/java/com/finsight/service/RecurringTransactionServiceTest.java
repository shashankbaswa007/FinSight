package com.finsight.service;

import com.finsight.dto.RecurringTransactionRequest;
import com.finsight.dto.RecurringTransactionResponse;
import com.finsight.exception.ResourceNotFoundException;
import com.finsight.model.*;
import com.finsight.repository.RecurringTransactionRepository;
import com.finsight.repository.TransactionRepository;
import com.finsight.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionServiceTest {

    @Mock private RecurringTransactionRepository recurringRepo;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @Mock private CategoryService categoryService;

    @InjectMocks private RecurringTransactionService service;

    private User user;
    private Category category;
    private RecurringTransaction recurringTx;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test").email("test@test.com").password("pass").build();
        category = Category.builder().id(1L).name("Rent").type(TransactionType.EXPENSE).build();

        recurringTx = new RecurringTransaction();
        recurringTx.setId(1L);
        recurringTx.setUser(user);
        recurringTx.setAmount(BigDecimal.valueOf(15000));
        recurringTx.setType(TransactionType.EXPENSE);
        recurringTx.setCategory(category);
        recurringTx.setDescription("Monthly Rent");
        recurringTx.setFrequency(RecurringFrequency.MONTHLY);
        recurringTx.setStartDate(LocalDate.of(2024, 1, 1));
        recurringTx.setNextOccurrence(LocalDate.of(2024, 1, 1));
        recurringTx.setActive(true);
    }

    @Test
    void create_success() {
        RecurringTransactionRequest request = new RecurringTransactionRequest();
        request.setAmount(BigDecimal.valueOf(15000));
        request.setType(TransactionType.EXPENSE);
        request.setCategoryId(1L);
        request.setDescription("Monthly Rent");
        request.setFrequency(RecurringFrequency.MONTHLY);
        request.setStartDate(LocalDate.of(2024, 1, 1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(categoryService.findById(1L)).thenReturn(category);
        when(recurringRepo.save(any(RecurringTransaction.class))).thenReturn(recurringTx);

        RecurringTransactionResponse result = service.create(1L, request);

        assertThat(result.getAmount()).isEqualByComparingTo("15000");
        assertThat(result.getFrequency()).isEqualTo(RecurringFrequency.MONTHLY);
        assertThat(result.getCategoryName()).isEqualTo("Rent");
        assertThat(result.isActive()).isTrue();
        verify(recurringRepo).save(any(RecurringTransaction.class));
    }

    @Test
    void create_userNotFound_throws() {
        RecurringTransactionRequest request = new RecurringTransactionRequest();
        request.setCategoryId(1L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getByUser_returnsList() {
        when(recurringRepo.findByUserId(1L)).thenReturn(List.of(recurringTx));

        List<RecurringTransactionResponse> result = service.getByUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDescription()).isEqualTo("Monthly Rent");
    }

    @Test
    void getByUser_empty_returnsEmptyList() {
        when(recurringRepo.findByUserId(1L)).thenReturn(List.of());

        List<RecurringTransactionResponse> result = service.getByUser(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void deactivate_success() {
        when(recurringRepo.findById(1L)).thenReturn(Optional.of(recurringTx));

        service.deactivate(1L, 1L);

        assertThat(recurringTx.isActive()).isFalse();
        verify(recurringRepo).save(recurringTx);
    }

    @Test
    void deactivate_wrongUser_throws() {
        when(recurringRepo.findById(1L)).thenReturn(Optional.of(recurringTx));

        // User ID 99 doesn't match the recurring tx owner (userId=1)
        assertThatThrownBy(() -> service.deactivate(99L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void processRecurring_createsTransactionAndAdvancesDate() {
        LocalDate today = LocalDate.now();
        recurringTx.setNextOccurrence(today);
        when(recurringRepo.findByActiveAndNextOccurrenceLessThanEqual(true, today))
                .thenReturn(List.of(recurringTx));

        service.processRecurring();

        verify(transactionRepository).save(any(Transaction.class));
        verify(recurringRepo, times(1)).save(recurringTx);
        // Next occurrence should be 1 month ahead
        assertThat(recurringTx.getNextOccurrence()).isEqualTo(today.plusMonths(1));
    }

    @Test
    void processRecurring_pastEndDate_deactivates() {
        LocalDate today = LocalDate.now();
        recurringTx.setNextOccurrence(today);
        recurringTx.setEndDate(today.minusDays(1)); // Already ended
        when(recurringRepo.findByActiveAndNextOccurrenceLessThanEqual(true, today))
                .thenReturn(List.of(recurringTx));

        service.processRecurring();

        verify(transactionRepository, never()).save(any(Transaction.class));
        assertThat(recurringTx.isActive()).isFalse();
    }

    @Test
    void processRecurring_weeklyFrequency_advancesOneWeek() {
        LocalDate today = LocalDate.now();
        recurringTx.setNextOccurrence(today);
        recurringTx.setFrequency(RecurringFrequency.WEEKLY);
        when(recurringRepo.findByActiveAndNextOccurrenceLessThanEqual(true, today))
                .thenReturn(List.of(recurringTx));

        service.processRecurring();

        assertThat(recurringTx.getNextOccurrence()).isEqualTo(today.plusWeeks(1));
    }
}
