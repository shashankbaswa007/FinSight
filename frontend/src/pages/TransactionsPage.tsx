import { useEffect, useState, useCallback } from 'react';
import { Plus, Search, Filter, ChevronLeft, ChevronRight, Pencil, Trash2 } from 'lucide-react';
import { transactionApi } from '../api/transactions';
import { categoryApi } from '../api/categories';
import type { TransactionResponse, CategoryResponse } from '../types';
import { formatCurrency } from '../utils/formatters';
import Modal from '../components/ui/Modal';

interface Filters {
  page: number;
  size: number;
  type?: 'INCOME' | 'EXPENSE';
  categoryId?: number;
  startDate?: string;
  endDate?: string;
}

const PAGE_SIZE = 10;

export default function TransactionsPage() {
  const [transactions, setTransactions] = useState<TransactionResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [filters, setFilters] = useState<Filters>({ page: 0, size: PAGE_SIZE });
  const [showFilters, setShowFilters] = useState(false);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<TransactionResponse | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<TransactionResponse | null>(null);
  const [saving, setSaving] = useState(false);

  /* Form state */
  const [form, setForm] = useState({ amount: '', type: 'EXPENSE' as 'INCOME' | 'EXPENSE', categoryId: '', date: new Date().toISOString().slice(0, 10), description: '' });

  const fetchTransactions = useCallback(async () => {
    setLoading(true);
    try {
      const res = await transactionApi.list(filters);
      setTransactions(res.content);
      setTotalPages(res.totalPages);
    } catch { /* ignore */ } finally { setLoading(false); }
  }, [filters]);

  useEffect(() => { fetchTransactions(); }, [fetchTransactions]);
  useEffect(() => { categoryApi.list().then(setCategories).catch(() => {}); }, []);

  /* Filter-matching categories */
  const filteredCategories = form.type ? categories.filter((c) => c.type === form.type) : categories;

  function openCreate() {
    setEditing(null);
    setForm({ amount: '', type: 'EXPENSE', categoryId: '', date: new Date().toISOString().slice(0, 10), description: '' });
    setModalOpen(true);
  }

  function openEdit(tx: TransactionResponse) {
    setEditing(tx);
    const cat = categories.find((c) => c.name === tx.categoryName);
    setForm({ amount: String(tx.amount), type: tx.type as 'INCOME' | 'EXPENSE', categoryId: cat ? String(cat.id) : '', date: tx.date, description: tx.description || '' });
    setModalOpen(true);
  }

  async function handleSave(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = {
        amount: parseFloat(form.amount),
        type: form.type,
        categoryId: parseInt(form.categoryId),
        date: form.date,
        description: form.description || undefined,
      };
      if (editing) {
        await transactionApi.update(editing.id, payload);
      } else {
        await transactionApi.create(payload);
      }
      setModalOpen(false);
      fetchTransactions();
    } catch { /* ignore */ } finally { setSaving(false); }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    try {
      await transactionApi.delete(deleteTarget.id);
      setDeleteTarget(null);
      fetchTransactions();
    } catch { /* ignore */ }
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Transactions</h1>
          <p className="text-gray-500 dark:text-slate-400 mt-1">Manage your income and expenses</p>
        </div>
        <button onClick={openCreate} className="btn-primary flex items-center gap-2 self-start">
          <Plus className="h-4 w-4" /> Add Transaction
        </button>
      </div>

      {/* Filters */}
      <div className="card p-4">
        <button onClick={() => setShowFilters(!showFilters)} className="flex items-center gap-2 text-sm font-medium text-gray-600 dark:text-slate-300 hover:text-brand-600">
          <Filter className="h-4 w-4" /> {showFilters ? 'Hide Filters' : 'Show Filters'}
        </button>
        {showFilters && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-4">
            <select className="input-field" value={filters.type || ''} onChange={(e) => setFilters({ ...filters, page: 0, type: e.target.value ? e.target.value as 'INCOME'|'EXPENSE' : undefined })}>
              <option value="">All Types</option>
              <option value="INCOME">Income</option>
              <option value="EXPENSE">Expense</option>
            </select>
            <select className="input-field" value={filters.categoryId || ''} onChange={(e) => setFilters({ ...filters, page: 0, categoryId: e.target.value ? Number(e.target.value) : undefined })}>
              <option value="">All Categories</option>
              {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
            <input type="date" className="input-field" value={filters.startDate || ''} onChange={(e) => setFilters({ ...filters, page: 0, startDate: e.target.value || undefined })} placeholder="From" />
            <input type="date" className="input-field" value={filters.endDate || ''} onChange={(e) => setFilters({ ...filters, page: 0, endDate: e.target.value || undefined })} placeholder="To" />
          </div>
        )}
      </div>

      {/* Table */}
      <div className="card overflow-hidden">
        {loading ? (
          <div className="flex justify-center py-16"><div className="h-8 w-8 border-4 border-brand-600 border-t-transparent rounded-full animate-spin" /></div>
        ) : transactions.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 text-center">
            <Search className="h-10 w-10 text-gray-300 dark:text-slate-600 mb-3" />
            <p className="text-sm text-gray-400 dark:text-slate-500">No transactions found.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-200 dark:border-slate-700 bg-gray-50 dark:bg-slate-800/50">
                  <th className="text-left py-3 px-4 font-medium text-gray-500 dark:text-slate-400">Date</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500 dark:text-slate-400">Description</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500 dark:text-slate-400">Category</th>
                  <th className="text-left py-3 px-4 font-medium text-gray-500 dark:text-slate-400">Type</th>
                  <th className="text-right py-3 px-4 font-medium text-gray-500 dark:text-slate-400">Amount</th>
                  <th className="text-right py-3 px-4 font-medium text-gray-500 dark:text-slate-400">Actions</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((tx) => (
                  <tr key={tx.id} className="border-b border-gray-100 dark:border-slate-700/50 hover:bg-gray-50 dark:hover:bg-slate-800/50 transition-colors">
                    <td className="py-3 px-4 text-gray-500 dark:text-slate-400 whitespace-nowrap">{tx.date}</td>
                    <td className="py-3 px-4 text-gray-900 dark:text-slate-200 max-w-[200px] truncate">{tx.description || '—'}</td>
                    <td className="py-3 px-4">
                      <span className={tx.type === 'INCOME' ? 'badge-income' : 'badge-expense'}>{tx.categoryName}</span>
                    </td>
                    <td className="py-3 px-4">
                      <span className={`text-xs font-semibold uppercase ${tx.type === 'INCOME' ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'}`}>{tx.type}</span>
                    </td>
                    <td className={`py-3 px-4 text-right font-medium whitespace-nowrap ${tx.type === 'INCOME' ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'}`}>
                      {tx.type === 'INCOME' ? '+' : '-'}{formatCurrency(tx.amount)}
                    </td>
                    <td className="py-3 px-4 text-right whitespace-nowrap">
                      <button onClick={() => openEdit(tx)} className="p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-slate-700 text-gray-400 hover:text-brand-600 transition-colors">
                        <Pencil className="h-4 w-4" />
                      </button>
                      <button onClick={() => setDeleteTarget(tx)} className="p-1.5 rounded-lg hover:bg-gray-100 dark:hover:bg-slate-700 text-gray-400 hover:text-red-600 transition-colors ml-1">
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between px-4 py-3 border-t border-gray-200 dark:border-slate-700">
            <span className="text-sm text-gray-500 dark:text-slate-400">Page {filters.page + 1} of {totalPages}</span>
            <div className="flex gap-2">
              <button disabled={filters.page === 0} onClick={() => setFilters({ ...filters, page: filters.page - 1 })} className="p-2 rounded-lg border border-gray-200 dark:border-slate-700 disabled:opacity-40 hover:bg-gray-50 dark:hover:bg-slate-800 transition-colors">
                <ChevronLeft className="h-4 w-4" />
              </button>
              <button disabled={filters.page >= totalPages - 1} onClick={() => setFilters({ ...filters, page: filters.page + 1 })} className="p-2 rounded-lg border border-gray-200 dark:border-slate-700 disabled:opacity-40 hover:bg-gray-50 dark:hover:bg-slate-800 transition-colors">
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Create / Edit modal */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title={editing ? 'Edit Transaction' : 'New Transaction'}>
        <form onSubmit={handleSave} className="space-y-4">
          {/* Type toggle */}
          <div className="flex rounded-lg border border-gray-200 dark:border-slate-700 overflow-hidden">
            {(['EXPENSE', 'INCOME'] as const).map((t) => (
              <button key={t} type="button" onClick={() => setForm({ ...form, type: t, categoryId: '' })}
                className={`flex-1 py-2.5 text-sm font-medium transition-colors ${form.type === t ? (t === 'INCOME' ? 'bg-emerald-600 text-white' : 'bg-red-600 text-white') : 'bg-transparent text-gray-500 dark:text-slate-400 hover:bg-gray-50 dark:hover:bg-slate-800'}`}>
                {t}
              </button>
            ))}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Amount</label>
            <input type="number" step="0.01" min="0.01" required className="input-field" placeholder="0.00"
              value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Category</label>
            <select required className="input-field" value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
              <option value="">Select a category</option>
              {filteredCategories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Date</label>
            <input type="date" required className="input-field" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Description <span className="text-gray-400">(optional)</span></label>
            <input type="text" className="input-field" placeholder="e.g. Grocery shopping" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={() => setModalOpen(false)} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary">
              {saving ? 'Saving…' : editing ? 'Update' : 'Create'}
            </button>
          </div>
        </form>
      </Modal>

      {/* Delete confirmation */}
      <Modal open={!!deleteTarget} onClose={() => setDeleteTarget(null)} title="Delete Transaction">
        <p className="text-sm text-gray-600 dark:text-slate-400 mb-6">
          Are you sure you want to delete this transaction?{' '}
          <span className="font-medium text-gray-900 dark:text-white">{deleteTarget?.description || 'Untitled'}</span>{' '}
          — {formatCurrency(deleteTarget?.amount ?? 0)}. This action cannot be undone.
        </p>
        <div className="flex justify-end gap-3">
          <button onClick={() => setDeleteTarget(null)} className="btn-secondary">Cancel</button>
          <button onClick={handleDelete} className="btn-danger">Delete</button>
        </div>
      </Modal>
    </div>
  );
}
