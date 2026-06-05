package com.finsight.service;

import com.finsight.model.Currency;
import com.finsight.model.ExchangeRate;
import com.finsight.repository.CurrencyRepository;
import com.finsight.repository.ExchangeRateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ExchangeRateService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);
    private static final int CONVERSION_SCALE = 8;
    private static final RoundingMode CONVERSION_ROUNDING = RoundingMode.HALF_UP;
    
    private final ExchangeRateRepository exchangeRateRepository;
    private final CurrencyRepository currencyRepository;
    private final Map<String, ExchangeRate> rateCache = new HashMap<>();
    
    public ExchangeRateService(ExchangeRateRepository exchangeRateRepository, 
                              CurrencyRepository currencyRepository) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.currencyRepository = currencyRepository;
    }
    
    /**
     * Get exchange rate between two currencies
     */
    @Transactional(readOnly = true)
    public BigDecimal getExchangeRate(String fromCode, String toCurrency, LocalDate effectiveDate) {
        if (fromCode.equals(toCurrency)) {
            return BigDecimal.ONE;
        }
        
        Currency fromCurr = currencyRepository.findByCode(fromCode)
            .orElseThrow(() -> new RuntimeException("Currency not found: " + fromCode));
        
        Currency toCurr = currencyRepository.findByCode(toCurrency)
            .orElseThrow(() -> new RuntimeException("Currency not found: " + toCurrency));
        
        String cacheKey = fromCode + "-" + toCurrency + "-" + effectiveDate;
        if (rateCache.containsKey(cacheKey)) {
            return rateCache.get(cacheKey).getRate();
        }
        
        Optional<ExchangeRate> rate = exchangeRateRepository.findLatestRate(
            fromCurr.getId(), toCurr.getId(), effectiveDate
        );
        
        if (rate.isEmpty()) {
            logger.warn("No exchange rate found for {} to {} on {}", fromCode, toCurrency, effectiveDate);
            return BigDecimal.ONE; // Fallback to 1:1 ratio
        }
        
        ExchangeRate exchangeRate = rate.get();
        rateCache.put(cacheKey, exchangeRate);
        return exchangeRate.getRate();
    }
    
    /**
     * Convert amount from one currency to another
     */
    public BigDecimal convertAmount(BigDecimal amount, String fromCode, String toCode, LocalDate effectiveDate) {
        BigDecimal rate = getExchangeRate(fromCode, toCode, effectiveDate);
        return amount.multiply(rate).setScale(CONVERSION_SCALE, CONVERSION_ROUNDING);
    }
    
    /**
     * Store exchange rate
     */
    public ExchangeRate storeExchangeRate(String fromCode, String toCode, BigDecimal rate, 
                                        String source, LocalDate effectiveDate) {
        Currency fromCurr = currencyRepository.findByCode(fromCode)
            .orElseThrow(() -> new RuntimeException("Currency not found: " + fromCode));
        
        Currency toCurr = currencyRepository.findByCode(toCode)
            .orElseThrow(() -> new RuntimeException("Currency not found: " + toCode));
        
        // Check for reverse rate
        Optional<ExchangeRate> existing = exchangeRateRepository.findLatestRate(
            fromCurr.getId(), toCurr.getId(), effectiveDate
        );
        
        if (existing.isPresent() && existing.get().getEffectiveDate().equals(effectiveDate)) {
            ExchangeRate ex = existing.get();
            ex.setRate(rate);
            return exchangeRateRepository.save(ex);
        }
        
        ExchangeRate exchangeRate = new ExchangeRate(fromCurr, toCurr, rate, source, effectiveDate);
        ExchangeRate saved = exchangeRateRepository.save(exchangeRate);
        
        // Clear cache
        String cacheKey = fromCode + "-" + toCode + "-" + effectiveDate;
        rateCache.remove(cacheKey);
        
        logger.info("Stored exchange rate: {} {} = {} {}", fromCode, rate, toCode, rate);
        return saved;
    }
    
    /**
     * Clear rate cache
     */
    public void clearCache() {
        rateCache.clear();
        logger.info("Cleared exchange rate cache");
    }
}
