import React from 'react';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { vi } from 'vitest';

import ReconciliationPage from '../ReconciliationPage';
import { ToastProvider } from '../../context/ToastContext';

// Mock the reconciliation API module
vi.mock('../../api/reconciliation', () => {
  return {
    reconciliationApi: {
      listBatches: vi.fn(async () => ({
        content: [
          {
            id: 1,
            batchDate: new Date().toISOString().slice(0, 10),
            status: 'COMPLETED',
            totalTransactions: 5,
            matchedTransactions: 4,
            unmatchedTransactions: 1,
            discrepancyAmount: 0,
            matchPercentage: 80,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
          },
        ],
        totalElements: 1,
        totalPages: 1,
        size: 8,
        number: 0,
        last: true,
      })),
      getBatch: vi.fn(async (id: number) => ({
        id,
        batchDate: new Date().toISOString().slice(0, 10),
        status: 'COMPLETED',
        totalTransactions: 5,
        matchedTransactions: 4,
        unmatchedTransactions: 1,
        discrepancyAmount: 0,
        matchPercentage: 80,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      })),
      getMatches: vi.fn(async () => []),
      importTransactions: vi.fn(async () => ({ count: 1, message: 'ok' })),
      createBatch: vi.fn(async (batchDate: string) => ({
        id: 99,
        batchDate,
        status: 'PENDING',
        totalTransactions: 0,
        matchedTransactions: 0,
        unmatchedTransactions: 0,
        discrepancyAmount: 0,
        matchPercentage: 0,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      })),
      reconcile: vi.fn(async (batchId: number) => ({
        id: batchId,
        batchDate: new Date().toISOString().slice(0, 10),
        status: 'COMPLETED',
        totalTransactions: 5,
        matchedTransactions: 5,
        unmatchedTransactions: 0,
        discrepancyAmount: 0,
        matchPercentage: 100,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      })),
    },
  };
});

describe('ReconciliationPage', () => {
  test('renders header and loads batches', async () => {
    render(
      <ToastProvider>
        <ReconciliationPage />
      </ToastProvider>,
    );

    expect(screen.getByText(/Transaction Reconciliation/i)).toBeInTheDocument();

    // wait for batches to load and the batches count to update
    await waitFor(() => {
      expect(screen.getByText(/1 batches/i)).toBeInTheDocument();
    });
  });

  test('creates a new batch when clicking New Batch', async () => {
    const { reconciliationApi } = await import('../../api/reconciliation');

    render(
      <ToastProvider>
        <ReconciliationPage />
      </ToastProvider>,
    );

    // click New Batch toolbar button
    const newBatchButtons = await screen.findAllByText(/New Batch/i);
    expect(newBatchButtons.length).toBeGreaterThan(0);

    fireEvent.click(newBatchButtons[0]);

    await waitFor(() => {
      expect(reconciliationApi.createBatch).toHaveBeenCalled();
    });

    // success toast should appear
    await waitFor(() => {
      expect(screen.getByText(/Reconciliation batch created for/i)).toBeInTheDocument();
    });
  });
});
