package com.finsight.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.finsight.model.Category;
import com.finsight.model.Role;
import com.finsight.model.Transaction;
import com.finsight.model.TransactionType;
import com.finsight.model.User;

@DataJpaTest
@ActiveProfiles("test")
class TransactionRepositoryTest {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;

    private User user;
    private Category foodCategory;
    private Category salaryCategory;

        @BeforeEach
        @SuppressWarnings("unused")
    void setUp() {
        user = new User();
        user.setName("Test User");
        user.setEmail("repo-test@test.com");
        user.setPassword("hashed");
        user.setRole(Role.USER);
        user = userRepository.save(Objects.requireNonNull(user, "user"));

        foodCategory = Category.builder().name("Food").type(TransactionType.EXPENSE).build();
        foodCategory = categoryRepository.save(Objects.requireNonNull(foodCategory, "foodCategory"));

        salaryCategory = Category.builder().name("Salary").type(TransactionType.INCOME).build();
        salaryCategory = categoryRepository.save(Objects.requireNonNull(salaryCategory, "salaryCategory"));
    }

    @Test
        @SuppressWarnings("unused")
    void sumAmountByUserAndTypeAndDateRange() {
        Transaction t1 = Transaction.builder()
                .user(user).amount(BigDecimal.valueOf(500)).type(TransactionType.EXPENSE)
                .category(foodCategory).date(LocalDate.of(2026, 3, 5)).build();
        Transaction t2 = Transaction.builder()
                .user(user).amount(BigDecimal.valueOf(300)).type(TransactionType.EXPENSE)
                .category(foodCategory).date(LocalDate.of(2026, 3, 15)).build();
        transactionRepository.saveAll(Objects.requireNonNull(List.of(
                Objects.requireNonNull(t1, "t1"),
                Objects.requireNonNull(t2, "t2")
        ), "transactions"));

        BigDecimal sum = transactionRepository.sumAmountByUserAndTypeAndDateRange(
                user.getId(), TransactionType.EXPENSE,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(sum).isEqualByComparingTo(BigDecimal.valueOf(800));
    }

    @Test
        @SuppressWarnings("unused")
    void sumAmountByUserAndTypeAndDateRange_noResults_returnsZero() {
        BigDecimal sum = transactionRepository.sumAmountByUserAndTypeAndDateRange(
                user.getId(), TransactionType.INCOME,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertThat(sum).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
        @SuppressWarnings("unused")
    void findByIdAndUserId() {
        Transaction t = Transaction.builder()
                .user(user).amount(BigDecimal.valueOf(100)).type(TransactionType.EXPENSE)
                .category(foodCategory).date(LocalDate.now()).build();
        t = transactionRepository.save(Objects.requireNonNull(t, "transaction"));

        assertThat(transactionRepository.findByIdAndUserId(t.getId(), user.getId())).isPresent();
        assertThat(transactionRepository.findByIdAndUserId(t.getId(), 999L)).isEmpty();
    }

    @Test
        @SuppressWarnings("unused")
    void findTopSpendingCategories() {
        transactionRepository.save(Objects.requireNonNull(Transaction.builder()
                .user(user).amount(BigDecimal.valueOf(1000)).type(TransactionType.EXPENSE)
                .category(foodCategory).date(LocalDate.of(2026, 3, 10)).build(), "transaction"));

        List<Object[]> results = transactionRepository.findTopSpendingCategories(
                user.getId(), LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertThat(results).hasSize(1);
        assertThat((String) results.get(0)[0]).isEqualTo("Food");
    }
}
