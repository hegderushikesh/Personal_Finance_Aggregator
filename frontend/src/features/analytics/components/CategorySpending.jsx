import React from 'react';
import { useNavigate } from 'react-router-dom';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
} from 'recharts';
import { Layers, ArrowRight, AlertCircle } from 'lucide-react';
import { formatINR } from './SummaryCards';

export default function CategorySpending({ data = [], isLoading }) {
  const navigate = useNavigate();

  const handleCategoryClick = (categoryId) => {
    if (categoryId) {
      navigate(`/transactions?categoryId=${categoryId}`);
    } else {
      navigate('/transactions');
    }
  };

  // Top categories for horizontal bar chart (e.g., top 6)
  const topCategories = (data || []).slice(0, 6).map((item) => ({
    name: item.category,
    amount: parseFloat(item.amount) || 0,
    categoryId: item.categoryId,
  }));

  const CustomTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      const entry = payload[0];
      return (
        <div className="bg-slate-900 text-white px-3 py-2 rounded-xl text-xs shadow-lg border border-slate-700">
          <p className="font-semibold text-slate-300">{entry.payload.name}</p>
          <p className="text-sm font-bold text-blue-400">{formatINR(entry.value)}</p>
        </div>
      );
    }
    return null;
  };

  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs flex flex-col justify-between">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h3 className="text-base font-bold text-slate-900 font-display">Category Spending</h3>
          <p className="text-xs text-slate-500 mt-0.5">Ranked by total expense volume</p>
        </div>
        <div className="w-8 h-8 rounded-xl bg-amber-50 flex items-center justify-center text-amber-600">
          <Layers className="w-4 h-4" />
        </div>
      </div>

      {isLoading ? (
        <div className="h-64 flex items-center justify-center">
          <div className="w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
        </div>
      ) : data.length === 0 ? (
        <div className="h-64 flex flex-col items-center justify-center text-center p-6 text-slate-400 bg-slate-50/50 rounded-xl border border-dashed border-slate-200">
          <AlertCircle className="w-10 h-10 text-slate-300 mb-2" />
          <p className="text-sm font-medium text-slate-600">No category spending data for this period.</p>
          <p className="text-xs text-slate-400 mt-1">Expenses assigned to categories will appear here.</p>
        </div>
      ) : (
        <div className="space-y-6">
          {/* Horizontal Bar Chart for Top Categories */}
          {topCategories.length > 0 && (
            <div className="h-48 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  layout="vertical"
                  data={topCategories}
                  margin={{ top: 5, right: 20, left: 10, bottom: 5 }}
                >
                  <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="#E2E8F0" />
                  <XAxis
                    type="number"
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: '#64748B', fontSize: 11 }}
                    tickFormatter={(val) => `₹${val >= 1000 ? `${(val / 1000).toFixed(0)}k` : val}`}
                  />
                  <YAxis
                    type="category"
                    dataKey="name"
                    axisLine={false}
                    tickLine={false}
                    width={90}
                    tick={{ fill: '#475569', fontSize: 11, fontWeight: 500 }}
                  />
                  <Tooltip content={<CustomTooltip />} />
                  <Bar dataKey="amount" fill="#3B82F6" radius={[0, 4, 4, 0]} maxBarSize={20} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          )}

          {/* Detailed Category Table */}
          <div className="overflow-x-auto rounded-xl border border-slate-200/80">
            <table className="w-full text-left text-xs">
              <thead className="bg-slate-50 text-slate-500 font-semibold border-b border-slate-200/80 uppercase tracking-wider text-[10px]">
                <tr>
                  <th className="py-2.5 px-3.5">Category</th>
                  <th className="py-2.5 px-3.5">Group</th>
                  <th className="py-2.5 px-3.5 text-center">Transactions</th>
                  <th className="py-2.5 px-3.5 text-right">Amount</th>
                  <th className="py-2.5 px-2 text-center w-8"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 font-medium">
                {data.map((cat, idx) => (
                  <tr
                    key={cat.categoryId || `cat-${idx}`}
                    onClick={() => handleCategoryClick(cat.categoryId)}
                    className="hover:bg-blue-50/50 cursor-pointer transition-colors group"
                  >
                    <td className="py-2.5 px-3.5 font-semibold text-slate-800">
                      {cat.category}
                    </td>
                    <td className="py-2.5 px-3.5 text-slate-500">
                      <span className="inline-flex px-2 py-0.5 rounded-md bg-slate-100 text-slate-600 text-[11px]">
                        {cat.group || cat.categoryGroup || 'Uncategorized'}
                      </span>
                    </td>
                    <td className="py-2.5 px-3.5 text-center text-slate-600">
                      {cat.transactionCount}
                    </td>
                    <td className="py-2.5 px-3.5 text-right font-bold text-slate-900">
                      {formatINR(cat.amount)}
                    </td>
                    <td className="py-2.5 px-2 text-center">
                      <ArrowRight className="w-3.5 h-3.5 text-slate-400 group-hover:text-blue-600 transition-colors" />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}
