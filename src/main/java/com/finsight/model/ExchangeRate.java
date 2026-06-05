package com.finsight.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity representing exchange rates between two currencies.
 * Used for multi-currency transactions and wallet conversions.
 */
@Entity
@Table(name = "exchange_rates", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"from_currency_id", "to_currency_id", "effective_date"})
})
public class ExchangeRate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_currency_id", nullable = false)
    private Currency fromCurrency;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_currency_id", nullable = false)
    private Currency toCurrency;
    
    @Column(nullable = false, precision = 18, scale = 8)
    private BigDecimal rate;
    
    @Column(nullable = false, length = 50)
    private String source; // API source: ECB, ALPHAVANTAGE, etc
    
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;
    
    // Constructors
    public ExchangeRate() {}
    
    public ExchangeRate(Currency fromCurrency, Currency toCurrency, BigDecimal rate, 
                       String source, LocalDate effectiveDate) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.source = source;
        this.effectiveDate = effectiveDate;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Currency getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(Currency fromCurrency) { this.fromCurrency = fromCurrency; }
    
    public Currency getToCurrency() { return toCurrency; }
    public void setToCurrency(Currency toCurrency) { this.toCurrency = toCurrency; }
    
    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }
    
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
}
