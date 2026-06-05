package com.finsight.dto;

public class CurrencyResponse {
    private Long id;
    private String code;
    private String name;
    private String symbol;
    private Boolean active;
    
    public CurrencyResponse() {}
    
    public CurrencyResponse(Long id, String code, String name, String symbol, Boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.symbol = symbol;
        this.active = active;
    }
    
    public static CurrencyResponse fromEntity(com.finsight.model.Currency currency) {
        return new CurrencyResponse(
            currency.getId(),
            currency.getCode(),
            currency.getName(),
            currency.getSymbol(),
            currency.getActive()
        );
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
