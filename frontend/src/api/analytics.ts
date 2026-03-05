import api from './axios';
import type {
  MonthlySummaryResponse,
  TopCategoryResponse,
  SpendingTrendResponse,
  AnomalyResponse,
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
};
