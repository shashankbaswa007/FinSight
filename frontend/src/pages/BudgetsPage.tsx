import { useEffect, useState } from 'react';
import { Plus, Target, AlertCircle, CheckCircle2, Pencil, Trash2 } from 'lucide-react';
import { budgetApi } from '../api/budgets';
import { categoryApi } from '../api/categories';
import type { BudgetResponse, BudgetStatusResponse, CategoryResponse } from '../types';
import { formatCurrency, getCurrentMonthYear, getMonthName } from '../utils/formatters';
import { useToast } from '../context/ToastContext';
import Modal from '../components/ui/Modal';

export default function BudgetsPage() {
  const { toast } = useToast();
  const { month, year } = getCurrentMonthYear();
  const [budgets, setBudgets] = useState<BudgetResponse[]>([]);
  const [statuses, setStatuses] = useState<BudgetStatusResponse[]>([]);
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ categoryId: '', monthlyLimit: '', month: String(month), year: String(year) });

  /* Edit / Delete state */
  const [editBudget, setEditBudget] = useState<BudgetResponse | null>(null);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editLimit, setEditLimit] = useState('');
  const [deleteConfirm, setDeleteConfirm] = useState<BudgetResponse | null>(null);

  async function load() {
    setLoading(true);
    try {
      const [b, s, c] = await Promise.all([
        budgetApi.list(),
        budgetApi.status(month, year),
        categoryApi.list(),
      ]);
      setBudgets(b);
      setStatuses(s);
      setCategories(c.filter((cat) => cat.type === 'EXPENSE'));
    } catch {
      toast('error', 'Failed to load budgets');
    } finally { setLoading(false); }
  }

  useEffect(() => { load(); }, []);

  /* Merge budget + status for display */
  const merged = budgets.map((b) => {
    const st = statuses.find((s) => s.budgetId === b.id);
    return { ...b, spent: st?.amountSpent ?? 0, remaining: st?.remaining ?? b.monthlyLimit, exceeded: st?.exceeded ?? false };
  });

  /* Categories not yet budgeted */
  const availableCats = categories.filter((c) => !budgets.some((b) => b.categoryName === c.name));

  async function handleCreate(e: React.FormEvent) {
    e.preventDefault();
    setSaving(true);
    try {
      await budgetApi.create({
        categoryId: parseInt(form.categoryId),
        monthlyLimit: parseFloat(form.monthlyLimit),
        month: parseInt(form.month),
        year: parseInt(form.year),
      });
      setModalOpen(false);
      toast('success', 'Budget created');
      load();
    } catch {
      toast('error', 'Failed to create budget');
    } finally { setSaving(false); }
  }

  function openEdit(b: BudgetResponse) {
    setEditBudget(b);
    setEditLimit(String(b.monthlyLimit));
    setEditModalOpen(true);
  }

  async function handleEdit(e: React.FormEvent) {
    e.preventDefault();
    if (!editBudget) return;
    setSaving(true);
    try {
      await budgetApi.update(editBudget.id, {
        categoryId: editBudget.categoryId,
        monthlyLimit: parseFloat(editLimit),
        month: editBudget.month,
        year: editBudget.year,
      });
      setEditModalOpen(false);
      toast('success', 'Budget updated');
      load();
    } catch {
      toast('error', 'Failed to update budget');
    } finally { setSaving(false); }
  }

  async function handleDelete() {
    if (!deleteConfirm) return;
    try {
      await budgetApi.delete(deleteConfirm.id);
      setDeleteConfirm(null);
      toast('success', 'Budget deleted');
      load();
    } catch {
      toast('error', 'Failed to delete budget');
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
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Budgets</h1>
          <p className="text-gray-500 dark:text-slate-400 mt-1">{getMonthName(month)} {year} budget tracking</p>
        </div>
        <button onClick={() => { setForm({ categoryId: '', monthlyLimit: '', month: String(month), year: String(year) }); setModalOpen(true); }} className="btn-primary flex items-center gap-2 self-start" disabled={availableCats.length === 0}>
          <Plus className="h-4 w-4" /> Set Budget
        </button>
      </div>

      {/* Budget cards */}
      {merged.length === 0 ? (
        <div className="card p-12 text-center">
          <Target className="h-12 w-12 mx-auto text-gray-300 dark:text-slate-600 mb-4" />
          <h3 className="text-lg font-semibold text-gray-700 dark:text-slate-300">No budgets set</h3>
          <p className="text-sm text-gray-400 dark:text-slate-500 mt-1">Create your first budget to start tracking spending against limits.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 stagger-grid">
          {merged.map((b) => {
            const pct = b.monthlyLimit > 0 ? Math.min((b.spent / b.monthlyLimit) * 100, 100) : 0;
            let barColor = 'bg-brand-500';
            if (pct > 90) barColor = 'bg-red-500';
            else if (pct > 70) barColor = 'bg-amber-500';

            return (
              <div key={b.id} className={`card-hover p-5 ${b.exceeded ? 'ring-2 ring-red-400/60' : ''}`}>
                <div className="flex items-start justify-between mb-4">
                  <div>
                    <h3 className="font-semibold text-gray-900 dark:text-white">{b.categoryName}</h3>
                    <p className="text-xs text-gray-400 dark:text-slate-500 mt-0.5">
                      {getMonthName(b.month)} {b.year}
                    </p>
                  </div>
                  {b.exceeded ? (
                    <span className="flex items-center gap-1 text-xs font-medium text-red-600 dark:text-red-400 bg-red-50 dark:bg-red-900/20 px-2 py-1 rounded-full">
                      <AlertCircle className="h-3.5 w-3.5" /> Over
                    </span>
                  ) : (
                    <span className="flex items-center gap-1 text-xs font-medium text-emerald-600 dark:text-emerald-400 bg-emerald-50 dark:bg-emerald-900/20 px-2 py-1 rounded-full">
                      <CheckCircle2 className="h-3.5 w-3.5" /> On track
                    </span>
                  )}
                </div>

                {/* Progress bar */}
                <div className="space-y-2">
                  <div className="h-2.5 bg-gray-100 dark:bg-slate-700 rounded-full overflow-hidden">
                    <div className={`h-full ${barColor} rounded-full transition-all duration-500`} style={{ width: `${pct}%` }} />
                  </div>
                  <div className="flex justify-between text-xs">
                    <span className="text-gray-500 dark:text-slate-400">Spent: {formatCurrency(b.spent)}</span>
                    <span className="text-gray-500 dark:text-slate-400">Limit: {formatCurrency(b.monthlyLimit)}</span>
                  </div>
                  <div className="text-right">
                    <span className={`text-sm font-semibold ${b.exceeded ? 'text-red-600 dark:text-red-400' : 'text-emerald-600 dark:text-emerald-400'}`}>
                      {b.exceeded ? `Over by ${formatCurrency(Math.abs(b.remaining))}` : `${formatCurrency(b.remaining)} left`}
                    </span>
                  </div>
                  <div className="flex justify-end gap-2 mt-3 pt-3 border-t border-gray-100 dark:border-slate-700">
                    <button onClick={() => openEdit(b)} className="p-1.5 rounded-md text-gray-400 hover:text-brand-600 hover:bg-brand-50 dark:hover:bg-brand-900/20 transition-colors" title="Edit">
                      <Pencil className="h-4 w-4" />
                    </button>
                    <button onClick={() => setDeleteConfirm(b)} className="p-1.5 rounded-md text-gray-400 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors" title="Delete">
                      <Trash2 className="h-4 w-4" />
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Create modal */}
      <Modal open={modalOpen} onClose={() => setModalOpen(false)} title="Set Budget">
        <form onSubmit={handleCreate} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Category</label>
            <select required className="input-field" value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
              <option value="">Select a category</option>
              {availableCats.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Monthly Limit</label>
            <input type="number" step="0.01" min="1" required className="input-field" placeholder="10000" value={form.monthlyLimit} onChange={(e) => setForm({ ...form, monthlyLimit: e.target.value })} />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Month</label>
              <select className="input-field" value={form.month} onChange={(e) => setForm({ ...form, month: e.target.value })}>
                {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => <option key={m} value={m}>{getMonthName(m)}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Year</label>
              <input type="number" min="2020" max="2100" className="input-field" value={form.year} onChange={(e) => setForm({ ...form, year: e.target.value })} />
            </div>
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={() => setModalOpen(false)} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Saving…' : 'Create Budget'}</button>
          </div>
        </form>
      </Modal>

      {/* Edit modal */}
      <Modal open={editModalOpen} onClose={() => setEditModalOpen(false)} title={`Edit Budget – ${editBudget?.categoryName ?? ''}`}>
        <form onSubmit={handleEdit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-slate-300 mb-1">Monthly Limit</label>
            <input type="number" step="0.01" min="1" required className="input-field" value={editLimit} onChange={(e) => setEditLimit(e.target.value)} />
          </div>
          <div className="flex justify-end gap-3 pt-2">
            <button type="button" onClick={() => setEditModalOpen(false)} className="btn-secondary">Cancel</button>
            <button type="submit" disabled={saving} className="btn-primary">{saving ? 'Saving…' : 'Update'}</button>
          </div>
        </form>
      </Modal>

      {/* Delete confirmation */}
      <Modal open={deleteConfirm !== null} onClose={() => setDeleteConfirm(null)} title="Delete Budget">
        <p className="text-sm text-gray-600 dark:text-slate-400">
          Are you sure you want to delete the <strong>{deleteConfirm?.categoryName}</strong> budget? This action cannot be undone.
        </p>
        <div className="flex justify-end gap-3 pt-4">
          <button onClick={() => setDeleteConfirm(null)} className="btn-secondary">Cancel</button>
          <button onClick={handleDelete} className="px-4 py-2 rounded-lg bg-red-600 text-white hover:bg-red-700 transition-colors text-sm font-medium">Delete</button>
        </div>
      </Modal>
    </div>
  );
}
