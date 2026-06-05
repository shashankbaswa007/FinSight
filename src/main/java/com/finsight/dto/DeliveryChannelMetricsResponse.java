package com.finsight.dto;

public class DeliveryChannelMetricsResponse {

    private Long total;
    private Long succeeded;
    private Long failed;
    private Long pending;
    private Double successRate;
    private Double averageAttempts;

    public DeliveryChannelMetricsResponse() {}

    public DeliveryChannelMetricsResponse(Long total, Long succeeded, Long failed, Long pending,
                                          Double successRate, Double averageAttempts) {
        this.total = total;
        this.succeeded = succeeded;
        this.failed = failed;
        this.pending = pending;
        this.successRate = successRate;
        this.averageAttempts = averageAttempts;
    }

    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }

    public Long getSucceeded() { return succeeded; }
    public void setSucceeded(Long succeeded) { this.succeeded = succeeded; }

    public Long getFailed() { return failed; }
    public void setFailed(Long failed) { this.failed = failed; }

    public Long getPending() { return pending; }
    public void setPending(Long pending) { this.pending = pending; }

    public Double getSuccessRate() { return successRate; }
    public void setSuccessRate(Double successRate) { this.successRate = successRate; }

    public Double getAverageAttempts() { return averageAttempts; }
    public void setAverageAttempts(Double averageAttempts) { this.averageAttempts = averageAttempts; }

    public static DeliveryChannelMetricsResponseBuilder builder() { return new DeliveryChannelMetricsResponseBuilder(); }

    public static class DeliveryChannelMetricsResponseBuilder {
        private Long total;
        private Long succeeded;
        private Long failed;
        private Long pending;
        private Double successRate;
        private Double averageAttempts;

        public DeliveryChannelMetricsResponseBuilder total(Long total) { this.total = total; return this; }
        public DeliveryChannelMetricsResponseBuilder succeeded(Long succeeded) { this.succeeded = succeeded; return this; }
        public DeliveryChannelMetricsResponseBuilder failed(Long failed) { this.failed = failed; return this; }
        public DeliveryChannelMetricsResponseBuilder pending(Long pending) { this.pending = pending; return this; }
        public DeliveryChannelMetricsResponseBuilder successRate(Double successRate) { this.successRate = successRate; return this; }
        public DeliveryChannelMetricsResponseBuilder averageAttempts(Double averageAttempts) { this.averageAttempts = averageAttempts; return this; }

        public DeliveryChannelMetricsResponse build() {
            return new DeliveryChannelMetricsResponse(total, succeeded, failed, pending, successRate, averageAttempts);
        }
    }
}
