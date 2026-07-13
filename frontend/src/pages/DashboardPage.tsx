import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { TrendingUp, TrendingDown, Wallet, PiggyBank, AlertTriangle, RefreshCw, Download, ArrowUpRight, ArrowDownRight } from 'lucide-react';
import { analyticsApi } from '../api/analytics';
import { transactionApi } from '../api/transactions';
import { exportApi } from '../api/export';
import type { MonthlySummaryResponse, TopCategoryResponse, SpendingTrendResponse, TransactionResponse, MonthOverMonthResponse, SpendingForecastResponse } from '../types';
import { formatCurrency, getCurrentMonthYear, getMonthName } from '../utils/formatters';
import { useToast } from '../context/ToastContext';
import { showErrorWithCorrelation } from '../utils/errorHandler';
import SpendingForecastChart from '../components/analytics/SpendingForecastChart';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend,
} from 'recharts';

const COLORS = ['#facc15', '#f472b6', '#4ade80', '#3b82f6', '#f87171', '#a855f7', '#06b6d4'];

export default function DashboardPage() {
  const { month, year } = getCurrentMonthYear();
  const { toast } = useToast();
  const [summary, setSummary] = useState<MonthlySummaryResponse | null>(null);
  const [topCats, setTopCats] = useState<TopCategoryResponse[]>([]);
  const [trends, setTrends] = useState<SpendingTrendResponse[]>([]);
  const [recentTx, setRecentTx] = useState<TransactionResponse[]>([]);
  const [mom, setMom] = useState<MonthOverMonthResponse | null>(null);
  const [forecast, setForecast] = useState<SpendingForecastResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [exporting, setExporting] = useState(false);

  async function loadData(showRefresh = false) {
    if (showRefresh) setRefreshing(true); else setLoading(true);
    try {
      const [summaryRes, topCatsRes, trendsRes, recentTxRes, momRes, forecastRes] = await Promise.all([
        analyticsApi.monthlySummary(month, year),
        analyticsApi.topCategories(month, year),
        analyticsApi.spendingTrends(6),
        transactionApi.list({ page: 0, size: 5 }),
        analyticsApi.monthOverMonth(month, year),
        analyticsApi.spendingForecast().catch(() => null),
      ]);
      setSummary(summaryRes);
      setTopCats(topCatsRes);
      setTrends(trendsRes);
      setRecentTx(recentTxRes.content);
      setMom(momRes);
      setForecast(forecastRes);
    } catch (error) {
      showErrorWithCorrelation(toast, 'Failed to load dashboard data', error);
    } finally {
      setLoading(false);

      setRefreshing(false);
    }
  }

  useEffect(() => { loadData(); }, [month, year]);

  async function handleExport() {
    setExporting(true);
    try {
      const blob = await exportApi.transactionsCsv();
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `transactions_${year}_${month}.csv`;
      a.click();
      URL.revokeObjectURL(url);
      toast('success', 'Transactions exported');
    } catch (error) {
      showErrorWithCorrelation(toast, 'Export failed', error);
    } finally { setExporting(false); }
  }

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
    <div className="space-y-6 page-enter">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Dashboard</h1>
          <p className="text-gray-500 dark:text-slate-400 mt-1">
            {getMonthName(month)} {year} overview
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button onClick={handleExport} disabled={exporting} className="btn-secondary flex items-center gap-2 text-sm" title="Export CSV">
            <Download className={`h-4 w-4 ${exporting ? 'animate-pulse' : ''}`} /> Export
          </button>
          <button onClick={() => loadData(true)} disabled={refreshing} className="p-2 rounded-lg text-gray-500 hover:bg-gray-100 dark:hover:bg-slate-700 transition-colors disabled:opacity-50" title="Refresh">
            <RefreshCw className={`h-5 w-5 ${refreshing ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 stagger-grid">
        <SummaryCard
          title="Total Income"
          value={formatCurrency(summary?.totalIncome ?? 0)}
          icon={<TrendingUp className="h-5 w-5" />}
          color="emerald"
          badge={mom ? { value: mom.incomeChangePercent, positive: mom.incomeChangePercent >= 0 } : undefined}
        />
        <SummaryCard
          title="Total Expenses"
          value={formatCurrency(summary?.totalExpense ?? 0)}
          icon={<TrendingDown className="h-5 w-5" />}
          color="red"
          badge={mom ? { value: mom.expenseChangePercent, positive: mom.expenseChangePercent <= 0 } : undefined}
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
          color="ocean"
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
                <Area type="monotone" dataKey="Income" stroke="#000000" fill="#4ade80" strokeWidth={3} />
                <Area type="monotone" dataKey="Expenses" stroke="#000000" fill="#f87171" strokeWidth={3} />
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
                <Pie data={pieData} cx="50%" cy="50%" innerRadius={60} outerRadius={100} paddingAngle={4} dataKey="value" label={{ fill: '#ffffff', fontSize: 12, fontWeight: 500 }}>
                  {pieData.map((_, i) => (
                    <Cell key={i} fill={COLORS[i % COLORS.length]} />
                  ))}
                </Pie>
                <Legend
                  formatter={(value) => <span className="text-xs font-medium text-white">{value}</span>}
                />
                <Tooltip
                  contentStyle={{ backgroundColor: '#1e293b', border: 'none', borderRadius: '8px', color: '#ffffff' }}
                  itemStyle={{ color: '#ffffff' }}
                  labelStyle={{ color: '#ffffff' }}
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
      <div className="glass-panel p-6">
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-lg font-display font-semibold text-gray-900 dark:text-white">Recent Transactions</h3>
          <button className="text-sm font-medium text-brand-600 dark:text-brand-400 hover:text-brand-700 dark:hover:text-brand-300 transition-colors">
            View All
          </button>
        </div>
        {recentTx.length > 0 ? (
          <div className="space-y-3">
            {recentTx.map((tx) => (
              <motion.div 
                key={tx.id} 
                initial={{ opacity: 0, y: 5 }}
                animate={{ opacity: 1, y: 0 }}
                whileHover={{ x: -2, y: -2, boxShadow: '4px 4px 0px 0px rgba(0,0,0,1)' }}
                className="flex items-center justify-between p-4 rounded-none border-2 border-black dark:border-white bg-white dark:bg-black transition-all cursor-pointer group"
              >
                <div className="flex items-center gap-4">
                  <div className={`h-12 w-12 rounded-none border-2 border-black dark:border-white shadow-brutal-sm dark:shadow-brutal-dark flex items-center justify-center ${tx.type === 'INCOME' ? 'bg-accent-green text-black' : 'bg-accent-red text-black'}`}>
                    {tx.type === 'INCOME' ? <TrendingUp className="h-5 w-5" /> : <TrendingDown className="h-5 w-5" />}
                  </div>
                  <div>
                    <p className="font-bold text-black dark:text-white group-hover:text-accent-blue transition-colors">{tx.description || 'Untitled Transaction'}</p>
                    <p className="text-xs font-bold text-gray-500 dark:text-gray-400 mt-0.5">{tx.date} • {tx.categoryName}</p>
                  </div>
                </div>
                <div className={`text-right font-display font-black text-xl ${tx.type === 'INCOME' ? 'text-accent-green' : 'text-black dark:text-white'}`}>
                  {tx.type === 'INCOME' ? '+' : '-'}{formatCurrency(tx.amount)}
                </div>
              </motion.div>
            ))}
          </div>
        ) : (
          <EmptyState message="No transactions yet. Start by adding your first transaction." />
        )}
      </div>

      {/* Spending Forecast */}
      {forecast ? (
        <div className="card p-6">
          <h3 className="text-base font-semibold text-gray-900 dark:text-white mb-4">Spending Forecast</h3>
          <SpendingForecastChart data={forecast} />
        </div>
      ) : null}
    </div>
  );
}

/* ── Helper components ── */

function SummaryCard({ title, value, icon, color, highlight, badge }: {
  title: string; value: string; icon: React.ReactNode; color: string; highlight?: boolean;
  badge?: { value: number; positive: boolean };
}) {
  const colorMap: Record<string, string> = {
    emerald: 'bg-accent-green',
    red: 'bg-accent-red',
    brand: 'bg-accent-pink',
    ocean: 'bg-accent-blue',
  };

  return (
    <div className={`card p-6 relative overflow-hidden group hover:-translate-y-1 hover:-translate-x-1 hover:shadow-brutal-lg transition-all duration-200 ${highlight ? 'border-accent-red' : ''}`}>
      <div className="flex items-center justify-between mb-4 relative z-10">
        <span className="text-sm font-bold text-black dark:text-white uppercase tracking-widest">{title}</span>
        <div className={`flex h-12 w-12 items-center justify-center rounded-none ${colorMap[color]} text-black border-2 border-black dark:border-white shadow-brutal-sm dark:shadow-brutal-dark`}>
          {icon}
        </div>
      </div>
      <p className="text-4xl font-display font-black text-black dark:text-white relative z-10 tracking-tight">{value}</p>
      {badge && (
        <div className={`inline-flex items-center gap-1.5 mt-3 px-2 py-1 text-xs font-bold border-2 border-black dark:border-white shadow-brutal-sm dark:shadow-brutal-dark bg-white dark:bg-black text-black dark:text-white`}>
          {badge.positive ? <ArrowUpRight className="h-4 w-4 text-accent-green" /> : <ArrowDownRight className="h-4 w-4 text-accent-red" />}
          {Math.abs(badge.value).toFixed(1)}% vs last
        </div>
      )}
    </div>
  );
}

function EmptyState({ message }: { message: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-4 text-center card bg-accent-yellow">
      <div className="h-16 w-16 mb-4 rounded-none bg-white border-2 border-black dark:border-white flex items-center justify-center shadow-brutal-sm dark:shadow-brutal-dark">
        <AlertTriangle className="h-8 w-8 text-black" />
      </div>
      <p className="text-sm font-bold text-black max-w-sm">{message}</p>
    </div>
  );
}
