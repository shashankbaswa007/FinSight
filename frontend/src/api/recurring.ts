import api from './axios';
import type { RecurringTransactionRequest, RecurringTransactionResponse } from '../types';

export const recurringApi = {
  list: () =>
    api.get<RecurringTransactionResponse[]>('/recurring-transactions').then((r) => r.data),

  create: (data: RecurringTransactionRequest) =>
    api.post<RecurringTransactionResponse>('/recurring-transactions', data).then((r) => r.data),

  deactivate: (id: number) =>
    api.delete(`/recurring-transactions/${id}`).then((r) => r.data),
};
