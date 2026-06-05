package com.finsight.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class CreateWalletRequest {
    
    @NotBlank(message = "Currency code is required")
    private String currencyCode;
    
    private String walletName;
    
    @DecimalMin("0.00")
    private BigDecimal initialBalance;
    
    public CreateWalletRequest() {}
    
    public CreateWalletRequest(String currencyCode, String walletName) {
        this.currencyCode = currencyCode;
        this.walletName = walletName;
        this.initialBalance = BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    
    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }
    
    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }
}
