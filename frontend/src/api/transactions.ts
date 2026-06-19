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
    return Promise.resolve({
      content: [
        { id: 1, amount: 2000, type: 'EXPENSE', categoryName: 'Housing', date: '2026-06-15', description: 'Rent Payment' },
        { id: 2, amount: 4500, type: 'INCOME', categoryName: 'Salary', date: '2026-06-14', description: 'Bi-weekly Paycheck' },
        { id: 3, amount: 120.50, type: 'EXPENSE', categoryName: 'Food & Dining', date: '2026-06-12', description: 'Whole Foods Market' },
        { id: 4, amount: 50, type: 'EXPENSE', categoryName: 'Transportation', date: '2026-06-10', description: 'Uber Ride' },
      ],
      totalElements: 4,
      totalPages: 1,
      size: 10,
      number: 0
    } as unknown as PagedResponse<TransactionResponse>);
  },

  create: (data: TransactionRequest) =>
    api.post<TransactionResponse>('/transactions', data).then((r) => r.data),

  update: (id: number, data: TransactionRequest) =>
    api.put<TransactionResponse>(`/transactions/${id}`, data).then((r) => r.data),

  delete: (id: number) =>
    api.delete(`/transactions/${id}`).then((r) => r.data),
};
