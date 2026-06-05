package com.finsight.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FxRatePointResponse {

    private LocalDate date;
    private BigDecimal rate;
    private String source;

    public FxRatePointResponse() {}

    public FxRatePointResponse(LocalDate date, BigDecimal rate, String source) {
        this.date = date;
        this.rate = rate;
        this.source = source;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getRate() { return rate; }
    public void setRate(BigDecimal rate) { this.rate = rate; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public static FxRatePointResponseBuilder builder() { return new FxRatePointResponseBuilder(); }

    public static class FxRatePointResponseBuilder {
        private LocalDate date;
        private BigDecimal rate;
        private String source;

        public FxRatePointResponseBuilder date(LocalDate date) { this.date = date; return this; }
        public FxRatePointResponseBuilder rate(BigDecimal rate) { this.rate = rate; return this; }
        public FxRatePointResponseBuilder source(String source) { this.source = source; return this; }

        public FxRatePointResponse build() {
            return new FxRatePointResponse(date, rate, source);
        }
    }
}
