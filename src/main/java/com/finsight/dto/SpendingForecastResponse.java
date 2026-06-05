package com.finsight.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * Spending forecast response using 6-month moving average algorithm.
 * 
 * Algorithm:
 * - Uses past 6 months of spending data
 * - Calculates simple moving average (SMA)
 * - Forecasts next 3 months based on trend
 * - Includes confidence intervals (±10% from forecast)
 * 
 * Useful for:
 * - Budget planning
 * - Spending trend analysis
 * - Identifying unusual spending patterns
 */
public class SpendingForecastResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    private List<ForecastPoint> forecasts;
    private List<HistoricalPoint> historical;
    private Double averageMonthlySpending;
    private Double trend; // Positive = increasing, negative = decreasing
    private String confidence; // "HIGH", "MEDIUM", "LOW"
    private LocalDate forecastStartDate;
    private LocalDate forecastEndDate;
    private String algorithm;

    public SpendingForecastResponse() {
    }

    public SpendingForecastResponse(
            List<ForecastPoint> forecasts,
            List<HistoricalPoint> historical,
            Double averageMonthlySpending,
            Double trend,
            String confidence,
            LocalDate forecastStartDate,
            LocalDate forecastEndDate
    ) {
        this.forecasts = forecasts;
        this.historical = historical;
        this.averageMonthlySpending = averageMonthlySpending;
        this.trend = trend;
        this.confidence = confidence;
        this.forecastStartDate = forecastStartDate;
        this.forecastEndDate = forecastEndDate;
        this.algorithm = "6-Month Moving Average";
    }

    public static class ForecastPoint implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer month;
        private Integer year;
        private Double forecast;
        private Double lowerBound; // Confidence interval: forecast - 10%
        private Double upperBound; // Confidence interval: forecast + 10%

        public ForecastPoint() {
        }

        public ForecastPoint(Integer month, Integer year, Double forecast, Double lowerBound, Double upperBound) {
            this.month = month;
            this.year = year;
            this.forecast = forecast;
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        // Getters and Setters
        public Integer getMonth() { return month; }
        public void setMonth(Integer month) { this.month = month; }

        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }

        public Double getForecast() { return forecast; }
        public void setForecast(Double forecast) { this.forecast = forecast; }

        public Double getLowerBound() { return lowerBound; }
        public void setLowerBound(Double lowerBound) { this.lowerBound = lowerBound; }

        public Double getUpperBound() { return upperBound; }
        public void setUpperBound(Double upperBound) { this.upperBound = upperBound; }
    }

    public static class HistoricalPoint implements Serializable {
        private static final long serialVersionUID = 1L;
        private Integer month;
        private Integer year;
        private Double actual;

        public HistoricalPoint() {
        }

        public HistoricalPoint(Integer month, Integer year, Double actual) {
            this.month = month;
            this.year = year;
            this.actual = actual;
        }

        // Getters and Setters
        public Integer getMonth() { return month; }
        public void setMonth(Integer month) { this.month = month; }

        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }

        public Double getActual() { return actual; }
        public void setActual(Double actual) { this.actual = actual; }
    }

    // Getters and Setters
    public List<ForecastPoint> getForecasts() { return forecasts; }
    public void setForecasts(List<ForecastPoint> forecasts) { this.forecasts = forecasts; }

    public List<HistoricalPoint> getHistorical() { return historical; }
    public void setHistorical(List<HistoricalPoint> historical) { this.historical = historical; }

    public Double getAverageMonthlySpending() { return averageMonthlySpending; }
    public void setAverageMonthlySpending(Double averageMonthlySpending) { this.averageMonthlySpending = averageMonthlySpending; }

    public Double getTrend() { return trend; }
    public void setTrend(Double trend) { this.trend = trend; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public LocalDate getForecastStartDate() { return forecastStartDate; }
    public void setForecastStartDate(LocalDate forecastStartDate) { this.forecastStartDate = forecastStartDate; }

    public LocalDate getForecastEndDate() { return forecastEndDate; }
    public void setForecastEndDate(LocalDate forecastEndDate) { this.forecastEndDate = forecastEndDate; }

    public String getAlgorithm() { return algorithm; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
}
