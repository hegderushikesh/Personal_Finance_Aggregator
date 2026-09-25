import React from 'react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
} from 'recharts';
import { ArrowLeftRight, AlertCircle } from 'lucide-react';
import { formatINR } from './SummaryCards';

function formatMonthLabel(monthStr) {
  if (!monthStr) return '';
  const parts = monthStr.split('-');
  if (parts.length < 2) return monthStr;
  const year = parts[0];
  const monthIdx = parseInt(parts[1], 10) - 1;
  const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  return `${monthNames[monthIdx] || ''} '${year.slice(-2)}`;
}

export default function IncomeExpenseChart({ data = [], isLoading }) {
  const chartData = data.map((item) => ({
    month: item.month,
    formattedMonth: formatMonthLabel(item.month),
    income: parseFloat(item.income) || 0,
    expense: parseFloat(item.expense) || 0,
  }));

  const hasData = chartData.some((item) => item.income > 0 || item.expense > 0);

  const CustomTooltip = ({ active, payload, label }) => {
    if (active && payload && payload.length) {
      const entry = payload[0];
      return (
        <div className="bg-slate-900 text-white px-3.5 py-2.5 rounded-xl text-xs shadow-lg border border-slate-700 space-y-1">
          <p className="font-semibold text-slate-300 border-b border-slate-700 pb-1">
            {entry.payload.formattedMonth}
          </p>
          <div className="flex items-center justify-between gap-4">
            <span className="text-emerald-400 font-medium">Income:</span>
            <span className="font-bold">{formatINR(entry.payload.income)}</span>
          </div>
          <div className="flex items-center justify-between gap-4">
            <span className="text-rose-400 font-medium">Expense:</span>
            <span className="font-bold">{formatINR(entry.payload.expense)}</span>
          </div>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs flex flex-col justify-between">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h3 className="text-base font-bold text-slate-900 font-display">Income vs Expense</h3>
          <p className="text-xs text-slate-500 mt-0.5">Monthly cash inflow compared against expenses</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-1.5 text-xs font-semibold text-emerald-700">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
            <span>Income</span>
          </div>
          <div className="flex items-center gap-1.5 text-xs font-semibold text-rose-700">
            <span className="w-2.5 h-2.5 rounded-full bg-rose-500" />
            <span>Expense</span>
          </div>
        </div>
      </div>

      {isLoading ? (
        <div className="h-64 flex items-center justify-center">
          <div className="w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
        </div>
      ) : chartData.length === 0 || !hasData ? (
        <div className="h-64 flex flex-col items-center justify-center text-center p-6 text-slate-400 bg-slate-50/50 rounded-xl border border-dashed border-slate-200">
          <AlertCircle className="w-10 h-10 text-slate-300 mb-2" />
          <p className="text-sm font-medium text-slate-600">No income or expense data for this period.</p>
          <p className="text-xs text-slate-400 mt-1">Both income and expense flows will be graphed side by side.</p>
        </div>
      ) : (
        <div className="h-64 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <BarChart
              data={chartData}
              margin={{ top: 10, right: 10, left: 10, bottom: 5 }}
              barGap={4}
            >
              <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
              <XAxis
                dataKey="formattedMonth"
                axisLine={false}
                tickLine={false}
                tick={{ fill: '#64748B', fontSize: 11 }}
              />
              <YAxis
                axisLine={false}
                tickLine={false}
                tick={{ fill: '#64748B', fontSize: 11 }}
                tickFormatter={(val) => `₹${val >= 1000 ? `${(val / 1000).toFixed(0)}k` : val}`}
              />
              <Tooltip content={<CustomTooltip />} />
              <Bar dataKey="income" name="Income" fill="#10B981" radius={[4, 4, 0, 0]} maxBarSize={32} />
              <Bar dataKey="expense" name="Expense" fill="#F43F5E" radius={[4, 4, 0, 0]} maxBarSize={32} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}
    </div>
  );
}
