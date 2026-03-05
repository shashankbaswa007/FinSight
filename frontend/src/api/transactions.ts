import api from './axios';
import type { TransactionRequest, TransactionResponse, PagedResponse } from '../types';

export interface TransactionFilters {
  type?: 'INCOME' | 'EXPENSE';
  categoryId?: number;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

export const transactionApi = {
  list: (filters: TransactionFilters = {}) => {
    const params = new URLSearchParams();
    if (filters.type) params.append('type', filters.type);
    if (filters.categoryId) params.append('categoryId', String(filters.categoryId));
    if (filters.startDate) params.append('startDate', filters.startDate);
    if (filters.endDate) params.append('endDate', filters.endDate);
    params.append('page', String(filters.page ?? 0));
    params.append('size', String(filters.size ?? 10));
    return api.get<PagedResponse<TransactionResponse>>(`/transactions?${params}`).then((r) => r.data);
  },

  create: (data: TransactionRequest) =>
    api.post<TransactionResponse>('/transactions', data).then((r) => r.data),

  update: (id: number, data: TransactionRequest) =>
    api.put<TransactionResponse>(`/transactions/${id}`, data).then((r) => r.data),

  delete: (id: number) =>
    api.delete(`/transactions/${id}`).then((r) => r.data),
};
