package com.finsight.dto;

import java.math.BigDecimal;

public class DailySpendingResponse {

    private String date;
    private BigDecimal amount;

    public DailySpendingResponse() {}

    public DailySpendingResponse(String date, BigDecimal amount) {
        this.date = date;
        this.amount = amount;
    }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public static DailySpendingResponseBuilder builder() { return new DailySpendingResponseBuilder(); }

    public static class DailySpendingResponseBuilder {
        private String date;
        private BigDecimal amount;

        public DailySpendingResponseBuilder date(String date) { this.date = date; return this; }
        public DailySpendingResponseBuilder amount(BigDecimal amount) { this.amount = amount; return this; }

        public DailySpendingResponse build() { return new DailySpendingResponse(date, amount); }
    }
}
