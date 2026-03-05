import { useEffect, useState } from 'react';
import { BarChart3, AlertTriangle, TrendingUp } from 'lucide-react';
import { analyticsApi } from '../api/analytics';
import type { SpendingTrendResponse, TopCategoryResponse, AnomalyResponse } from '../types';
import { formatCurrency, getMonthName, getCurrentMonthYear } from '../utils/formatters';
import {
  LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, PieChart, Pie, Cell, Legend,
} from 'recharts';

const PIE_COLORS = ['#6366f1', '#f43f5e', '#10b981', '#f59e0b', '#8b5cf6', '#06b6d4', '#ec4899', '#14b8a6'];

export default function AnalyticsPage() {
  const { month, year } = getCurrentMonthYear();
  const [months, setMonths] = useState(6);
  const [trends, setTrends] = useState<SpendingTrendResponse[]>([]);
  const [topCats, setTopCats] = useState<TopCategoryResponse[]>([]);
  const [anomalies, setAnomalies] = useState<AnomalyResponse[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      setLoading(true);
      try {
        const [tr, tc, an] = await Promise.all([
          analyticsApi.spendingTrends(months),
          analyticsApi.topCategories(month, year),
          analyticsApi.anomalies(),
        ]);
        setTrends(tr);
        setTopCats(tc);
        setAnomalies(an);
      } catch { /* ignore */ } finally { setLoading(false); }
    }
    load();
  }, [months, month, year]);

  const trendData = trends.map((t) => ({
    name: `${getMonthName(t.month).slice(0, 3)} ${t.year}`,
    Income: t.totalIncome,
    Spending: t.totalSpending,
    Savings: t.totalIncome - t.totalSpending,
  }));

  const totalSpending = topCats.reduce((sum, c) => sum + c.totalAmount, 0);
  const pieData = topCats.map((c) => ({ name: c.categoryName, value: c.totalAmount, percentage: totalSpending > 0 ? (c.totalAmount / totalSpending) * 100 : 0 }));

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="h-8 w-8 border-4 border-brand-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">Analytics</h1>
          <p className="text-gray-500 dark:text-slate-400 mt-1">Deep insights into your finances</p>
        </div>
        <div className="flex items-center gap-2 self-start">
          <span className="text-sm text-gray-500 dark:text-slate-400">Period:</span>
          <select className="input-field w-auto !py-1.5 !text-sm" value={months} onChange={(e) => setMonths(Number(e.target.value))}>
            <option value={3}>3 months</option>
            <option value={6}>6 months</option>
            <option value={12}>12 months</option>
          </select>
        </div>
      </div>

      {/* Anomaly alerts */}
      {anomalies.length > 0 && (
        <div className="space-y-3">
          {anomalies.map((a, i) => (
            <div key={i} className="flex items-start gap-3 p-4 rounded-xl border border-amber-200 dark:border-amber-800/50 bg-amber-50 dark:bg-amber-900/10">
              <AlertTriangle className="h-5 w-5 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
              <div>
                <p className="text-sm font-medium text-amber-800 dark:text-amber-300">{a.categoryName}: Unusual spending detected</p>
                <p className="text-xs text-amber-600 dark:text-amber-400 mt-1">
                  Amount: {formatCurrency(a.amount)} · Z-Score: {a.zScore.toFixed(2)} · Severity: {a.severity}
                </p>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Income vs Expense trend */}
      <div className="card p-6">
        <div className="flex items-center gap-2 mb-4">
          <TrendingUp className="h-5 w-5 text-brand-600" />
          <h3 className="text-base font-semibold text-gray-900 dark:text-white">Income vs Expenses</h3>
        </div>
        {trendData.length > 0 ? (
          <ResponsiveContainer width="100%" height={350}>
            <LineChart data={trendData}>
              <CartesianGrid strokeDasharray="3 3" className="stroke-gray-200 dark:stroke-slate-700" />
              <XAxis dataKey="name" tick={{ fill: '#94a3b8', fontSize: 12 }} />
              <YAxis tick={{ fill: '#94a3b8', fontSize: 12 }} />
              <Tooltip contentStyle={{ backgroundColor: '#1e293b', border: 'none', borderRadius: '8px', color: '#f8fafc' }} formatter={(v: number) => formatCurrency(v)} />
              <Legend />
              <Line type="monotone" dataKey="Income" stroke="#10b981" strokeWidth={2.5} dot={{ r: 4 }} />
              <Line type="monotone" dataKey="Spending" stroke="#f43f5e" strokeWidth={2.5} dot={{ r: 4 }} />
              <Line type="monotone" dataKey="Savings" stroke="#6366f1" strokeWidth={2} strokeDasharray="5 5" dot={{ r: 3 }} />
            </LineChart>
          </ResponsiveContainer>
        ) : (
          <EmptyChart message="No trend data available." />
        )}
      </div>

      {/* Row: Bar chart + Pie */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Monthly breakdown bar */}
        <div className="card p-6">
          <div className="flex items-center gap-2 mb-4">
            <BarChart3 className="h-5 w-5 text-brand-600" />
            <h3 className="text-base font-semibold text-gray-900 dark:text-white">Monthly Breakdown</h3>
          </div>
          {trendData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={trendData}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-gray-200 dark:stroke-slate-700" />
                <XAxis dataKey="name" tick={{ fill: '#94a3b8', fontSize: 12 }} />
                <YAxis tick={{ fill: '#94a3b8', fontSize: 12 }} />
                <Tooltip contentStyle={{ backgroundColor: '#1e293b', border: 'none', borderRadius: '8px', color: '#f8fafc' }} formatter={(v: number) => formatCurrency(v)} />
                <Bar dataKey="Income" fill="#10b981" radius={[4, 4, 0, 0]} />
                <Bar dataKey="Spending" fill="#f43f5e" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <EmptyChart message="No monthly data yet." />
          )}
        </div>

        {/* Category pie */}
        <div className="card p-6">
          <h3 className="text-base font-semibold text-gray-900 dark:text-white mb-4">Top Spending Categories</h3>
          {pieData.length > 0 ? (
            <>
              <ResponsiveContainer width="100%" height={240}>
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" innerRadius={55} outerRadius={95} paddingAngle={3} dataKey="value">
                    {pieData.map((_, i) => (
                      <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={{ backgroundColor: '#1e293b', border: 'none', borderRadius: '8px', color: '#f8fafc' }} formatter={(v: number) => formatCurrency(v)} />
                </PieChart>
              </ResponsiveContainer>
              {/* Legend list */}
              <div className="mt-4 space-y-2">
                {topCats.map((c, i) => (
                  <div key={i} className="flex items-center justify-between text-sm">
                    <div className="flex items-center gap-2">
                      <span className="w-3 h-3 rounded-full" style={{ backgroundColor: PIE_COLORS[i % PIE_COLORS.length] }} />
                      <span className="text-gray-700 dark:text-slate-300">{c.categoryName}</span>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="text-gray-500 dark:text-slate-400">{(totalSpending > 0 ? (c.totalAmount / totalSpending) * 100 : 0).toFixed(1)}%</span>
                      <span className="font-medium text-gray-900 dark:text-white">{formatCurrency(c.totalAmount)}</span>
                    </div>
                  </div>
                ))}
              </div>
            </>
          ) : (
            <EmptyChart message="No category data for this month." />
          )}
        </div>
      </div>
    </div>
  );
}

function EmptyChart({ message }: { message: string }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <BarChart3 className="h-10 w-10 text-gray-300 dark:text-slate-600 mb-3" />
      <p className="text-sm text-gray-400 dark:text-slate-500">{message}</p>
    </div>
  );
}
