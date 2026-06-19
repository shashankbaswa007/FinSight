import api from './axios';
import type { CategoryRequest, CategoryResponse } from '../types';

export const categoryApi = {
  list: () => Promise.resolve([
    { id: 1, name: 'Housing', type: 'EXPENSE', color: '#f43f5e' },
    { id: 2, name: 'Food & Dining', type: 'EXPENSE', color: '#10b981' },
    { id: 3, name: 'Transportation', type: 'EXPENSE', color: '#06b6d4' },
    { id: 4, name: 'Salary', type: 'INCOME', color: '#14b8a6' }
  ] as CategoryResponse[]),

  create: (data: CategoryRequest) =>
    api.post<CategoryResponse>('/categories', data).then((r) => r.data),
};
