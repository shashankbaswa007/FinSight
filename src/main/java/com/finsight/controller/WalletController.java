package com.finsight.controller;

import com.finsight.dto.CreateWalletRequest;
import com.finsight.dto.WalletResponse;
import com.finsight.model.UserWallet;
import com.finsight.service.CurrencyService;
import com.finsight.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController extends BaseController {
    
    private final CurrencyService currencyService;
    private final SecurityUtil securityUtil;
    
    public WalletController(CurrencyService currencyService, SecurityUtil securityUtil) {
        this.currencyService = currencyService;
        this.securityUtil = securityUtil;
    }
    
    /**
     * Get all wallets for user
     */
    @GetMapping
    public ResponseEntity<List<WalletResponse>> getUserWallets() {
        Long userId = getUserId();
        
        // Ensure primary wallet exists
        currencyService.getPrimaryWallet(userId);
        
        List<WalletResponse> wallets = currencyService.getUserWallets(userId)
            .stream()
            .map(WalletResponse::fromEntity)
            .toList();
        return ResponseEntity.ok(wallets);
    }
    
    /**
     * Create new wallet for user
     */
    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        Long userId = getUserId();
        UserWallet wallet = currencyService.createWallet(userId, request.getCurrencyCode(), 
            request.getWalletName());
        
        if (request.getInitialBalance() != null && request.getInitialBalance().compareTo(BigDecimal.ZERO) > 0) {
            wallet.setBalance(request.getInitialBalance());
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(WalletResponse.fromEntity(wallet));
    }
    
    /**
     * Get primary wallet
     */
    @GetMapping("/primary")
    public ResponseEntity<WalletResponse> getPrimaryWallet() {
        Long userId = getUserId();
        UserWallet wallet = currencyService.getPrimaryWallet(userId);
        return ResponseEntity.ok(WalletResponse.fromEntity(wallet));
    }

    private @NonNull Long getUserId() {
        return Objects.requireNonNull(securityUtil.getCurrentUserId(), "userId");
    }
}
