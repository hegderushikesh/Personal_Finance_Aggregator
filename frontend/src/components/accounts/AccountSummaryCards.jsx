import React from 'react';
import { Wallet, PiggyBank, CreditCard, TrendingUp } from 'lucide-react';

export default function AccountSummaryCards({ summary, loading = false }) {
  const totalCash = parseFloat(summary?.totalCash || 0);
  const totalSavings = parseFloat(summary?.totalSavings || 0);
  const totalCredit = parseFloat(summary?.totalCredit || 0);
  const netWorth = parseFloat(summary?.netWorth || 0);

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {/* Total Cash */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs flex items-center justify-between">
        <div>
          <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
            Total Cash
          </span>
          <div className="font-display font-black text-2xl text-slate-900 mt-1">
            {loading ? '—' : `₹${totalCash.toLocaleString()}`}
          </div>
          <span className="text-[10px] text-slate-400 font-medium">Checking & cash wallets</span>
        </div>
        <div className="w-11 h-11 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center border border-blue-100">
          <Wallet className="w-6 h-6" />
        </div>
      </div>

      {/* Total Savings */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs flex items-center justify-between">
        <div>
          <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
            Total Savings
          </span>
          <div className="font-display font-black text-2xl text-indigo-600 mt-1">
            {loading ? '—' : `₹${totalSavings.toLocaleString()}`}
          </div>
          <span className="text-[10px] text-slate-400 font-medium">Emergency & interest accounts</span>
        </div>
        <div className="w-11 h-11 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center border border-indigo-100">
          <PiggyBank className="w-6 h-6" />
        </div>
      </div>

      {/* Total Credit */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs flex items-center justify-between">
        <div>
          <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
            Credit Liabilities
          </span>
          <div className="font-display font-black text-2xl text-rose-600 mt-1">
            {loading ? '—' : `₹${totalCredit.toLocaleString()}`}
          </div>
          <span className="text-[10px] text-slate-400 font-medium">Credit card balance owed</span>
        </div>
        <div className="w-11 h-11 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center border border-rose-100">
          <CreditCard className="w-6 h-6" />
        </div>
      </div>

      {/* Net Worth */}
      <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs flex items-center justify-between">
        <div>
          <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
            Net Worth
          </span>
          <div
            className={`font-display font-black text-2xl mt-1 ${
              netWorth >= 0 ? 'text-emerald-600' : 'text-rose-600'
            }`}
          >
            {loading ? '—' : `${netWorth < 0 ? '-' : ''}₹${Math.abs(netWorth).toLocaleString()}`}
          </div>
          <span className="text-[10px] text-slate-400 font-medium">Cash + Savings - Credit</span>
        </div>
        <div className="w-11 h-11 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center border border-emerald-100">
          <TrendingUp className="w-6 h-6" />
        </div>
      </div>
    </div>
  );
}
