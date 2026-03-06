import { useEffect, useState } from 'react';
import { Plus, Repeat, XCircle } from 'lucide-react';
import { recurringApi } from '../api/recurring';
import { categoryApi } from '../api/categories';
import type { RecurringTransactionResponse, CategoryResponse, RecurringFrequency } from '../types';
import { formatCurrency } from '../utils/formatters';
import { useToast } from '../context/ToastContext';
import Modal from '../components/ui/Modal';

export default function RecurringPage() {
  const { toast } = useToast();
  const [items, setItems] = useState<RecurringTransactionResponse[]>([]);
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({
    amount: '', type: 'EXPENSE' as 'INCOME' | 'EXPENSE', categoryId: '',
    description: '', frequency: 'MONTHLY' as RecurringFrequency,
    startDate: new Date().toISOString().slice(0, 10), endDate: '',
  });

  async function load() {
    setLoading(true);
    try {
      const [r, c] = await Promise.all([recurringApi.list(), categoryApi.list()]);
      setItems(r);
      setCategories(c);
    } catch {
      toast('error', 'Failed to load recurring transactions');
    } finally { setLoading(false); }
  }

  useEffect(() => { load(); }, []);

  const filteredCategories = categories.filter((c) => c.type === form.type);

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    try {
      await recurringApi.create({
        amount: parseFloat(form.amount),
        type: form.type,
        categoryId: parseInt(form.categoryId),
        description: form.description || undefined,
        frequency: form.frequency,
        startDate: form.startDate,
        endDate: form.endDate || undefined,
      });
      setModalOpen(false);
      toast('success', 'Recurring transaction created');
      load();
    } catch {
      toast('error', 'Failed to create recurring transaction');
    } finally { setSaving(false); }
  }

  async function handleDeactivate(id: number) {
    try {
      await recurringApi.deactivate(id);
      toast('success', 'Recurring transaction deactivated');
      load();
    } catch {
      toast('error', 'Failed to deactivate');
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="h-8 w-8 border-4 border-brand-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-6 page-enter">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Recurring Transactions</h1>
          <p className="text-gray-500 dark:text-slate-400 mt-1">Automate your regular income and expenses</p>
        </div>
        <button onClick={() => { setForm({ amount: '', type: 'EXPENSE', categoryId: '', description: '', frequency: 'MONTHLY', startDate: new Date().toISOString().slice(0, 10), endDate: '' }); setModalOpen(true); }} className="btn-primary flex items-center gap-2 self-start">
          <Plus className="h-4 w-4" /> Add Recurring
        </button>
      </div>

      {items.length === 0 ? (
        <div className="card p-12 text-center">
          <Repeat className="h-12 w-12 mx-auto text-gray-300 dark:text-slate-600 mb-4" />
          <h3 className="text-lg font-semibold text-gray-700 dark:text-slate-300">No recurring transactions</h3>
          <p className="text-sm text-gray-400 dark:text-slate-500 mt-1">Set up recurring income or expenses to auto-create transactions.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 stagger-grid">
          {items.map((item) => (
            <div key={item.id} className={`card-hover p-5 ${!item.active ? 'opacity-50' : ''}`}>
              <div className="flex items-start justify-between mb-3">
                <div>
                  <h3 className="font-semibold text-gray-900 dark:text-white">{item.description || item.categoryName}</h3>
                  <p className="text-xs text-gray-400 dark:text-slate-500 mt-0.5">{item.categoryName} · {item.frequency}</p>
                </div>
                <span className={`text-sm font-bold ${item.type === 'INCOME' ? 'text-emerald-600' : 'text-red-600'}`}>
                  {item.type === 'INCOME' ? '+' : '-'}{formatCurrency(item.amount)}
                </span>
              </div>
              <div className="flex items-center justify-between text-xs text-gray-500 dark:text-slate-400">
                <span>Next: {item.nextOccurrence}</span>
                {item.active && (
                  <button onClick={() => handleDeactivate(item.id)} className="flex items-center gap-1 text-red-500 hover:text-red-700 transition-colors">
                    <XCircle className="h-3.5 w-3.5" /> Stop
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="New Recurring Transaction">
        <form onSubmit={handleCreate} className="space-y-4">
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
            <input type="number" step="0.01" min="0.01" required className="input-field" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })} />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Category</label>
            <select required className="input-field" value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
              <option value="">Select a category</option>
              {filteredCategories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Frequency</label>
            <select required className="input-field" value={form.frequency} onChange={(e) => setForm({ ...form, frequency: e.target.value as RecurringFrequency })}>
              <option value="DAILY">Daily</option>
              <option value="WEEKLY">Weekly</option>
              <option value="MONTHLY">Monthly</option>
              <option value="YEARLY">Yearly</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Start Date</label>
            <input type="date" required className="input-field" value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })} />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">End Date <span className="text-gray-400">(optional)</span></label>
            <input type="date" className="input-field" value={form.endDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })} />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Description <span className="text-gray-400">(optional)</span></label>
            <input type="text" className="input-field" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={() => setModalOpen(false)} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Saving…' : 'Create'}</button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
