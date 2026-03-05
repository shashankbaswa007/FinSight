import { useEffect, useState } from 'react';
import { TrendingUp, TrendingDown, Wallet, PiggyBank, AlertTriangle } from 'lucide-react';
import { analyticsApi } from '../api/analytics';
import { transactionApi } from '../api/transactions';
import type { MonthlySummaryResponse, TopCategoryResponse, SpendingTrendResponse, TransactionResponse } from '../types';
import { formatCurrency, getCurrentMonthYear, getMonthName } from '../utils/formatters';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts';

const COLORS = ['#6366f1', '#f43f5e', '#10b981', '#f59e0b', '#8b5cf6', '#06b6d4', '#ec4899', '#14b8a6'];

export default function DashboardPage() {
  const { month, year } = getCurrentMonthYear();
  const [summary, setSummary] = useState<MonthlySummaryResponse | null>(null);
  const [topCats, setTopCats] = useState<TopCategoryResponse[]>([]);
  const [trends, setTrends] = useState<SpendingTrendResponse[]>([]);
  const [recentTx, setRecentTx] = useState<TransactionResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [s, tc, tr, tx] = await Promise.all([
          analyticsApi.monthlySummary(month, year),
          analyticsApi.topCategories(month, year),
          analyticsApi.spendingTrends(6),
          transactionApi.list({ page: 0, size: 5 }),
        ]);
        setSummary(s);
        setTopCats(tc);
        setTrends(tr);
        setRecentTx(tx.content);
      } catch {
        // API may 404 if no data yet – gracefully handle
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [month, year]);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="h-8 w-8 border-4 border-brand-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  const trendData = trends.map((t) => ({
    name: `${getMonthName(t.month).slice(0, 3)} ${t.year}`,
    Income: t.totalIncome,
    Expenses: t.totalSpending,
  }));

  const pieData = topCats.map((c) => ({ name: c.categoryName, value: c.totalAmount }));

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Dashboard</h1>
        <p className="text-gray-500 dark:text-slate-400 mt-1">
          {getMonthName(month)} {year} overview
        </p>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <SummaryCard
          title="Total Income"
          value={formatCurrency(summary?.totalIncome ?? 0)}
          icon={<TrendingUp className="h-5 w-5" />}
          color="emerald"
        />
        <SummaryCard
          title="Total Expenses"
          value={formatCurrency(summary?.totalExpense ?? 0)}
          icon={<TrendingDown className="h-5 w-5" />}
          color="red"
        />
        <SummaryCard
          title="Net Savings"
          value={formatCurrency(summary?.netSavings ?? 0)}
          icon={<PiggyBank className="h-5 w-5" />}
          color="brand"
          highlight={summary != null && summary.netSavings < 0}
        />
        <SummaryCard
          title="I/E Ratio"
          value={summary?.incomeExpenseRatio?.toFixed(2) ?? '0.00'}
          icon={<Wallet className="h-5 w-5" />}
          color="amber"
        />
      </div>

      {/* Charts row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Spending trends */}
        <div className="lg:col-span-2 card p-6">
          <h3 className="text-base font-semibold text-gray-900 dark:text-white mb-4">Spending Trends</h3>
          {trendData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <AreaChart data={trendData}>
                <defs>
                  <linearGradient id="incomeGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="expenseGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#f43f5e" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#f43f5e" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" className="stroke-gray-200 dark:stroke-slate-700" />
                <XAxis dataKey="name" className="text-xs" tick={{ fill: '#94a3b8' }} />
                <YAxis className="text-xs" tick={{ fill: '#94a3b8' }} />
                <Tooltip
                  contentStyle={{ backgroundColor: 'var(--tw-bg-opacity, #1e293b)', border: 'none', borderRadius: '8px', color: '#f8fafc' }}
                />
                <Area type="monotone" dataKey="Income" stroke="#10b981" fill="url(#incomeGrad)" strokeWidth={2} />
                <Area type="monotone" dataKey="Expenses" stroke="#f43f5e" fill="url(#expenseGrad)" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <EmptyState message="No trend data yet. Add some transactions to see your spending trends." />
          )}
        </div>

        {/* Top categories */}
        <div className="card p-6">
          <h3 className="text-base font-semibold text-gray-900 dark:text-white mb-4">Top Categories</h3>
          {pieData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie data={pieData} cx="50%" cy="50%" innerRadius={60} outerRadius={100} paddingAngle={4} dataKey="value">
                  {pieData.map((_, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
                  ))}
                </Pie>
                <Legend
                  formatter={(value) => <span className="text-xs text-gray-600 dark:text-slate-400">{value}</span>}
                />
                <Tooltip
                  contentStyle={{ backgroundColor: '#1e293b', border: 'none', borderRadius: '8px', color: '#f8fafc' }}
                  formatter={(value: number) => formatCurrency(value)}
                />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <EmptyState message="No expense data for this month." />
          )}
        </div>
      </div>

      {/* Recent transactions */}
      <div className="card p-6">
        <h3 className="text-base font-semibold text-gray-900 dark:text-white mb-4">Recent Transactions</h3>
        {recentTx.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-200 dark:border-slate-700">
                  <th className="text-left py-3 px-2 font-medium text-gray-500 dark:text-slate-400">Description</th>
                  <th className="text-left py-3 px-2 font-medium text-gray-500 dark:text-slate-400">Category</th>
                  <th className="text-left py-3 px-2 font-medium text-gray-500 dark:text-slate-400">Date</th>
                  <th className="text-right py-3 px-2 font-medium text-gray-500 dark:text-slate-400">Amount</th>
                </tr>
              </thead>
              <tbody>
                {recentTx.map((tx) => (
                  <tr key={tx.id} className="border-b border-gray-100 dark:border-slate-700/50 hover:bg-gray-50 dark:hover:bg-slate-800/50">
                    <td className="py-3 px-2 text-gray-900 dark:text-slate-200">{tx.description || '—'}</td>
                    <td className="py-3 px-2">
                      <span className={tx.type === 'INCOME' ? 'badge-income' : 'badge-expense'}>{tx.categoryName}</span>
                    </td>
                    <td className="py-3 px-2 text-gray-500 dark:text-slate-400">{tx.date}</td>
                    <td className={`py-3 px-2 text-right font-medium ${tx.type === 'INCOME' ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'}`}>
                      {tx.type === 'INCOME' ? '+' : '-'}{formatCurrency(tx.amount)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <EmptyState message="No transactions yet. Start by adding your first transaction." />
        )}
      </div>
    </div>
  );
}

/* ── Helper components ── */

function SummaryCard({ title, value, icon, color, highlight }: {
  title: string; value: string; icon: React.ReactNode; color: string; highlight?: boolean;
}) {
  const colorMap: Record<string, string> = {
    emerald: 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400',
    red: 'bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400',
    brand: 'bg-brand-100 dark:bg-brand-900/30 text-brand-600 dark:text-brand-400',
    amber: 'bg-amber-100 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400',
  };

  return (
    <div className={`card p-5 ${highlight ? 'ring-2 ring-red-400' : ''}`}>
      <div className="flex items-center justify-between mb-3">
        <span className="text-sm font-medium text-gray-500 dark:text-slate-400">{title}</span>
        <div className={`flex h-9 w-9 items-center justify-center rounded-lg ${colorMap[color]}`}>
          {icon}
        </div>
      </div>
      <p className="text-2xl font-bold text-gray-900 dark:text-white">{value}</p>
    </div>
  );
}

function EmptyState({ message }: { message: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-center">
      <AlertTriangle className="h-10 w-10 text-gray-300 dark:text-slate-600 mb-3" />
      <p className="text-sm text-gray-400 dark:text-slate-500">{message}</p>
    </div>
  );
}
