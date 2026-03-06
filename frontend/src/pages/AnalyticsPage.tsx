import { useEffect, useState } from 'react';
import {
  BarChart3, AlertTriangle, TrendingUp, TrendingDown, Wallet, PiggyBank,
  ArrowUpRight, ArrowDownRight, Calendar, Layers, Receipt,
} from 'lucide-react';
import { analyticsApi } from '../api/analytics';
import type {
  SpendingTrendResponse, TopCategoryResponse, AnomalyResponse,
  MonthlySummaryResponse, MonthOverMonthResponse,
  DailySpendingResponse, CategoryTrendResponse,
  ExpenseDistributionResponse, TopDescriptionResponse,
} from '../types';
import { formatCurrency, getMonthName, getCurrentMonthYear } from '../utils/formatters';
import { useToast } from '../context/ToastContext';
import {
  LineChart, Line, BarChart, Bar, AreaChart, Area, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend,
} from 'recharts';

const PIE_COLORS = ['#6366f1', '#f43f5e', '#10b981', '#f59e0b', '#8b5cf6', '#06b6d4', '#ec4899', '#14b8a6'];
const CATEGORY_COLORS = ['#6366f1', '#10b981', '#f43f5e', '#f59e0b', '#8b5cf6', '#06b6d4', '#ec4899', '#14b8a6', '#a855f7', '#84cc16'];

const tooltipStyle = { backgroundColor: '#1e293b', border: 'none', borderRadius: '8px', color: '#f8fafc' };

export default function AnalyticsPage() {
  const { toast } = useToast();
  const { month, year } = getCurrentMonthYear();
  const [months, setMonths] = useState(6);

  // Existing data
  const [trends, setTrends] = useState<SpendingTrendResponse[]>([]);
  const [topCats, setTopCats] = useState<TopCategoryResponse[]>([]);
  const [anomalies, setAnomalies] = useState<AnomalyResponse[]>([]);

  // New data
  const [summary, setSummary] = useState<MonthlySummaryResponse | null>(null);
  const [mom, setMom] = useState<MonthOverMonthResponse | null>(null);
  const [dailySpending, setDailySpending] = useState<DailySpendingResponse[]>([]);
  const [categoryTrends, setCategoryTrends] = useState<CategoryTrendResponse[]>([]);
  const [distribution, setDistribution] = useState<ExpenseDistributionResponse[]>([]);
  const [topDescs, setTopDescs] = useState<TopDescriptionResponse[]>([]);

  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      setLoading(true);
      try {
        const [tr, tc, an, sum, mo, ds, ct, ed, td] = await Promise.all([
          analyticsApi.spendingTrends(months),
          analyticsApi.topCategories(month, year),
          analyticsApi.anomalies(),
          analyticsApi.monthlySummary(month, year),
          analyticsApi.monthOverMonth(month, year),
          analyticsApi.dailySpending(month, year),
          analyticsApi.categoryTrends(months),
          analyticsApi.expenseDistribution(month, year),
          analyticsApi.topDescriptions(month, year),
        ]);
        setTrends(tr);
        setTopCats(tc);
        setAnomalies(an);
        setSummary(sum);
        setMom(mo);
        setDailySpending(ds);
        setCategoryTrends(ct);
        setDistribution(ed);
        setTopDescs(td);
      } catch {
        toast('error', 'Failed to load analytics data');
      } finally { setLoading(false); }
    }
    load();
  }, [months, month, year]);

  /* ── Derived data ── */
  const trendData = trends.map((t) => ({
    name: `${getMonthName(t.month).slice(0, 3)} ${t.year}`,
    Income: t.totalIncome,
    Spending: t.totalSpending,
    Savings: t.totalIncome - t.totalSpending,
    SavingsRate: t.totalIncome > 0 ? Math.round(((t.totalIncome - t.totalSpending) / t.totalIncome) * 100) : 0,
  }));

  const totalSpending = topCats.reduce((sum, c) => sum + c.totalAmount, 0);
  const pieData = topCats.map((c) => ({ name: c.categoryName, value: c.totalAmount }));

  const dailyData = dailySpending.map((d) => ({
    date: d.date.slice(8), // day part
    amount: d.amount,
  }));

  const avgDaily = dailySpending.length > 0
    ? dailySpending.reduce((s, d) => s + d.amount, 0) / dailySpending.length
    : 0;
  const daysInMonth = new Date(year, month, 0).getDate();
  const daysElapsed = month === new Date().getMonth() + 1 && year === new Date().getFullYear()
    ? new Date().getDate()
    : daysInMonth;
  const projectedSpend = avgDaily * daysInMonth;

  // Category trends → stacked area data
  const catNames = [...new Set(categoryTrends.map((c) => c.categoryName))];
  const catTrendMap = new Map<string, Record<string, number | string>>();
  for (const ct of categoryTrends) {
    const key = `${getMonthName(ct.month).slice(0, 3)} ${ct.year}`;
    if (!catTrendMap.has(key)) catTrendMap.set(key, { name: key });
    catTrendMap.get(key)![ct.categoryName] = ct.totalAmount;
  }
  const stackedCatData = [...catTrendMap.values()];

  // Distribution data for bar chart
  const distData = distribution.filter((d) => d.count > 0);

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

      {/* ① Anomaly alerts */}
      {anomalies.length > 0 && (
        <div className="space-y-3 animate-slide-down">
          {anomalies.map((a, i) => (
            <div key={i} className="flex items-start gap-3 p-4 rounded-xl border border-amber-200 dark:border-amber-800/50 bg-amber-50 dark:bg-amber-900/10 hover:shadow-sm transition-shadow">
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

      {/* ② Summary KPI cards + MoM comparison */}
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4 stagger-grid">
        <KpiCard
          label="Income"
          value={formatCurrency(summary?.totalIncome ?? 0)}
          icon={<TrendingUp className="h-4 w-4" />}
          color="emerald"
          change={mom?.incomeChangePercent}
          positive={mom ? mom.incomeChangePercent >= 0 : undefined}
        />
        <KpiCard
          label="Expenses"
          value={formatCurrency(summary?.totalExpense ?? 0)}
          icon={<TrendingDown className="h-4 w-4" />}
          color="red"
          change={mom?.expenseChangePercent}
          positive={mom ? mom.expenseChangePercent <= 0 : undefined}
        />
        <KpiCard
          label="Net Savings"
          value={formatCurrency(summary?.netSavings ?? 0)}
          icon={<PiggyBank className="h-4 w-4" />}
          color="brand"
        />
        <KpiCard
          label="I/E Ratio"
          value={summary?.incomeExpenseRatio?.toFixed(2) ?? '0.00'}
          icon={<Wallet className="h-4 w-4" />}
          color="amber"
        />
        <KpiCard
          label="Avg Daily"
          value={formatCurrency(avgDaily)}
          icon={<Calendar className="h-4 w-4" />}
          color="violet"
          subtitle={`${daysElapsed} days tracked`}
        />
        <KpiCard
          label="Projected"
          value={formatCurrency(projectedSpend)}
          icon={<Layers className="h-4 w-4" />}
          color="cyan"
          subtitle={`${daysInMonth} day month`}
        />
      </div>

      {/* ③ Income vs Expense trend + Savings rate */}
      <div className="card p-6">
        <div className="flex items-center gap-2 mb-4">
          <TrendingUp className="h-5 w-5 text-brand-600" />
          <h3 className="text-base font-semibold text-gray-900 dark:text-white">Income vs Expenses & Savings Rate</h3>
        </div>
        {trendData.length > 0 ? (
          <ResponsiveContainer width="100%" height={350}>
            <LineChart data={trendData}>
              <CartesianGrid strokeDasharray="3 3" className="stroke-gray-200 dark:stroke-slate-700" />
              <XAxis dataKey="name" tick={{ fill: '#94a3b8', fontSize: 12 }} />
              <YAxis yAxisId="left" tick={{ fill: '#94a3b8', fontSize: 12 }} />
              <YAxis yAxisId="right" orientation="right" tick={{ fill: '#94a3b8', fontSize: 12 }} unit="%" />
              <Tooltip contentStyle={tooltipStyle} formatter={(v: number, name: string) => name === 'SavingsRate' ? `${v}%` : formatCurrency(v)} />
              <Legend />
              <Line yAxisId="left" type="monotone" dataKey="Income" stroke="#10b981" strokeWidth={2.5} dot={{ r: 4 }} />
              <Line yAxisId="left" type="monotone" dataKey="Spending" stroke="#f43f5e" strokeWidth={2.5} dot={{ r: 4 }} />
              <Line yAxisId="left" type="monotone" dataKey="Savings" stroke="#6366f1" strokeWidth={2} strokeDasharray="5 5" dot={{ r: 3 }} />
              <Line yAxisId="right" type="monotone" dataKey="SavingsRate" stroke="#f59e0b" strokeWidth={2} dot={{ r: 3 }} name="Savings Rate %" />
            </LineChart>
          </ResponsiveContainer>
        ) : (
          <EmptyChart message="No trend data available." />
        )}
      </div>

      {/* ④ Daily spending + ⑤ Expense distribution */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="card p-6">
          <div className="flex items-center gap-2 mb-4">
            <Calendar className="h-5 w-5 text-brand-600" />
            <h3 className="text-base font-semibold text-gray-900 dark:text-white">Daily Spending — {getMonthName(month)}</h3>
          </div>
          {dailyData.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={dailyData}>
                <CartesianGrid strokeDasharray="3 3" className="stroke-gray-200 dark:stroke-slate-700" />
                <XAxis dataKey="date" tick={{ fill: '#94a3b8', fontSize: 11 }} />
                <YAxis tick={{ fill: '#94a3b8', fontSize: 12 }} />
                <Tooltip contentStyle={tooltipStyle} formatter={(v: number) => formatCurrency(v)} labelFormatter={(l) => `Day ${l}`} />
                <Bar dataKey="amount" fill="#6366f1" radius={[3, 3, 0, 0]} name="Spent" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <EmptyChart message="No daily spending data yet." />
          )}
        </div>

        <div className="card p-6">
          <div className="flex items-center gap-2 mb-4">
            <Layers className="h-5 w-5 text-brand-600" />
            <h3 className="text-base font-semibold text-gray-900 dark:text-white">Expense Distribution</h3>
          </div>
          {distData.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={distData} layout="vertical">
                <CartesianGrid strokeDasharray="3 3" className="stroke-gray-200 dark:stroke-slate-700" />
                <XAxis type="number" tick={{ fill: '#94a3b8', fontSize: 12 }} />
                <YAxis type="category" dataKey="range" tick={{ fill: '#94a3b8', fontSize: 11 }} width={80} />
                <Tooltip contentStyle={tooltipStyle} formatter={(v: number, name: string) => name === 'count' ? `${v} txns` : formatCurrency(v)} />
                <Bar dataKey="count" fill="#8b5cf6" radius={[0, 4, 4, 0]} name="Transactions" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <EmptyChart message="No expense data for this month." />
          )}
        </div>
      </div>

      {/* ⑥ Monthly breakdown bar + ⑦ Category pie */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
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
                <Tooltip contentStyle={tooltipStyle} formatter={(v: number) => formatCurrency(v)} />
                <Bar dataKey="Income" fill="#10b981" radius={[4, 4, 0, 0]} />
                <Bar dataKey="Spending" fill="#f43f5e" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <EmptyChart message="No monthly data yet." />
          )}
        </div>

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
                  <Tooltip contentStyle={tooltipStyle} formatter={(v: number) => formatCurrency(v)} />
                </PieChart>
              </ResponsiveContainer>
              <div className="mt-4 space-y-2">
                {topCats.map((c, i) => (
                  <div key={i} className="flex items-center justify-between text-sm">
                    <div className="flex items-center gap-2">
                      <span className="w-3 h-3 rounded-full" style={{ backgroundColor: PIE_COLORS[i % PIE_COLORS.length] }} />
                      <span className="text-gray-700 dark:text-slate-300">{c.categoryName}</span>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="text-gray-400 dark:text-slate-500 text-xs">{c.transactionCount} txns</span>
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

      {/* ⑧ Category trends stacked area */}
      <div className="card p-6">
        <div className="flex items-center gap-2 mb-4">
          <Layers className="h-5 w-5 text-brand-600" />
          <h3 className="text-base font-semibold text-gray-900 dark:text-white">Category Spending Over Time</h3>
        </div>
        {stackedCatData.length > 0 ? (
          <ResponsiveContainer width="100%" height={350}>
            <AreaChart data={stackedCatData}>
              <CartesianGrid strokeDasharray="3 3" className="stroke-gray-200 dark:stroke-slate-700" />
              <XAxis dataKey="name" tick={{ fill: '#94a3b8', fontSize: 12 }} />
              <YAxis tick={{ fill: '#94a3b8', fontSize: 12 }} />
              <Tooltip contentStyle={tooltipStyle} formatter={(v: number) => formatCurrency(v)} />
              <Legend />
              {catNames.map((cat, i) => (
                <Area
                  key={cat}
                  type="monotone"
                  dataKey={cat}
                  stackId="1"
                  stroke={CATEGORY_COLORS[i % CATEGORY_COLORS.length]}
                  fill={CATEGORY_COLORS[i % CATEGORY_COLORS.length]}
                  fillOpacity={0.6}
                />
              ))}
            </AreaChart>
          </ResponsiveContainer>
        ) : (
          <EmptyChart message="No category trend data available." />
        )}
      </div>

      {/* ⑨ Top merchants/descriptions table */}
      <div className="card p-6">
        <div className="flex items-center gap-2 mb-4">
          <Receipt className="h-5 w-5 text-brand-600" />
          <h3 className="text-base font-semibold text-gray-900 dark:text-white">Top Merchants / Descriptions — {getMonthName(month)}</h3>
        </div>
        {topDescs.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-gray-200 dark:border-slate-700">
                  <th className="text-left py-3 px-2 font-medium text-gray-500 dark:text-slate-400">#</th>
                  <th className="text-left py-3 px-2 font-medium text-gray-500 dark:text-slate-400">Description</th>
                  <th className="text-right py-3 px-2 font-medium text-gray-500 dark:text-slate-400">Count</th>
                  <th className="text-right py-3 px-2 font-medium text-gray-500 dark:text-slate-400">Total</th>
                  <th className="text-right py-3 px-2 font-medium text-gray-500 dark:text-slate-400">% of Spend</th>
                </tr>
              </thead>
              <tbody>
                {topDescs.map((d, i) => (
                  <tr key={i} className="border-b border-gray-100 dark:border-slate-700/50 hover:bg-gray-50 dark:hover:bg-slate-800/50">
                    <td className="py-2.5 px-2 text-gray-400 dark:text-slate-500">{i + 1}</td>
                    <td className="py-2.5 px-2 text-gray-900 dark:text-slate-200 font-medium">{d.description}</td>
                    <td className="py-2.5 px-2 text-right text-gray-500 dark:text-slate-400">{d.count}</td>
                    <td className="py-2.5 px-2 text-right font-medium text-gray-900 dark:text-white">{formatCurrency(d.totalAmount)}</td>
                    <td className="py-2.5 px-2 text-right text-gray-500 dark:text-slate-400">
                      {totalSpending > 0 ? ((d.totalAmount / totalSpending) * 100).toFixed(1) : '0.0'}%
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <EmptyChart message="No description data for this month." />
        )}
      </div>
    </div>
  );
}

/* ── Helper components ── */

function KpiCard({ label, value, icon, color, change, positive, subtitle }: {
  label: string; value: string; icon: React.ReactNode; color: string;
  change?: number; positive?: boolean; subtitle?: string;
}) {
  const colorMap: Record<string, string> = {
    emerald: 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-600 dark:text-emerald-400',
    red: 'bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400',
    brand: 'bg-brand-100 dark:bg-brand-900/30 text-brand-600 dark:text-brand-400',
    amber: 'bg-amber-100 dark:bg-amber-900/30 text-amber-600 dark:text-amber-400',
    violet: 'bg-violet-100 dark:bg-violet-900/30 text-violet-600 dark:text-violet-400',
    cyan: 'bg-cyan-100 dark:bg-cyan-900/30 text-cyan-600 dark:text-cyan-400',
  };

  return (
    <div className="card-hover p-4">
      <div className="flex items-center justify-between mb-2">
        <span className="text-xs font-medium text-gray-500 dark:text-slate-400">{label}</span>
        <div className={`flex h-7 w-7 items-center justify-center rounded-md ${colorMap[color]}`}>
          {icon}
        </div>
      </div>
      <p className="text-lg font-bold text-gray-900 dark:text-white truncate">{value}</p>
      {change !== undefined && positive !== undefined && (
        <div className={`flex items-center gap-0.5 mt-1 text-[11px] font-medium ${positive ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-600 dark:text-red-400'}`}>
          {positive ? <ArrowUpRight className="h-3 w-3" /> : <ArrowDownRight className="h-3 w-3" />}
          {Math.abs(change).toFixed(1)}% vs last mo
        </div>
      )}
      {subtitle && (
        <p className="text-[11px] text-gray-400 dark:text-slate-500 mt-1">{subtitle}</p>
      )}
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
