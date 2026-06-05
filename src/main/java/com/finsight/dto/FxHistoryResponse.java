package com.finsight.dto;

import java.time.LocalDate;
import java.util.List;

public class FxHistoryResponse {

    private String fromCurrency;
    private String toCurrency;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<FxRatePointResponse> points;

    public FxHistoryResponse() {}

    public FxHistoryResponse(String fromCurrency, String toCurrency,
                             LocalDate startDate, LocalDate endDate,
                             List<FxRatePointResponse> points) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.startDate = startDate;
        this.endDate = endDate;
        this.points = points;
    }

    public String getFromCurrency() { return fromCurrency; }
    public void setFromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; }

    public String getToCurrency() { return toCurrency; }
    public void setToCurrency(String toCurrency) { this.toCurrency = toCurrency; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public List<FxRatePointResponse> getPoints() { return points; }
    public void setPoints(List<FxRatePointResponse> points) { this.points = points; }

    public static FxHistoryResponseBuilder builder() { return new FxHistoryResponseBuilder(); }

    public static class FxHistoryResponseBuilder {
        private String fromCurrency;
        private String toCurrency;
        private LocalDate startDate;
        private LocalDate endDate;
        private List<FxRatePointResponse> points;

        public FxHistoryResponseBuilder fromCurrency(String fromCurrency) { this.fromCurrency = fromCurrency; return this; }
        public FxHistoryResponseBuilder toCurrency(String toCurrency) { this.toCurrency = toCurrency; return this; }
        public FxHistoryResponseBuilder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public FxHistoryResponseBuilder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public FxHistoryResponseBuilder points(List<FxRatePointResponse> points) { this.points = points; return this; }

        public FxHistoryResponse build() {
            return new FxHistoryResponse(fromCurrency, toCurrency, startDate, endDate, points);
        }
    }
}
