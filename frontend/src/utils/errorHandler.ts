import { AxiosError } from 'axios';
import { getCorrelationId } from '../api/axios';

/**
 * Show an error toast with optional correlation ID from the most recent request.
 * 
 * Usage:
 *   try {
 *     await api.post(...)
 *   } catch (error) {
 *     showErrorWithCorrelation(toast, 'Failed to create item', error);
 *   }
 * 
 * The error message displayed includes the correlation ID if available,
 * making it easy for users to reference requests in support/debugging.
 */
export function showErrorWithCorrelation(
  toast: (type: 'error' | 'warning' | 'success' | 'info', message: string, correlationId?: string) => void,
  message: string,
  error?: unknown
) {
  const correlationId = getCorrelationId();

  // Extract user-friendly error message if available
  let displayMessage = message;
  if (error instanceof AxiosError && error.response?.data?.message) {
    displayMessage = error.response.data.message;
  }

  toast('error', displayMessage, correlationId || undefined);
}
