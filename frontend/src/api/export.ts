import api from './axios';

export const exportApi = {
  transactionsCsv: (startDate?: string, endDate?: string) => {
    const params = new URLSearchParams();
    if (startDate) params.append('startDate', startDate);
    if (endDate) params.append('endDate', endDate);
    return api.get(`/export/transactions/csv?${params}`, { responseType: 'blob' }).then((r) => r.data as Blob);
  },
};
