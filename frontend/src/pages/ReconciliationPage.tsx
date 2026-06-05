import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  ArrowRightLeft,
  BadgeCheck,
  CalendarDays,
  ChevronLeft,
  ChevronRight,
  CircleAlert,
  Database,
  Download,
  FileSpreadsheet,
  Plus,
  RefreshCw,
  Sparkles,
  Target,
  Upload,
  Wand2,
  X,
} from 'lucide-react';
import {
  reconciliationApi,
  type ImportExternalTransactionItem,
  type ReconciliationBatchResponse,
  type TransactionMatchResponse,
} from '../api/reconciliation';
import { useToast } from '../context/ToastContext';
import { formatCurrency, formatDate } from '../utils/formatters';
import Modal from '../components/ui/Modal';

const PAGE_SIZE = 8;

function statusStyles(status: string) {
  switch (status) {
    case 'COMPLETED':
      return 'bg-emerald-50 text-emerald-700 dark:bg-emerald-900/20 dark:text-emerald-300';
    case 'IN_PROGRESS':
      return 'bg-blue-50 text-blue-700 dark:bg-blue-900/20 dark:text-blue-300';
    case 'FAILED':
      return 'bg-red-50 text-red-700 dark:bg-red-900/20 dark:text-red-300';
    default:
      return 'bg-amber-50 text-amber-700 dark:bg-amber-900/20 dark:text-amber-300';
  }
}

function matchBadge(matchStatus: string) {
  switch (matchStatus) {
    case 'EXACT_MATCH':
      return 'bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-300';
    case 'PARTIAL_MATCH':
      return 'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-300';
    default:
      return 'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-300';
  }
}

function blankItem(): ImportExternalTransactionItem {
  return {
    externalId: '',
    amount: 0,
    transactionDate: new Date().toISOString().slice(0, 10),
    description: '',
  };
}

export default function ReconciliationPage() {
  const { toast } = useToast();
  const [batches, setBatches] = useState<ReconciliationBatchResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [selectedBatchId, setSelectedBatchId] = useState<number | null>(null);
  const [selectedBatch, setSelectedBatch] = useState<ReconciliationBatchResponse | null>(null);
  const [matches, setMatches] = useState<TransactionMatchResponse[]>([]);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [batchDate, setBatchDate] = useState(new Date().toISOString().slice(0, 10));
  const [creatingBatch, setCreatingBatch] = useState(false);
  const [reconciling, setReconciling] = useState(false);
  const [importOpen, setImportOpen] = useState(false);
  const [importSource, setImportSource] = useState('BANK');
  const [importRows, setImportRows] = useState<ImportExternalTransactionItem[]>([blankItem()]);
  const [importing, setImporting] = useState(false);
  const [exporting, setExporting] = useState(false);

  const selectedBatchFromList = useMemo(
    () => batches.find((batch) => batch.id === selectedBatchId) ?? null,
    [batches, selectedBatchId],
  );

  const currentBatch = selectedBatch ?? selectedBatchFromList;

  const loadBatches = useCallback(async (nextPage = page) => {
    setLoading(true);
    try {
      const response = await reconciliationApi.listBatches({ page: nextPage, size: PAGE_SIZE });
      setBatches(response.content);
      setTotalPages(response.totalPages);
      setPage(response.number);
      if (!selectedBatchId && response.content.length > 0) {
        setSelectedBatchId(response.content[0].id);
      }
    } catch {
      toast('error', 'Failed to load reconciliation batches');
    } finally {
      setLoading(false);
    }
  }, [page, selectedBatchId, toast]);

  const loadBatchDetail = useCallback(async (batchId: number) => {
    setLoadingDetail(true);
    try {
      const [batch, batchMatches] = await Promise.all([
        reconciliationApi.getBatch(batchId),
        reconciliationApi.getMatches(batchId),
      ]);
      setSelectedBatch(batch);
      setMatches(batchMatches);
    } catch {
      toast('error', 'Failed to load batch details');
    } finally {
      setLoadingDetail(false);
    }
  }, [toast]);

  useEffect(() => {
    loadBatches(0);
  }, []);

  useEffect(() => {
    if (selectedBatchId) {
      loadBatchDetail(selectedBatchId);
    } else {
      setSelectedBatch(null);
      setMatches([]);
    }
  }, [selectedBatchId, loadBatchDetail]);

  function updateImportRow(index: number, key: keyof ImportExternalTransactionItem, value: string) {
    setImportRows((prev) => prev.map((row, rowIndex) => {
      if (rowIndex !== index) return row;
      if (key === 'amount') {
        return { ...row, amount: Number(value) };
      }
      return { ...row, [key]: value } as ImportExternalTransactionItem;
    }));
  }

  function addImportRow() {
    setImportRows((prev) => [...prev, blankItem()]);
  }

  function removeImportRow(index: number) {
    setImportRows((prev) => prev.filter((_, rowIndex) => rowIndex !== index));
  }

  async function handleCreateBatch() {
    setCreatingBatch(true);
    try {
      const batch = await reconciliationApi.createBatch(batchDate);
      toast('success', `Reconciliation batch created for ${batchDate}`);
      setSelectedBatchId(batch.id);
      await loadBatches(0);
    } catch {
      toast('error', 'Failed to create reconciliation batch');
    } finally {
      setCreatingBatch(false);
    }
  }

  async function handleImport() {
    if (importRows.length === 0) {
      toast('warning', 'Add at least one external transaction');
      return;
    }

    setImporting(true);
    try {
      const payload = {
        source: importSource,
        transactions: importRows
          .filter((row) => row.externalId.trim() && row.amount > 0)
          .map((row) => ({
            externalId: row.externalId.trim(),
            amount: Number(row.amount),
            transactionDate: row.transactionDate,
            description: row.description?.trim() || undefined,
          })),
      };

      if (payload.transactions.length === 0) {
        toast('warning', 'Enter valid transaction rows before importing');
        setImporting(false);
        return;
      }

      const response = await reconciliationApi.importTransactions(payload);
      toast('success', `${response.count} external transactions imported`);
      setImportOpen(false);
      setImportRows([blankItem()]);
      await loadBatches(page);
    } catch {
      toast('error', 'Failed to import external transactions');
    } finally {
      setImporting(false);
    }
  }

  async function handleReconcile() {
    if (!currentBatch) {
      toast('warning', 'Select or create a batch first');
      return;
    }

    setReconciling(true);
    try {
      const updated = await reconciliationApi.reconcile(currentBatch.id, currentBatch.batchDate);
      toast('success', 'Reconciliation completed');
      setSelectedBatch(updated);
      await loadBatches(page);
      await loadBatchDetail(updated.id);
    } catch {
      toast('error', 'Failed to run reconciliation');
    } finally {
      setReconciling(false);
    }
  }

  async function handleExport() {
    if (!currentBatch) {
      toast('warning', 'Select a batch to export');
      return;
    }

    setExporting(true);
    try {
      const blob = await reconciliationApi.exportBatch(currentBatch.id, 'csv');
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `reconciliation-batch-${currentBatch.id}.csv`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      toast('success', 'Export started');
    } catch (e) {
      toast('error', 'Failed to export reconciliation batch');
    } finally {
      setExporting(false);
    }
  }

  const exactMatches = matches.filter((match) => match.matchStatus === 'EXACT_MATCH').length;
  const partialMatches = matches.filter((match) => match.matchStatus === 'PARTIAL_MATCH').length;
  const unmatched = matches.filter((match) => match.matchStatus === 'NO_MATCH').length;

  return (
    <div className="space-y-6 page-enter">
      <div className="relative overflow-hidden rounded-2xl border border-gray-200 dark:border-slate-700 bg-gradient-to-br from-slate-950 via-slate-900 to-brand-950 text-white shadow-xl">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(56,189,248,0.28),transparent_35%),radial-gradient(circle_at_bottom_left,rgba(99,102,241,0.24),transparent_30%)]" />
        <div className="relative flex flex-col gap-6 p-6 lg:p-8 xl:flex-row xl:items-end xl:justify-between">
          <div className="max-w-3xl space-y-4">
            <div className="inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.2em] text-slate-200">
              <Sparkles className="h-3.5 w-3.5" />
              Daily settlement control center
            </div>
            <div>
              <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">Transaction Reconciliation</h1>
              <p className="mt-2 max-w-2xl text-sm leading-6 text-slate-300 sm:text-base">
                Create settlement batches, import external bank data, and review match quality in a single workflow.
                High-confidence matches are surfaced first, while variances stay visible for manual review.
              </p>
            </div>
            <div className="flex flex-wrap gap-3 text-sm text-slate-200">
              <span className="rounded-full bg-white/10 px-3 py-1.5">{batches.length} batches</span>
              <span className="rounded-full bg-white/10 px-3 py-1.5">{matches.length} matches in view</span>
              <span className="rounded-full bg-white/10 px-3 py-1.5">{exactMatches} exact matches</span>
            </div>
          </div>
          <div className="grid gap-3 sm:grid-cols-3 xl:min-w-[420px]">
            <button onClick={() => setImportOpen(true)} className="flex items-center justify-center gap-2 rounded-xl border border-white/15 bg-white/10 px-4 py-3 text-sm font-semibold text-white transition hover:bg-white/15">
              <Upload className="h-4 w-4" /> Import External Data
            </button>
            <button onClick={handleCreateBatch} disabled={creatingBatch} className="flex items-center justify-center gap-2 rounded-xl bg-brand-500 px-4 py-3 text-sm font-semibold text-white transition hover:bg-brand-600 disabled:opacity-60">
              <Plus className="h-4 w-4" /> {creatingBatch ? 'Creating…' : 'New Batch'}
            </button>
            <button onClick={handleReconcile} disabled={reconciling || !currentBatch} className="flex items-center justify-center gap-2 rounded-xl border border-white/15 bg-white text-sm font-semibold text-slate-900 transition hover:bg-slate-100 disabled:opacity-60">
              <RefreshCw className={`h-4 w-4 ${reconciling ? 'animate-spin' : ''}`} /> {reconciling ? 'Reconciling…' : 'Run Match'}
            </button>
          </div>
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-4">
        <div className="card p-5">
          <div className="flex items-center gap-2 text-sm text-gray-500 dark:text-slate-400"><Database className="h-4 w-4" /> Selected Batch</div>
          <p className="mt-2 text-2xl font-bold text-gray-900 dark:text-white">{currentBatch ? formatDate(currentBatch.batchDate) : 'None'}</p>
          <p className="mt-1 text-sm text-gray-500 dark:text-slate-400">{currentBatch ? `Batch #${currentBatch.id}` : 'Create or select a batch to inspect details'}</p>
        </div>
        <div className="card p-5">
          <div className="flex items-center gap-2 text-sm text-gray-500 dark:text-slate-400"><BadgeCheck className="h-4 w-4" /> Match Rate</div>
          <p className="mt-2 text-2xl font-bold text-gray-900 dark:text-white">{currentBatch ? `${currentBatch.matchPercentage.toFixed(1)}%` : '—'}</p>
          <p className="mt-1 text-sm text-gray-500 dark:text-slate-400">Internal transactions matched</p>
        </div>
        <div className="card p-5">
          <div className="flex items-center gap-2 text-sm text-gray-500 dark:text-slate-400"><CircleAlert className="h-4 w-4" /> Discrepancy</div>
          <p className="mt-2 text-2xl font-bold text-gray-900 dark:text-white">{currentBatch ? formatCurrency(Number(currentBatch.discrepancyAmount ?? 0)) : '—'}</p>
          <p className="mt-1 text-sm text-gray-500 dark:text-slate-400">Variance across the batch</p>
        </div>
        <div className="card p-5">
          <div className="flex items-center gap-2 text-sm text-gray-500 dark:text-slate-400"><ArrowRightLeft className="h-4 w-4" /> Match Breakdown</div>
          <p className="mt-2 text-2xl font-bold text-gray-900 dark:text-white">{exactMatches + partialMatches}/{matches.length || 0}</p>
          <p className="mt-1 text-sm text-gray-500 dark:text-slate-400">{partialMatches} partial, {unmatched} unmatched</p>
        </div>
      </div>

      <div className="grid gap-6 xl:grid-cols-[380px_minmax(0,1fr)]">
        <div className="space-y-6">
          <div className="card p-5">
            <div className="flex items-center justify-between gap-3">
              <div>
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Batch Workflow</h2>
                <p className="text-sm text-gray-500 dark:text-slate-400">Create a daily batch and run reconciliation for that date.</p>
              </div>
              <CalendarDays className="h-5 w-5 text-brand-600" />
            </div>

            <div className="mt-5 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Batch date</label>
                <input type="date" className="input-field" value={batchDate} onChange={(e) => setBatchDate(e.target.value)} />
              </div>
              <div className="grid grid-cols-2 gap-3">
                <button onClick={handleCreateBatch} disabled={creatingBatch} className="btn-primary flex items-center justify-center gap-2">
                  <Plus className="h-4 w-4" /> {creatingBatch ? 'Creating…' : 'Create Batch'}
                </button>
                <button onClick={handleReconcile} disabled={reconciling || !currentBatch} className="btn-secondary flex items-center justify-center gap-2">
                  <Wand2 className={`h-4 w-4 ${reconciling ? 'animate-spin' : ''}`} /> {reconciling ? 'Running…' : 'Reconcile'}
                </button>
              </div>
              <button onClick={() => setImportOpen(true)} className="btn-secondary flex w-full items-center justify-center gap-2">
                <Upload className="h-4 w-4" /> Import External Transactions
              </button>
            </div>
          </div>

          <div className="card p-5">
            <div className="flex items-center justify-between mb-4">
              <div>
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Batch Summary</h2>
                <p className="text-sm text-gray-500 dark:text-slate-400">Selected batch health snapshot.</p>
              </div>
              <Target className="h-5 w-5 text-brand-600" />
            </div>

            {currentBatch ? (
              <div className="space-y-4">
                <div>
                  <div className="mb-2 flex items-center justify-between text-sm">
                    <span className="text-gray-500 dark:text-slate-400">Status</span>
                    <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${statusStyles(currentBatch.status)}`}>{currentBatch.status}</span>
                  </div>
                  <div className="h-2 rounded-full bg-gray-100 dark:bg-slate-700 overflow-hidden">
                    <div className="h-full rounded-full bg-gradient-to-r from-brand-500 to-cyan-500 transition-all" style={{ width: `${currentBatch.matchPercentage}%` }} />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3 text-sm">
                  <div className="rounded-xl bg-gray-50 dark:bg-slate-700/40 p-3">
                    <div className="text-gray-500 dark:text-slate-400">Total</div>
                    <div className="mt-1 text-lg font-semibold text-gray-900 dark:text-white">{currentBatch.totalTransactions}</div>
                  </div>
                  <div className="rounded-xl bg-gray-50 dark:bg-slate-700/40 p-3">
                    <div className="text-gray-500 dark:text-slate-400">Matched</div>
                    <div className="mt-1 text-lg font-semibold text-emerald-600 dark:text-emerald-400">{currentBatch.matchedTransactions}</div>
                  </div>
                  <div className="rounded-xl bg-gray-50 dark:bg-slate-700/40 p-3">
                    <div className="text-gray-500 dark:text-slate-400">Unmatched</div>
                    <div className="mt-1 text-lg font-semibold text-amber-600 dark:text-amber-400">{currentBatch.unmatchedTransactions}</div>
                  </div>
                  <div className="rounded-xl bg-gray-50 dark:bg-slate-700/40 p-3">
                    <div className="text-gray-500 dark:text-slate-400">Created</div>
                    <div className="mt-1 text-sm font-medium text-gray-900 dark:text-white">{formatDate(currentBatch.createdAt)}</div>
                  </div>
                </div>
              </div>
            ) : (
              <div className="rounded-xl border border-dashed border-gray-200 dark:border-slate-700 p-6 text-center text-sm text-gray-500 dark:text-slate-400">
                Create a batch to inspect reconciliation details.
              </div>
            )}
          </div>
        </div>

        <div className="space-y-6">
          <div className="card overflow-hidden">
            <div className="flex flex-col gap-4 border-b border-gray-200 dark:border-slate-700 p-5 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Batches</h2>
                <p className="text-sm text-gray-500 dark:text-slate-400">Click a batch to load its match details.</p>
              </div>
              <button onClick={() => loadBatches(page)} className="btn-secondary flex items-center gap-2 self-start">
                <RefreshCw className="h-4 w-4" /> Refresh
              </button>
            </div>

            {loading ? (
              <div className="flex justify-center py-16"><div className="h-8 w-8 rounded-full border-4 border-brand-600 border-t-transparent animate-spin" /></div>
            ) : batches.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-16 text-center">
                <FileSpreadsheet className="h-12 w-12 text-gray-300 dark:text-slate-600 mb-3" />
                <h3 className="text-base font-semibold text-gray-700 dark:text-slate-300">No reconciliation batches yet</h3>
                <p className="mt-1 text-sm text-gray-400 dark:text-slate-500">Create the first batch for a settlement date to begin matching.</p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-200 dark:border-slate-700 bg-gray-50 dark:bg-slate-800/50 text-left text-gray-500 dark:text-slate-400">
                      <th className="px-4 py-3 font-medium">Batch Date</th>
                      <th className="px-4 py-3 font-medium">Status</th>
                      <th className="px-4 py-3 font-medium">Match %</th>
                      <th className="px-4 py-3 font-medium">Transactions</th>
                      <th className="px-4 py-3 font-medium text-right">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {batches.map((batch) => {
                      const active = batch.id === currentBatch?.id;
                      return (
                        <tr key={batch.id} className={`border-b border-gray-100 dark:border-slate-700/50 transition-colors ${active ? 'bg-brand-50/60 dark:bg-brand-900/10' : 'hover:bg-gray-50 dark:hover:bg-slate-800/50'}`}>
                          <td className="px-4 py-3">
                            <button onClick={() => setSelectedBatchId(batch.id)} className="flex items-center gap-2 text-left font-medium text-gray-900 dark:text-white">
                              <CalendarDays className="h-4 w-4 text-gray-400" /> {formatDate(batch.batchDate)}
                            </button>
                          </td>
                          <td className="px-4 py-3">
                            <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${statusStyles(batch.status)}`}>{batch.status}</span>
                          </td>
                          <td className="px-4 py-3 text-gray-700 dark:text-slate-300">{batch.matchPercentage.toFixed(1)}%</td>
                          <td className="px-4 py-3 text-gray-700 dark:text-slate-300">{batch.totalTransactions}</td>
                          <td className="px-4 py-3 text-right">
                            <button onClick={() => setSelectedBatchId(batch.id)} className="inline-flex items-center gap-2 rounded-lg px-3 py-1.5 text-xs font-medium text-brand-600 hover:bg-brand-50 dark:hover:bg-brand-900/20">
                              View <ChevronRight className="h-3.5 w-3.5" />
                            </button>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}

            {totalPages > 1 && (
              <div className="flex items-center justify-between border-t border-gray-200 dark:border-slate-700 px-4 py-3">
                <span className="text-sm text-gray-500 dark:text-slate-400">Page {page + 1} of {totalPages}</span>
                <div className="flex gap-2">
                  <button disabled={page === 0} onClick={() => loadBatches(page - 1)} className="p-2 rounded-lg border border-gray-200 dark:border-slate-700 disabled:opacity-40 hover:bg-gray-50 dark:hover:bg-slate-800 transition-colors">
                    <ChevronLeft className="h-4 w-4" />
                  </button>
                  <button disabled={page >= totalPages - 1} onClick={() => loadBatches(page + 1)} className="p-2 rounded-lg border border-gray-200 dark:border-slate-700 disabled:opacity-40 hover:bg-gray-50 dark:hover:bg-slate-800 transition-colors">
                    <ChevronRight className="h-4 w-4" />
                  </button>
                </div>
              </div>
            )}
          </div>

          <div className="card overflow-hidden">
            <div className="flex items-center justify-between border-b border-gray-200 dark:border-slate-700 px-5 py-4">
              <div>
                <h2 className="text-lg font-semibold text-gray-900 dark:text-white">Match Review</h2>
                <p className="text-sm text-gray-500 dark:text-slate-400">
                  {loadingDetail ? 'Loading batch details…' : currentBatch ? `Showing ${matches.length} transaction matches for ${formatDate(currentBatch.batchDate)}` : 'Select a batch to review matches.'}
                </p>
              </div>
              {currentBatch && (
                <div className="flex items-center gap-2">
                  <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${statusStyles(currentBatch.status)}`}>{currentBatch.status}</span>
                  <button onClick={handleExport} disabled={exporting} className="btn-secondary flex items-center gap-2">
                    <Download className="h-4 w-4" /> {exporting ? 'Exporting…' : 'Export CSV'}
                  </button>
                  <button onClick={() => setImportOpen(true)} className="btn-secondary flex items-center gap-2">
                    <Download className="h-4 w-4" /> Import More
                  </button>
                </div>
              )}
            </div>

            {currentBatch && matches.length > 0 ? (
              <div className="overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b border-gray-200 dark:border-slate-700 bg-gray-50 dark:bg-slate-800/50 text-left text-gray-500 dark:text-slate-400">
                      <th className="px-4 py-3 font-medium">Internal ID</th>
                      <th className="px-4 py-3 font-medium">External ID</th>
                      <th className="px-4 py-3 font-medium">Match</th>
                      <th className="px-4 py-3 font-medium">Confidence</th>
                      <th className="px-4 py-3 font-medium">Variance</th>
                      <th className="px-4 py-3 font-medium">Matched At</th>
                    </tr>
                  </thead>
                  <tbody>
                    {matches.map((match) => (
                      <tr key={match.id} className="border-b border-gray-100 dark:border-slate-700/50 hover:bg-gray-50 dark:hover:bg-slate-800/50 transition-colors">
                        <td className="px-4 py-3 font-medium text-gray-900 dark:text-white">#{match.internalTransactionId}</td>
                        <td className="px-4 py-3 text-gray-600 dark:text-slate-300">{match.externalTransactionId}</td>
                        <td className="px-4 py-3">
                          <span className={`rounded-full px-2.5 py-1 text-xs font-semibold ${matchBadge(match.matchStatus)}`}>{match.matchStatus}</span>
                        </td>
                        <td className="px-4 py-3 text-gray-700 dark:text-slate-300">{Number(match.matchConfidence).toFixed(1)}%</td>
                        <td className="px-4 py-3">
                          <div className="font-medium text-gray-900 dark:text-white">{formatCurrency(Number(match.varianceAmount ?? 0))}</div>
                          <div className="text-xs text-gray-400 dark:text-slate-500">{Number(match.variancePercentage ?? 0).toFixed(2)}%</div>
                        </td>
                        <td className="px-4 py-3 text-gray-500 dark:text-slate-400 whitespace-nowrap">{formatDate(match.matchedAt)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="flex flex-col items-center justify-center py-16 text-center">
                <BadgeCheck className="h-12 w-12 text-gray-300 dark:text-slate-600 mb-3" />
                <h3 className="text-base font-semibold text-gray-700 dark:text-slate-300">No match data loaded</h3>
                <p className="mt-1 text-sm text-gray-400 dark:text-slate-500">Pick a batch or run reconciliation to generate match details.</p>
              </div>
            )}
          </div>
        </div>
      </div>

      <Modal open={importOpen} onClose={() => setImportOpen(false)} title="Import External Transactions" maxWidth="max-w-3xl">
        <div className="space-y-5">
          <div className="grid gap-4 md:grid-cols-2">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Source</label>
              <select className="input-field" value={importSource} onChange={(e) => setImportSource(e.target.value)}>
                <option value="BANK">Bank</option>
                <option value="CSV_UPLOAD">CSV Upload</option>
                <option value="MANUAL">Manual Entry</option>
                <option value="API_SYNC">API Sync</option>
              </select>
            </div>
            <div className="rounded-xl border border-dashed border-gray-200 dark:border-slate-700 bg-gray-50/70 dark:bg-slate-800/40 p-4">
              <p className="text-sm font-medium text-gray-900 dark:text-white">Workflow tip</p>
              <p className="mt-1 text-sm text-gray-500 dark:text-slate-400">
                Import external rows first, then create or select the batch for the settlement date and run reconciliation.
              </p>
            </div>
          </div>

          <div className="space-y-3 max-h-[50vh] overflow-y-auto pr-1">
            {importRows.map((row, index) => (
              <div key={`${row.externalId}-${index}`} className="grid gap-3 rounded-xl border border-gray-200 dark:border-slate-700 p-4 md:grid-cols-[1.2fr_0.7fr_0.8fr_1.2fr_auto]">
                <div>
                  <label className="block text-xs font-medium uppercase tracking-wide text-gray-500 dark:text-slate-400 mb-1">External ID</label>
                  <input className="input-field" value={row.externalId} onChange={(e) => updateImportRow(index, 'externalId', e.target.value)} placeholder="BANK-0001" />
                </div>
                <div>
                  <label className="block text-xs font-medium uppercase tracking-wide text-gray-500 dark:text-slate-400 mb-1">Amount</label>
                  <input type="number" step="0.01" min="0.01" className="input-field" value={row.amount || ''} onChange={(e) => updateImportRow(index, 'amount', e.target.value)} placeholder="0.00" />
                </div>
                <div>
                  <label className="block text-xs font-medium uppercase tracking-wide text-gray-500 dark:text-slate-400 mb-1">Date</label>
                  <input type="date" className="input-field" value={row.transactionDate} onChange={(e) => updateImportRow(index, 'transactionDate', e.target.value)} />
                </div>
                <div>
                  <label className="block text-xs font-medium uppercase tracking-wide text-gray-500 dark:text-slate-400 mb-1">Description</label>
                  <input className="input-field" value={row.description || ''} onChange={(e) => updateImportRow(index, 'description', e.target.value)} placeholder="Merchant / memo" />
                </div>
                <div className="flex items-end justify-end">
                  <button type="button" onClick={() => removeImportRow(index)} disabled={importRows.length === 1} className="rounded-lg p-2 text-gray-400 hover:bg-gray-100 hover:text-red-600 dark:hover:bg-slate-700 disabled:opacity-40">
                    <X className="h-4 w-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div className="flex flex-wrap items-center justify-between gap-3 border-t border-gray-200 dark:border-slate-700 pt-4">
            <button type="button" onClick={addImportRow} className="btn-secondary flex items-center gap-2">
              <Plus className="h-4 w-4" /> Add Row
            </button>
            <div className="flex gap-3">
              <button type="button" onClick={() => setImportOpen(false)} className="btn-secondary">Cancel</button>
              <button type="button" onClick={handleImport} disabled={importing} className="btn-primary flex items-center gap-2">
                <Upload className="h-4 w-4" /> {importing ? 'Importing…' : 'Import Transactions'}
              </button>
            </div>
          </div>
        </div>
      </Modal>
    </div>
  );
}

 