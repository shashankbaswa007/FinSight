package com.finsight.service;

import com.finsight.model.Currency;
import com.finsight.model.User;
import com.finsight.model.UserWallet;
import com.finsight.repository.CurrencyRepository;
import com.finsight.repository.UserRepository;
import com.finsight.repository.UserWalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CurrencyService {
    
    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);
    
    private final CurrencyRepository currencyRepository;
    private final UserWalletRepository walletRepository;
    private final UserRepository userRepository;
    
    public CurrencyService(CurrencyRepository currencyRepository,
                          UserWalletRepository walletRepository,
                          UserRepository userRepository) {
        this.currencyRepository = currencyRepository;
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Get or create primary wallet for user
     */
    public UserWallet getPrimaryWallet(@NonNull Long userId) {
        Optional<UserWallet> primary = walletRepository.findByUserIdAndPrimaryWallet(userId, true);
        if (primary.isPresent()) {
            return primary.get();
        }
        
        // Create primary wallet with USD
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Currency usd = currencyRepository.findByCode("USD")
            .orElseThrow(() -> new RuntimeException("USD currency not found"));
        
        UserWallet wallet = new UserWallet(user, usd);
        wallet.setPrimaryWallet(true);
        wallet.setWalletName("Primary");
        UserWallet saved = walletRepository.save(wallet);
        logger.info("Created primary wallet for user {} with currency USD", userId);
        return saved;
    }
    
    /**
     * Create wallet for user in specific currency
     */
    public UserWallet createWallet(@NonNull Long userId, String currencyCode, String walletName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Currency currency = currencyRepository.findByCode(currencyCode)
            .orElseThrow(() -> new RuntimeException("Currency not found: " + currencyCode));
        
        Optional<UserWallet> existing = walletRepository.findByUserIdAndCurrencyId(userId, currency.getId());
        if (existing.isPresent()) {
            throw new RuntimeException("Wallet already exists for this currency");
        }
        
        UserWallet wallet = new UserWallet(user, currency);
        wallet.setWalletName(walletName != null ? walletName : currency.getName());
        UserWallet saved = walletRepository.save(wallet);
        logger.info("Created wallet for user {} in currency {}", userId, currencyCode);
        return saved;
    }
    
    /**
     * Get all wallets for user
     */
    @Transactional(readOnly = true)
    public List<UserWallet> getUserWallets(@NonNull Long userId) {
        return walletRepository.findByUserId(userId);
    }
    
    /**
     * Update wallet balance
     */
    public UserWallet updateWalletBalance(@NonNull Long userId, @NonNull Long walletId, BigDecimal newBalance) {
        UserWallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new RuntimeException("Wallet not found"));
        
        if (!wallet.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        wallet.setBalance(newBalance);
        return walletRepository.save(wallet);
    }
    
    /**
     * Get all active currencies
     */
    @Transactional(readOnly = true)
    public List<Currency> getActiveCurrencies() {
        return currencyRepository.findAll().stream()
            .filter(c -> c.getActive())
            .toList();
    }
}
