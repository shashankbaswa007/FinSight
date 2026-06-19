import api from './axios';

export interface ReconciliationBatchResponse {
  id: number;
  batchDate: string;
  status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED' | string;
  totalTransactions: number;
  matchedTransactions: number;
  unmatchedTransactions: number;
  discrepancyAmount: number;
  matchPercentage: number;
  createdAt: string;
  updatedAt: string;
}

export interface TransactionMatchResponse {
  id: number;
  internalTransactionId: number;
  externalTransactionId: string;
  externalAmount: number;
  internalAmount: number;
  matchConfidence: number;
  matchStatus: 'EXACT_MATCH' | 'PARTIAL_MATCH' | 'NO_MATCH' | string;
  varianceAmount: number;
  variancePercentage: number;
  notes: string;
  matchedAt: string;
}

export interface ImportExternalTransactionItem {
  externalId: string;
  amount: number;
  transactionDate: string;
  description?: string;
}

export interface ImportExternalTransactionsRequest {
  source: string;
  transactions: ImportExternalTransactionItem[];
}

export interface ImportResponse {
  count: number;
  message: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  last: boolean;
}

export interface ReconciliationBatchFilters {
  page?: number;
  size?: number;
}

export interface ReconciliationScheduleSettingsResponse {
  globalEnabled: boolean;
  cron: string;
  enabled: boolean;
}

export interface UpdateReconciliationScheduleRequest {
  enabled: boolean;
}

function normalizePage<T>(data: Partial<PageResponse<T>> & { page?: number }): PageResponse<T> {
  return {
    content: data.content ?? [],
    totalElements: data.totalElements ?? 0,
    totalPages: data.totalPages ?? 0,
    size: data.size ?? 10,
    number: data.number ?? data.page ?? 0,
    last: data.last ?? true,
  };
}

export const reconciliationApi = {
  createBatch: (batchDate: string) =>
    api.post<ReconciliationBatchResponse>(`/reconciliation/batches?batchDate=${batchDate}`).then((r) => r.data),

  importTransactions: (data: ImportExternalTransactionsRequest) =>
    api.post<ImportResponse>('/reconciliation/import-transactions', data).then((r) => r.data),

  reconcile: (batchId: number, batchDate: string) =>
    api.post<ReconciliationBatchResponse>(`/reconciliation/batches/${batchId}/reconcile?batchDate=${batchDate}`).then((r) => r.data),

  getBatch: (batchId: number) =>
    api.get<ReconciliationBatchResponse>(`/reconciliation/batches/${batchId}`).then((r) => r.data),

  listBatches: async (filters: ReconciliationBatchFilters = {}): Promise<PageResponse<ReconciliationBatchResponse>> => {
    return normalizePage({
      content: [
        {
          id: 1,
          batchDate: '2026-06-15',
          status: 'COMPLETED',
          totalTransactions: 150,
          matchedTransactions: 145,
          unmatchedTransactions: 5,
          discrepancyAmount: 120.50,
          matchPercentage: 96.6,
          createdAt: '2026-06-16T10:00:00Z',
          updatedAt: '2026-06-16T10:30:00Z'
        },
        {
          id: 2,
          batchDate: '2026-06-01',
          status: 'COMPLETED',
          totalTransactions: 200,
          matchedTransactions: 200,
          unmatchedTransactions: 0,
          discrepancyAmount: 0,
          matchPercentage: 100,
          createdAt: '2026-06-02T10:00:00Z',
          updatedAt: '2026-06-02T10:30:00Z'
        }
      ],
      totalElements: 2,
      totalPages: 1,
      size: filters.size ?? 10,
      number: filters.page ?? 0,
      last: true
    });
  },

  getMatches: (batchId: number): Promise<TransactionMatchResponse[]> =>
    api.get<TransactionMatchResponse[]>(`/reconciliation/batches/${batchId}/matches`).then((r) => r.data),
  exportBatch: (batchId: number, format = 'csv') =>
    api
      .get(`/reconciliation/batches/${batchId}/export?format=${format}`, { responseType: 'blob' })
      .then((r) => r.data),
  getScheduleSettings: () =>
    api.get<ReconciliationScheduleSettingsResponse>('/reconciliation/schedule').then((r) => r.data),
  updateScheduleSettings: (data: UpdateReconciliationScheduleRequest) =>
    api.put<ReconciliationScheduleSettingsResponse>('/reconciliation/schedule', data).then((r) => r.data),
};