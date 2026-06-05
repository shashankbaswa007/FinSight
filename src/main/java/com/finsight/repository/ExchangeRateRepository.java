package com.finsight.repository;

import com.finsight.model.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    
    @Query("SELECT e FROM ExchangeRate e WHERE e.fromCurrency.id = :fromId AND e.toCurrency.id = :toId " +
           "AND e.effectiveDate <= :date ORDER BY e.effectiveDate DESC LIMIT 1")
    Optional<ExchangeRate> findLatestRate(@Param("fromId") Long fromId, @Param("toId") Long toId, 
                                         @Param("date") LocalDate date);
    
    @Query("SELECT e FROM ExchangeRate e WHERE e.fromCurrency.id = :fromId AND e.toCurrency.id = :toId " +
           "ORDER BY e.effectiveDate DESC")
    java.util.List<ExchangeRate> findByFromCurrencyAndToCurrency(@Param("fromId") Long fromId, 
                                                                   @Param("toId") Long toId);

    @Query("SELECT e FROM ExchangeRate e WHERE e.fromCurrency.id = :fromId AND e.toCurrency.id = :toId " +
           "AND e.effectiveDate BETWEEN :startDate AND :endDate ORDER BY e.effectiveDate ASC")
    List<ExchangeRate> findHistory(@Param("fromId") Long fromId,
                                   @Param("toId") Long toId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);
}
