import api from './axios';
import type { CategoryRequest, CategoryResponse } from '../types';

export const categoryApi = {
  list: () => Promise.resolve([
    { id: 1, name: 'Housing', type: 'EXPENSE' },
    { id: 2, name: 'Food & Dining', type: 'EXPENSE' },
    { id: 3, name: 'Transportation', type: 'EXPENSE' },
    { id: 4, name: 'Salary', type: 'INCOME' }
  ] as CategoryResponse[]),

  create: (data: CategoryRequest) =>
    api.post<CategoryResponse>('/categories', data).then((r) => r.data),
};
