import api from './axios';
import type { TransactionRequest, TransactionResponse, PagedResponse, BulkUploadPreviewResponse, BulkUploadCommitResponse } from '../types';

export interface TransactionFilters {
  type?: 'INCOME' | 'EXPENSE';
  categoryId?: number;
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
}

export const transactionApi = {
  list: (filters: TransactionFilters = {}) =>
    api.get<PagedResponse<TransactionResponse>>('/transactions', { params: filters }).then((r) => r.data),

  create: (data: TransactionRequest) =>
    api.post<TransactionResponse>('/transactions', data).then((r) => r.data),

  update: (id: number, data: TransactionRequest) =>
    api.put<TransactionResponse>(`/transactions/${id}`, data).then((r) => r.data),

  delete: (id: number) =>
    api.delete(`/transactions/${id}`).then((r) => r.data),

  bulkDownloadTemplate: () =>
    api.get('/transactions/bulk/template', { responseType: 'blob' }).then(r => r.data as Blob),

  bulkPreview: (file: File) => {
    const formData = new FormData();
    formData.append('file', file);
    return api.post<BulkUploadPreviewResponse>('/transactions/bulk/preview', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }).then(r => r.data);
  },

  bulkCommit: (preview: BulkUploadPreviewResponse) =>
    api.post<BulkUploadCommitResponse>('/transactions/bulk/commit', preview).then(r => r.data),
};

