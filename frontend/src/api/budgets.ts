import api from './axios';
import type { BudgetRequest, BudgetResponse, BudgetStatusResponse } from '../types';

export const budgetApi = {
  list: () =>
    api.get<BudgetResponse[]>('/budgets').then((r) => r.data),

  create: (data: BudgetRequest) =>
    api.post<BudgetResponse>('/budgets', data).then((r) => r.data),

  update: (id: number, data: BudgetRequest) =>
    api.put<BudgetResponse>(`/budgets/${id}`, data).then((r) => r.data),

  delete: (id: number) =>
    api.delete(`/budgets/${id}`).then((r) => r.data),

  status: (month: number, year: number) =>
    api.get<BudgetStatusResponse[]>(`/budgets/status?month=${month}&year=${year}`).then((r) => r.data),
};
