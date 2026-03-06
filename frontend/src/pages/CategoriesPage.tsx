import { useEffect, useState } from 'react';
import { Plus, Tag, ArrowDownLeft, ArrowUpRight } from 'lucide-react';
import { categoryApi } from '../api/categories';
import type { CategoryResponse } from '../types';
import { useToast } from '../context/ToastContext';
import Modal from '../components/ui/Modal';

export default function CategoriesPage() {
  const { toast } = useToast();
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [filter, setFilter] = useState<'ALL' | 'INCOME' | 'EXPENSE'>('ALL');
  const [form, setForm] = useState({ name: '', type: 'EXPENSE' as 'INCOME' | 'EXPENSE' });

  async function load() {
    setLoading(true);
    try {
      const data = await categoryApi.list();
      setCategories(data);
    } catch {
      toast('error', 'Failed to load categories');
    } finally { setLoading(false); }
  }

  useEffect(() => { load(); }, []);

  const filtered = filter === 'ALL' ? categories : categories.filter((c) => c.type === filter);
  const incomeCount = categories.filter((c) => c.type === 'INCOME').length;
  const expenseCount = categories.filter((c) => c.type === 'EXPENSE').length;

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    try {
      await categoryApi.create({ name: form.name, type: form.type });
      setModalOpen(false);
      setForm({ name: '', type: 'EXPENSE' });
      toast('success', 'Category created');
      load();
    } catch {
      toast('error', 'Failed to create category');
    } finally { setSaving(false); }
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
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Categories</h1>
          <p className="text-gray-500 dark:text-slate-400 mt-1">
            {categories.length} categories · {incomeCount} income · {expenseCount} expense
          </p>
        </div>
        <button onClick={() => { setForm({ name: '', type: 'EXPENSE' }); setModalOpen(true); }} className="btn-primary flex items-center gap-2 self-start">
          <Plus className="h-4 w-4" /> Add Category
        </button>
      </div>

      {/* Filter tabs */}
      <div className="flex gap-2">
        {(['ALL', 'INCOME', 'EXPENSE'] as const).map((t) => (
          <button
            key={t}
            onClick={() => setFilter(t)}
            className={`px-4 py-2 text-sm font-medium rounded-lg transition-colors ${
              filter === t
                ? 'bg-brand-600 text-white shadow-sm'
                : 'bg-white dark:bg-slate-800 text-gray-600 dark:text-slate-400 border border-gray-200 dark:border-slate-700 hover:bg-gray-50 dark:hover:bg-slate-700'
            }`}
          >
            {t === 'ALL' ? 'All' : t === 'INCOME' ? 'Income' : 'Expense'}
          </button>
        ))}
      </div>

      {/* Category grid */}
      {filtered.length === 0 ? (
        <div className="card p-12 text-center">
          <Tag className="h-12 w-12 mx-auto text-gray-300 dark:text-slate-600 mb-4" />
          <h3 className="text-lg font-semibold text-gray-700 dark:text-slate-300">No categories found</h3>
          <p className="text-sm text-gray-400 dark:text-slate-500 mt-1">
            {filter !== 'ALL' ? `No ${filter.toLowerCase()} categories yet.` : 'Create your first category to get started.'}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 stagger-grid">
          {filtered.map((cat) => (
            <div key={cat.id} className="card-hover p-5 flex items-center gap-4">
              <div className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-xl ${
                cat.type === 'INCOME'
                  ? 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400'
                  : 'bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400'
              }`}>
                {cat.type === 'INCOME' ? <ArrowDownLeft className="h-5 w-5" /> : <ArrowUpRight className="h-5 w-5" />}
              </div>
              <div className="min-w-0">
                <h3 className="font-semibold text-gray-900 dark:text-white truncate">{cat.name}</h3>
                <span className={`text-xs font-medium uppercase ${
                  cat.type === 'INCOME' ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'
                }`}>
                  {cat.type}
                </span>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create modal */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="New Category">
        <form onSubmit={handleCreate} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Name</label>
            <input
              type="text"
              required
              minLength={2}
              maxLength={100}
              className="input-field"
              placeholder="e.g. Subscriptions"
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </div>
          {/* Type toggle */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Type</label>
            <div className="flex rounded-lg border border-gray-200 dark:border-slate-700 overflow-hidden">
              {(['EXPENSE', 'INCOME'] as const).map((t) => (
                <button
                  key={t}
                  type="button"
                  onClick={() => setForm({ ...form, type: t })}
                  className={`flex-1 py-2.5 text-sm font-medium transition-colors ${
                    form.type === t
                      ? t === 'INCOME'
                        ? 'bg-emerald-600 text-white'
                        : 'bg-red-600 text-white'
                      : 'bg-transparent text-gray-500 dark:text-slate-400 hover:bg-gray-50 dark:hover:bg-slate-800'
                  }`}
                >
                  {t}
                </button>
              ))}
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={() => setModalOpen(false)} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary">
              {saving ? 'Creating…' : 'Create'}
            </button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
