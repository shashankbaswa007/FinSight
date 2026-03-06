package com.finsight.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finsight.model.RecurringTransaction;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    List<RecurringTransaction> findByUserId(Long userId);

    List<RecurringTransaction> findByActiveAndNextOccurrenceLessThanEqual(boolean active, LocalDate date);
}
