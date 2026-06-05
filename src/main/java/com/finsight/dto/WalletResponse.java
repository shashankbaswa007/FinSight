package com.finsight.dto;

import java.math.BigDecimal;

public class WalletResponse {
    private Long id;
    private String currencyCode;
    private String currencyName;
    private BigDecimal balance;
    private Boolean primaryWallet;
    private String walletName;
    
    public WalletResponse() {}
    
    public WalletResponse(Long id, String currencyCode, String currencyName, BigDecimal balance, 
                         Boolean primaryWallet, String walletName) {
        this.id = id;
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.balance = balance;
        this.primaryWallet = primaryWallet;
        this.walletName = walletName;
    }
    
    public static WalletResponse fromEntity(com.finsight.model.UserWallet wallet) {
        return new WalletResponse(
            wallet.getId(),
            wallet.getCurrency().getCode(),
            wallet.getCurrency().getName(),
            wallet.getBalance(),
            wallet.getPrimaryWallet(),
            wallet.getWalletName()
        );
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    
    public String getCurrencyName() { return currencyName; }
    public void setCurrencyName(String currencyName) { this.currencyName = currencyName; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    public Boolean getPrimaryWallet() { return primaryWallet; }
    public void setPrimaryWallet(Boolean primaryWallet) { this.primaryWallet = primaryWallet; }
    
    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }
}
