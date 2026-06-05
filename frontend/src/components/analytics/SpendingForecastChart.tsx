import { SpendingForecastResponse, HistoricalPoint, ForecastPoint } from '../../types';
import {
  ComposedChart,
  Bar,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Area,
  AreaChart,
} from 'recharts';
import { formatCurrency } from '../../utils/formatters';

interface ChartDataPoint {
  month: string;
  actual?: number;
  forecast?: number;
  lowerBound?: number;
  upperBound?: number;
  type: 'historical' | 'forecast';
}

export default function SpendingForecastChart({ data }: { data: SpendingForecastResponse }) {
  const getMonthLabel = (month: number, year: number): string => {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return `${months[month - 1]} ${year}`;
  };

  // Combine historical and forecast data
  const chartData: ChartDataPoint[] = [
    // Historical data as bars
    ...data.historical.map((h: HistoricalPoint) => ({
      month: getMonthLabel(h.month, h.year),
      actual: h.actual,
      type: 'historical' as const,
    })),
    // Forecast data as line with confidence intervals
    ...data.forecasts.map((f: ForecastPoint) => ({
      month: getMonthLabel(f.month, f.year),
      forecast: f.forecast,
      lowerBound: f.lowerBound,
      upperBound: f.upperBound,
      type: 'forecast' as const,
    })),
  ];

  const confidenceColor: Record<string, string> = {
    HIGH: '#10b981',
    MEDIUM: '#f59e0b',
    LOW: '#ef4444',
  };

  const trendColor = data.trend >= 0 ? '#ef4444' : '#10b981'; // Red if spending increasing, green if decreasing
  const trendLabel = data.trend >= 0 ? 'Increasing' : 'Decreasing';
  const confidenceKey = (data.confidence as keyof typeof confidenceColor) || 'MEDIUM';

  return (
    <div className="space-y-4">
      {/* Summary stats */}
      <div className="grid grid-cols-3 gap-4">
        <div className="bg-slate-50 dark:bg-slate-900 rounded-lg p-3">
          <p className="text-xs text-slate-600 dark:text-slate-400 font-medium">Average Monthly</p>
          <p className="text-lg font-semibold text-slate-900 dark:text-slate-100">
            {formatCurrency(data.averageMonthlySpending)}
          </p>
        </div>
        <div className="bg-slate-50 dark:bg-slate-900 rounded-lg p-3">
          <p className="text-xs text-slate-600 dark:text-slate-400 font-medium">Trend</p>
          <div className="flex items-center gap-2 mt-1">
            <span className="text-lg font-semibold" style={{ color: trendColor }}>
              {data.trend > 0 ? '+' : ''}{Math.abs(data.trend).toFixed(1)}%
            </span>
            <span className="text-xs font-medium" style={{ color: trendColor }}>
              {trendLabel}
            </span>
          </div>
        </div>
        <div className="bg-slate-50 dark:bg-slate-900 rounded-lg p-3">
          <p className="text-xs text-slate-600 dark:text-slate-400 font-medium">Confidence</p>
          <div className="flex items-center gap-2 mt-1">
            <div
              className="w-3 h-3 rounded-full"
              style={{ backgroundColor: confidenceColor[confidenceKey] }}
            />
            <span className="text-sm font-semibold text-slate-900 dark:text-slate-100">
              {data.confidence}
            </span>
          </div>
        </div>
      </div>

      {/* Chart */}
      <div className="bg-white dark:bg-slate-950 rounded-xl border border-slate-200 dark:border-slate-800 p-6">
        <ResponsiveContainer width="100%" height={400}>
          <ComposedChart data={chartData}>
            <defs>
              <linearGradient id="confidenceGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor={confidenceColor[confidenceKey]} stopOpacity={0.1} />
                <stop offset="95%" stopColor={confidenceColor[confidenceKey]} stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
            <XAxis
              dataKey="month"
              tick={{ fontSize: 12 }}
              stroke="#94a3b8"
            />
            <YAxis
              tickFormatter={(value) => `$${(value / 1000).toFixed(0)}k`}
              tick={{ fontSize: 12 }}
              stroke="#94a3b8"
            />
            <Tooltip
              contentStyle={{
                backgroundColor: '#1e293b',
                border: '1px solid #475569',
                borderRadius: '0.5rem',
              }}
              labelStyle={{ color: '#e2e8f0' }}
              formatter={(value: number) => formatCurrency(value)}
            />
            <Legend
              wrapperStyle={{ fontSize: 12 }}
              iconType="line"
            />

            {/* Confidence interval as area between bounds */}
            <Area
              name="Confidence Interval"
              type="monotone"
              dataKey="lowerBound"
              fill="url(#confidenceGradient)"
              stroke="transparent"
              isAnimationActive={false}
            />

            {/* Historical spending as bars */}
            <Bar
              name="Actual Spending"
              dataKey="actual"
              fill="#6366f1"
              radius={[8, 8, 0, 0]}
            />

            {/* Forecast as line */}
            <Line
              name="Forecast"
              type="monotone"
              dataKey="forecast"
              stroke="#f43f5e"
              strokeWidth={2}
              dot={{ fill: '#f43f5e', r: 4 }}
              activeDot={{ r: 6 }}
            />
          </ComposedChart>
        </ResponsiveContainer>
      </div>

      {/* Description */}
      <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
        <p className="text-sm text-blue-900 dark:text-blue-300">
          <strong>Forecast Info:</strong> This forecast uses a 6-month moving average algorithm based on your recent
          spending patterns. The confidence interval (shaded area) shows the ±10% range around the forecast. Confidence
          level is determined by spending variability.
        </p>
      </div>
    </div>
  );
}
