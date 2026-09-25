import React from 'react';
import { ResponsiveContainer, PieChart, Pie, Cell, Tooltip } from 'recharts';
import { PieChart as PieIcon, AlertCircle } from 'lucide-react';
import { formatINR } from './SummaryCards';

const PALETTE = [
  '#2563EB', // Blue
  '#0D9488', // Teal
  '#F59E0B', // Amber
  '#E11D48', // Rose
  '#8B5CF6', // Purple
  '#10B981', // Emerald
  '#6366F1', // Indigo
  '#EC4899', // Pink
  '#F97316', // Orange
  '#64748B', // Slate
];

export default function SpendingBreakdown({ data = [], isLoading }) {
  const totalSpending = data.reduce(
    (sum, item) => sum + (parseFloat(item.amount) || 0),
    0
  );

  const chartData = data.map((item, idx) => ({
    name: item.category,
    value: parseFloat(item.amount) || 0,
    percentage: item.percentage || (totalSpending > 0 ? ((item.amount / totalSpending) * 100).toFixed(1) : 0),
    color: PALETTE[idx % PALETTE.length],
  }));

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      const entry = payload[0];
      return (
        <div className="bg-slate-900 text-white px-3 py-2 rounded-xl text-xs shadow-lg border border-slate-700">
          <p className="font-semibold text-slate-200">{entry.name}</p>
          <p className="text-sm font-bold text-blue-400">{formatINR(entry.value)}</p>
          <p className="text-[11px] text-slate-400">{entry.payload.percentage}% of spending</p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs flex flex-col justify-between">
      {/* Card Header */}
      <div className="flex items-center justify-between mb-4">
        <div>
          <h3 className="text-base font-bold text-slate-900 font-display">Spending Breakdown</h3>
          <p className="text-xs text-slate-500 mt-0.5">By category for expense transactions</p>
        </div>
        <div className="text-right">
          <span className="text-[11px] font-semibold uppercase text-slate-400">Total Spending</span>
          <p className="text-lg font-black text-slate-900 font-display">{formatINR(totalSpending)}</p>
        </div>
      </div>

      {isLoading ? (
        <div className="h-64 flex items-center justify-center">
          <div className="w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
        </div>
      ) : chartData.length === 0 || totalSpending === 0 ? (
        <div className="h-64 flex flex-col items-center justify-center text-center p-6 text-slate-400 bg-slate-50/50 rounded-xl border border-dashed border-slate-200">
          <AlertCircle className="w-10 h-10 text-slate-300 mb-2" />
          <p className="text-sm font-medium text-slate-600">No spending data for this period.</p>
          <p className="text-xs text-slate-400 mt-1">Expenses recorded in this range will appear here.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-12 gap-4 items-center">
          {/* Donut Chart */}
          <div className="md:col-span-6 h-60 relative flex items-center justify-center">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Tooltip content={<CustomTooltip />} />
                <Pie
                  data={chartData}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={85}
                  paddingAngle={3}
                  dataKey="value"
                >
                  {chartData.map((entry) => (
                    <Cell key={entry.name} fill={entry.color} stroke="#ffffff" strokeWidth={2} />
                  ))}
                </Pie>
              </PieChart>
            </ResponsiveContainer>
            {/* Center Label */}
            <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
              <span className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">Total</span>
              <span className="text-sm font-black text-slate-800">{formatINR(totalSpending)}</span>
            </div>
          </div>

          {/* Category Legend List */}
          <div className="md:col-span-6 max-h-60 overflow-y-auto space-y-2 pr-1">
            {chartData.map((item) => (
              <div
                key={item.name}
                className="flex items-center justify-between p-2 rounded-xl hover:bg-slate-50 transition-colors text-xs"
              >
                <div className="flex items-center gap-2 min-w-0">
                  <span
                    className="w-2.5 h-2.5 rounded-full shrink-0"
                    style={{ backgroundColor: item.color }}
                  />
                  <span className="font-semibold text-slate-700 truncate">{item.name}</span>
                </div>
                <div className="text-right shrink-0">
                  <span className="font-bold text-slate-900 mr-2">{formatINR(item.value)}</span>
                  <span className="text-[11px] font-medium text-slate-400">
                    {item.percentage}%
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
