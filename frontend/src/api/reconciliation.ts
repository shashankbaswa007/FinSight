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
    const page = filters.page ?? 0;
    const size = filters.size ?? 10;
    const response = await api.get<PageResponse<ReconciliationBatchResponse>>(`/reconciliation/batches?page=${page}&size=${size}`);
    return normalizePage(response.data);
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