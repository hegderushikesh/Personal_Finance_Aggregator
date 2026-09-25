import React from 'react';
import { TrendingUp, TrendingDown, Wallet, Landmark, ArrowUpRight, ArrowDownRight } from 'lucide-react';

export function formatINR(amount) {
  const num = typeof amount === 'number' ? amount : parseFloat(amount) || 0;
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(num);
}

export default function SummaryCards({
  incomeData,
  expenseData,
  cashFlowData,
  netWorthData,
  isLoading,
}) {
  // Aggregate totals across all months in the selected period
  const totalIncome = (incomeData || []).reduce(
    (sum, item) => sum + (parseFloat(item.income) || 0),
    0
  );
  const totalExpense = (expenseData || []).reduce(
    (sum, item) => sum + (parseFloat(item.amount || item.expense) || 0),
    0
  );
  const netCashFlow = totalIncome - totalExpense;
  const netWorth = netWorthData ? parseFloat(netWorthData.netWorth) || 0 : 0;

  const cards = [
    {
      title: 'Total Income',
      amount: totalIncome,
      icon: TrendingUp,
      badge: 'Inflow',
      badgeColor: 'text-emerald-700 bg-emerald-50 border-emerald-200',
      iconColor: 'text-emerald-600 bg-emerald-50',
      arrow: ArrowUpRight,
      arrowColor: 'text-emerald-600',
    },
    {
      title: 'Total Expenses',
      amount: totalExpense,
      icon: TrendingDown,
      badge: 'Outflow',
      badgeColor: 'text-rose-700 bg-rose-50 border-rose-200',
      iconColor: 'text-rose-600 bg-rose-50',
      arrow: ArrowDownRight,
      arrowColor: 'text-rose-600',
    },
    {
      title: 'Net Cash Flow',
      amount: netCashFlow,
      icon: Wallet,
      badge: netCashFlow >= 0 ? 'Surplus' : 'Deficit',
      badgeColor:
        netCashFlow >= 0
          ? 'text-blue-700 bg-blue-50 border-blue-200'
          : 'text-amber-700 bg-amber-50 border-amber-200',
      iconColor: netCashFlow >= 0 ? 'text-blue-600 bg-blue-50' : 'text-amber-600 bg-amber-50',
      isNet: true,
    },
    {
      title: 'Net Worth',
      amount: netWorth,
      icon: Landmark,
      badge: 'Accounts',
      badgeColor: 'text-indigo-700 bg-indigo-50 border-indigo-200',
      iconColor: 'text-indigo-600 bg-indigo-50',
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {cards.map((card) => {
        const Icon = card.icon;
        return (
          <div
            key={card.title}
            className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs hover:shadow-md transition-shadow relative overflow-hidden"
          >
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                {card.title}
              </span>
              <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${card.iconColor}`}>
                <Icon className="w-5 h-5" />
              </div>
            </div>

            {isLoading ? (
              <div className="space-y-2">
                <div className="h-7 w-32 bg-slate-200 rounded-md animate-pulse" />
                <div className="h-4 w-20 bg-slate-100 rounded-md animate-pulse" />
              </div>
            ) : (
              <div>
                <div className="flex items-baseline gap-2">
                  <span className="text-2xl font-black font-display text-slate-900 tracking-tight">
                    {formatINR(card.amount)}
                  </span>
                </div>
                <div className="mt-2 flex items-center gap-1.5">
                  <span
                    className={`inline-flex items-center px-2 py-0.5 rounded-md text-[11px] font-semibold border ${card.badgeColor}`}
                  >
                    {card.badge}
                  </span>
                  <span className="text-[11px] text-slate-400">
                    {card.title === 'Net Worth' ? 'Total balance' : 'In selected period'}
                  </span>
                </div>
              </div>
            )}
          </div>
        );
      })}
    </div>
  );
}
