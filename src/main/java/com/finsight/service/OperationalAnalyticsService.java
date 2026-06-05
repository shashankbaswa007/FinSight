package com.finsight.service;

import com.finsight.dto.DeliveryAnalyticsResponse;
import com.finsight.dto.DeliveryChannelMetricsResponse;
import com.finsight.dto.FxHistoryResponse;
import com.finsight.dto.FxRatePointResponse;
import com.finsight.dto.ReconciliationTrendResponse;
import com.finsight.model.Currency;
import com.finsight.model.ExchangeRate;
import com.finsight.model.NotificationDelivery;
import com.finsight.model.ReconciliationBatch;
import com.finsight.model.WebhookDelivery;
import com.finsight.repository.CurrencyRepository;
import com.finsight.repository.ExchangeRateRepository;
import com.finsight.repository.NotificationDeliveryRepository;
import com.finsight.repository.ReconciliationBatchRepository;
import com.finsight.repository.WebhookDeliveryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class OperationalAnalyticsService {

    private final ReconciliationBatchRepository reconciliationBatchRepository;
    private final WebhookDeliveryRepository webhookDeliveryRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final CurrencyRepository currencyRepository;
    private final ExchangeRateRepository exchangeRateRepository;

    public OperationalAnalyticsService(ReconciliationBatchRepository reconciliationBatchRepository,
                                       WebhookDeliveryRepository webhookDeliveryRepository,
                                       NotificationDeliveryRepository notificationDeliveryRepository,
                                       CurrencyRepository currencyRepository,
                                       ExchangeRateRepository exchangeRateRepository) {
        this.reconciliationBatchRepository = reconciliationBatchRepository;
        this.webhookDeliveryRepository = webhookDeliveryRepository;
        this.notificationDeliveryRepository = notificationDeliveryRepository;
        this.currencyRepository = currencyRepository;
        this.exchangeRateRepository = exchangeRateRepository;
    }

    public List<ReconciliationTrendResponse> getReconciliationTrends(Long userId, int months) {
        int window = Math.max(1, Math.min(months, 24));
        LocalDate endDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        LocalDate startDate = endDate.minusMonths(window - 1).withDayOfMonth(1);

        List<ReconciliationBatch> batches = reconciliationBatchRepository
                .findByUserIdAndBatchDateBetweenOrderByBatchDateAsc(userId, startDate, endDate);

        Map<YearMonth, TrendAccumulator> trendMap = new LinkedHashMap<>();
        YearMonth cursor = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(endDate);
        while (!cursor.isAfter(endMonth)) {
            trendMap.put(cursor, new TrendAccumulator(cursor));
            cursor = cursor.plusMonths(1);
        }

        for (ReconciliationBatch batch : batches) {
            YearMonth key = YearMonth.from(batch.getBatchDate());
            TrendAccumulator acc = trendMap.get(key);
            if (acc == null) {
                continue;
            }
            acc.totalBatches++;
            if (batch.getStatus() == ReconciliationBatch.ReconciliationStatus.COMPLETED) {
                acc.completedBatches++;
            } else if (batch.getStatus() == ReconciliationBatch.ReconciliationStatus.FAILED) {
                acc.failedBatches++;
            }
            acc.matchedTransactions += safeInt(batch.getMatchedTransactions());
            acc.unmatchedTransactions += safeInt(batch.getUnmatchedTransactions());
            acc.discrepancyTotal = acc.discrepancyTotal.add(
                    batch.getDiscrepancyAmount() == null ? BigDecimal.ZERO : batch.getDiscrepancyAmount()
            );
        }

        List<ReconciliationTrendResponse> results = new ArrayList<>();
        for (TrendAccumulator acc : trendMap.values()) {
            double successRate = acc.totalBatches > 0
                    ? roundTwoDecimals(acc.completedBatches * 100.0 / acc.totalBatches)
                    : 0.0;
            results.add(ReconciliationTrendResponse.builder()
                    .month(acc.month.getMonthValue())
                    .year(acc.month.getYear())
                    .totalBatches(acc.totalBatches)
                    .completedBatches(acc.completedBatches)
                    .failedBatches(acc.failedBatches)
                    .matchedTransactions(acc.matchedTransactions)
                    .unmatchedTransactions(acc.unmatchedTransactions)
                    .discrepancyTotal(acc.discrepancyTotal)
                    .successRate(successRate)
                    .build());
        }
        return results;
    }

    public DeliveryAnalyticsResponse getDeliveryAnalytics(Long userId, int days) {
        int window = Math.max(1, Math.min(days, 90));
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(window);

        List<NotificationDelivery> notificationDeliveries = notificationDeliveryRepository
                .findByUserIdAndCreatedAtBetween(userId, start, end);
        List<WebhookDelivery> webhookDeliveries = webhookDeliveryRepository
                .findByUserIdAndCreatedAtBetween(userId, start, end);

        DeliveryChannelMetricsResponse notificationEmail = buildNotificationMetrics(notificationDeliveries);
        DeliveryChannelMetricsResponse webhook = buildWebhookMetrics(webhookDeliveries);

        return DeliveryAnalyticsResponse.builder()
                .start(start)
                .end(end)
                .notificationEmail(notificationEmail)
                .webhook(webhook)
                .build();
    }

    public FxHistoryResponse getFxHistory(String fromCode, String toCode, int days) {
        int window = Math.max(1, Math.min(days, 365));
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(window - 1);

        String from = fromCode == null ? null : fromCode.trim().toUpperCase(Locale.ROOT);
        String to = toCode == null ? null : toCode.trim().toUpperCase(Locale.ROOT);
        if (from == null || from.isBlank() || to == null || to.isBlank()) {
            throw new RuntimeException("Currency codes are required");
        }

        Currency fromCurrency = currencyRepository.findByCode(from)
                .orElseThrow(() -> new RuntimeException("Currency not found: " + from));
        Currency toCurrency = currencyRepository.findByCode(to)
                .orElseThrow(() -> new RuntimeException("Currency not found: " + to));

        List<ExchangeRate> rates = exchangeRateRepository.findHistory(
                fromCurrency.getId(),
                toCurrency.getId(),
                startDate,
                endDate
        );

        List<FxRatePointResponse> points = new ArrayList<>();
        for (ExchangeRate rate : rates) {
            points.add(FxRatePointResponse.builder()
                    .date(rate.getEffectiveDate())
                    .rate(rate.getRate())
                    .source(rate.getSource())
                    .build());
        }

        return FxHistoryResponse.builder()
                .fromCurrency(from)
                .toCurrency(to)
                .startDate(startDate)
                .endDate(endDate)
                .points(points)
                .build();
    }

    private DeliveryChannelMetricsResponse buildNotificationMetrics(List<NotificationDelivery> deliveries) {
        long total = deliveries.size();
        long succeeded = deliveries.stream()
                .filter(d -> d.getStatus() == NotificationDelivery.Status.SENT)
                .count();
        long failed = deliveries.stream()
                .filter(d -> d.getStatus() == NotificationDelivery.Status.FAILED)
                .count();
        long pending = deliveries.stream()
                .filter(d -> d.getStatus() == NotificationDelivery.Status.PENDING
                        || d.getStatus() == NotificationDelivery.Status.RETRYING)
                .count();
        double successRate = total > 0 ? roundTwoDecimals(succeeded * 100.0 / total) : 0.0;
        double avgAttempts = total > 0
                ? roundTwoDecimals(deliveries.stream().mapToInt(d -> safeInt(d.getAttemptCount())).average().orElse(0))
                : 0.0;

        return DeliveryChannelMetricsResponse.builder()
                .total(total)
                .succeeded(succeeded)
                .failed(failed)
                .pending(pending)
                .successRate(successRate)
                .averageAttempts(avgAttempts)
                .build();
    }

    private DeliveryChannelMetricsResponse buildWebhookMetrics(List<WebhookDelivery> deliveries) {
        long total = deliveries.size();
        long succeeded = deliveries.stream()
                .filter(d -> d.getStatus() == WebhookDelivery.Status.DELIVERED)
                .count();
        long failed = deliveries.stream()
                .filter(d -> d.getStatus() == WebhookDelivery.Status.FAILED)
                .count();
        long pending = deliveries.stream()
                .filter(d -> d.getStatus() == WebhookDelivery.Status.PENDING)
                .count();
        double successRate = total > 0 ? roundTwoDecimals(succeeded * 100.0 / total) : 0.0;
        double avgAttempts = total > 0
                ? roundTwoDecimals(deliveries.stream().mapToInt(d -> safeInt(d.getAttemptCount())).average().orElse(0))
                : 0.0;

        return DeliveryChannelMetricsResponse.builder()
                .total(total)
                .succeeded(succeeded)
                .failed(failed)
                .pending(pending)
                .successRate(successRate)
                .averageAttempts(avgAttempts)
                .build();
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private static class TrendAccumulator {
        private final YearMonth month;
        private int totalBatches = 0;
        private int completedBatches = 0;
        private int failedBatches = 0;
        private int matchedTransactions = 0;
        private int unmatchedTransactions = 0;
        private BigDecimal discrepancyTotal = BigDecimal.ZERO;

        private TrendAccumulator(YearMonth month) {
            this.month = month;
        }
    }
}
