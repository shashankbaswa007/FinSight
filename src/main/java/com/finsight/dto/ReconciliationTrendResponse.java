package com.finsight.dto;

import java.math.BigDecimal;

public class ReconciliationTrendResponse {

    private Integer month;
    private Integer year;
    private Integer totalBatches;
    private Integer completedBatches;
    private Integer failedBatches;
    private Integer matchedTransactions;
    private Integer unmatchedTransactions;
    private BigDecimal discrepancyTotal;
    private Double successRate;

    public ReconciliationTrendResponse() {}

    public ReconciliationTrendResponse(Integer month, Integer year, Integer totalBatches,
                                       Integer completedBatches, Integer failedBatches,
                                       Integer matchedTransactions, Integer unmatchedTransactions,
                                       BigDecimal discrepancyTotal, Double successRate) {
        this.month = month;
        this.year = year;
        this.totalBatches = totalBatches;
        this.completedBatches = completedBatches;
        this.failedBatches = failedBatches;
        this.matchedTransactions = matchedTransactions;
        this.unmatchedTransactions = unmatchedTransactions;
        this.discrepancyTotal = discrepancyTotal;
        this.successRate = successRate;
    }

    public Integer getMonth() { return month; }
    public void setMonth(Integer month) { this.month = month; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getTotalBatches() { return totalBatches; }
    public void setTotalBatches(Integer totalBatches) { this.totalBatches = totalBatches; }

    public Integer getCompletedBatches() { return completedBatches; }
    public void setCompletedBatches(Integer completedBatches) { this.completedBatches = completedBatches; }

    public Integer getFailedBatches() { return failedBatches; }
    public void setFailedBatches(Integer failedBatches) { this.failedBatches = failedBatches; }

    public Integer getMatchedTransactions() { return matchedTransactions; }
    public void setMatchedTransactions(Integer matchedTransactions) { this.matchedTransactions = matchedTransactions; }

    public Integer getUnmatchedTransactions() { return unmatchedTransactions; }
    public void setUnmatchedTransactions(Integer unmatchedTransactions) { this.unmatchedTransactions = unmatchedTransactions; }

    public BigDecimal getDiscrepancyTotal() { return discrepancyTotal; }
    public void setDiscrepancyTotal(BigDecimal discrepancyTotal) { this.discrepancyTotal = discrepancyTotal; }

    public Double getSuccessRate() { return successRate; }
    public void setSuccessRate(Double successRate) { this.successRate = successRate; }

    public static ReconciliationTrendResponseBuilder builder() { return new ReconciliationTrendResponseBuilder(); }

    public static class ReconciliationTrendResponseBuilder {
        private Integer month;
        private Integer year;
        private Integer totalBatches;
        private Integer completedBatches;
        private Integer failedBatches;
        private Integer matchedTransactions;
        private Integer unmatchedTransactions;
        private BigDecimal discrepancyTotal;
        private Double successRate;

        public ReconciliationTrendResponseBuilder month(Integer month) { this.month = month; return this; }
        public ReconciliationTrendResponseBuilder year(Integer year) { this.year = year; return this; }
        public ReconciliationTrendResponseBuilder totalBatches(Integer totalBatches) { this.totalBatches = totalBatches; return this; }
        public ReconciliationTrendResponseBuilder completedBatches(Integer completedBatches) { this.completedBatches = completedBatches; return this; }
        public ReconciliationTrendResponseBuilder failedBatches(Integer failedBatches) { this.failedBatches = failedBatches; return this; }
        public ReconciliationTrendResponseBuilder matchedTransactions(Integer matchedTransactions) { this.matchedTransactions = matchedTransactions; return this; }
        public ReconciliationTrendResponseBuilder unmatchedTransactions(Integer unmatchedTransactions) { this.unmatchedTransactions = unmatchedTransactions; return this; }
        public ReconciliationTrendResponseBuilder discrepancyTotal(BigDecimal discrepancyTotal) { this.discrepancyTotal = discrepancyTotal; return this; }
        public ReconciliationTrendResponseBuilder successRate(Double successRate) { this.successRate = successRate; return this; }

        public ReconciliationTrendResponse build() {
            return new ReconciliationTrendResponse(month, year, totalBatches, completedBatches, failedBatches,
                    matchedTransactions, unmatchedTransactions, discrepancyTotal, successRate);
        }
    }
}
