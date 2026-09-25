import React from 'react';
import { Repeat, Sparkles, CheckCircle2 } from 'lucide-react';

export default function RecurringEmptyState({ onScan, isScanning }) {
  return (
    <div className="bg-white rounded-3xl border border-slate-200/80 p-8 sm:p-12 text-center max-w-2xl mx-auto shadow-xs">
      <div className="w-16 h-16 rounded-2xl bg-gradient-to-tr from-blue-500 to-indigo-600 text-white flex items-center justify-center mx-auto shadow-md mb-6">
        <Repeat className="w-8 h-8" />
      </div>

      <h3 className="text-xl font-bold font-display text-slate-900 mb-2">
        No recurring payments detected yet
      </h3>

      <p className="text-sm text-slate-500 max-w-md mx-auto mb-8 leading-relaxed">
        We'll look for repeated merchants, similar amounts, and regular payment intervals across your expense history.
      </p>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-left max-w-lg mx-auto mb-8">
        <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-100 flex items-start gap-2.5">
          <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
          <div className="text-xs">
            <span className="font-semibold text-slate-700 block">Same Merchant</span>
            <span className="text-slate-400 text-[11px]">Identifies normalized merchant names</span>
          </div>
        </div>

        <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-100 flex items-start gap-2.5">
          <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
          <div className="text-xs">
            <span className="font-semibold text-slate-700 block">Similar Amount</span>
            <span className="text-slate-400 text-[11px]">Handles minor price variations</span>
          </div>
        </div>

        <div className="p-3.5 rounded-xl bg-slate-50 border border-slate-100 flex items-start gap-2.5">
          <CheckCircle2 className="w-4 h-4 text-emerald-500 shrink-0 mt-0.5" />
          <div className="text-xs">
            <span className="font-semibold text-slate-700 block">Regular Cadence</span>
            <span className="text-slate-400 text-[11px]">Weekly, Monthly, or Yearly intervals</span>
          </div>
        </div>
      </div>

      <button
        onClick={onScan}
        disabled={isScanning}
        className="inline-flex items-center gap-2.5 px-6 py-3 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-sm shadow-sm hover:shadow-md transition-all disabled:opacity-50"
      >
        <Sparkles className={`w-4 h-4 ${isScanning ? 'animate-spin' : ''}`} />
        <span>{isScanning ? 'Analyzing Transactions...' : 'Scan Transactions'}</span>
      </button>
    </div>
  );
}
