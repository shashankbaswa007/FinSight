import api from './axios';
import type {
  MonthlySummaryResponse,
  TopCategoryResponse,
  SpendingTrendResponse,
  AnomalyResponse,
  MonthOverMonthResponse,
  DailySpendingResponse,
  CategoryTrendResponse,
  ExpenseDistributionResponse,
  TopDescriptionResponse,
  SpendingForecastResponse,
  ReconciliationTrendResponse,
  DeliveryAnalyticsResponse,
  FxHistoryResponse,
} from '../types';

export const analyticsApi = {
  monthlySummary: (month: number, year: number) =>
    api.get<MonthlySummaryResponse>(`/analytics/monthly-summary?month=${month}&year=${year}`).then((r) => r.data),

  topCategories: (month: number, year: number) =>
    api.get<TopCategoryResponse[]>(`/analytics/top-categories?month=${month}&year=${year}`).then((r) => r.data),

  spendingTrends: (months: number = 6) =>
    api.get<SpendingTrendResponse[]>(`/analytics/spending-trends?months=${months}`).then((r) => r.data),

  anomalies: () =>
    api.get<AnomalyResponse[]>('/analytics/anomaly-detection').then((r) => r.data),

  monthOverMonth: (month: number, year: number) =>
    api.get<MonthOverMonthResponse>(`/analytics/month-over-month?month=${month}&year=${year}`).then((r) => r.data),

  dailySpending: (month: number, year: number) =>
    api.get<DailySpendingResponse[]>(`/analytics/daily-spending?month=${month}&year=${year}`).then((r) => r.data),

  categoryTrends: (months: number = 6) =>
    api.get<CategoryTrendResponse[]>(`/analytics/category-trends?months=${months}`).then((r) => r.data),

  expenseDistribution: (month: number, year: number) =>
    api.get<ExpenseDistributionResponse[]>(`/analytics/expense-distribution?month=${month}&year=${year}`).then((r) => r.data),

  topDescriptions: (month: number, year: number, limit: number = 10) =>
    api.get<TopDescriptionResponse[]>(`/analytics/top-descriptions?month=${month}&year=${year}&limit=${limit}`).then((r) => r.data),

  spendingForecast: () =>
    api.get<SpendingForecastResponse>('/analytics/spending-forecast').then((r) => r.data),

  reconciliationTrends: (months: number = 6) =>
    api.get<ReconciliationTrendResponse[]>(`/analytics/reconciliation-trends?months=${months}`).then((r) => r.data),

  deliveryAnalytics: (days: number = 30) =>
    api.get<DeliveryAnalyticsResponse>(`/analytics/delivery-analytics?days=${days}`).then((r) => r.data),

  fxHistory: (from: string, to: string, days: number = 30) =>
    api.get<FxHistoryResponse>(`/analytics/fx-history?from=${from}&to=${to}&days=${days}`).then((r) => r.data),
};
