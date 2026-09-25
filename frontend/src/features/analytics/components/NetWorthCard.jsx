import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Landmark, ArrowUpRight, ArrowDownRight, Info, ExternalLink } from 'lucide-react';
import { formatINR } from './SummaryCards';

export default function NetWorthCard({ data, isLoading }) {
  const navigate = useNavigate();
  const assets = data ? parseFloat(data.assets) || 0 : 0;
  const liabilities = data ? parseFloat(data.liabilities) || 0 : 0;
  const netWorth = data ? parseFloat(data.netWorth) || 0 : 0;

  const totalPool = assets + liabilities;
  const assetRatio = totalPool > 0 ? (assets / totalPool) * 100 : 100;
  const liabilityRatio = totalPool > 0 ? (liabilities / totalPool) * 100 : 0;

  return (
    <div className="bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs flex flex-col justify-between">
      <div>
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="text-base font-bold text-slate-900 font-display">Net Worth Overview</h3>
            <p className="text-xs text-slate-500 mt-0.5">Assets minus liabilities across real accounts</p>
          </div>
          <button
            onClick={() => navigate('/accounts')}
            className="flex items-center gap-1 text-xs font-semibold text-blue-600 hover:text-blue-700 bg-blue-50 px-2.5 py-1.5 rounded-lg transition-colors"
          >
            <span>Accounts</span>
            <ExternalLink className="w-3 h-3" />
          </button>
        </div>

        {isLoading ? (
          <div className="h-64 flex items-center justify-center">
            <div className="w-10 h-10 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin" />
          </div>
        ) : (
          <div className="space-y-6">
            {/* Big Net Worth Value */}
            <div className="p-5 rounded-2xl bg-gradient-to-br from-slate-900 via-slate-800 to-indigo-950 text-white shadow-md">
              <div className="flex items-center justify-between">
                <span className="text-xs font-semibold uppercase tracking-wider text-slate-400">
                  Current Net Worth
                </span>
                <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-blue-500/20 text-blue-300 border border-blue-400/30">
                  Real-time
                </span>
              </div>
              <p className="text-3xl font-black font-display mt-2 tracking-tight">
                {formatINR(netWorth)}
              </p>
              <p className="text-[11px] text-slate-400 mt-1">Formula: Total Assets − Total Liabilities</p>
            </div>

            {/* Assets vs Liabilities Breakdown */}
            <div className="space-y-3">
              <div className="flex items-center justify-between text-xs font-semibold text-slate-700">
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 rounded-full bg-emerald-500" />
                  <span>Assets: {formatINR(assets)}</span>
                </div>
                <div className="flex items-center gap-2">
                  <span className="w-3 h-3 rounded-full bg-rose-500" />
                  <span>Liabilities: {formatINR(liabilities)}</span>
                </div>
              </div>

              {/* Stacked Ratio Bar */}
              <div className="h-3 w-full bg-slate-100 rounded-full overflow-hidden flex">
                <div
                  className="bg-emerald-500 h-full transition-all duration-500"
                  style={{ width: `${assetRatio}%` }}
                />
                <div
                  className="bg-rose-500 h-full transition-all duration-500"
                  style={{ width: `${liabilityRatio}%` }}
                />
              </div>

              <div className="grid grid-cols-2 gap-3 pt-2">
                <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
                  <div className="flex items-center gap-1.5 text-xs text-emerald-700 font-semibold mb-1">
                    <ArrowUpRight className="w-3.5 h-3.5" />
                    <span>Assets Pool</span>
                  </div>
                  <p className="text-lg font-bold text-slate-900">{formatINR(assets)}</p>
                  <p className="text-[10px] text-slate-400 mt-0.5">Checking, Savings, Cash, Investments</p>
                </div>

                <div className="p-3 rounded-xl bg-slate-50 border border-slate-100">
                  <div className="flex items-center gap-1.5 text-xs text-rose-700 font-semibold mb-1">
                    <ArrowDownRight className="w-3.5 h-3.5" />
                    <span>Liabilities Pool</span>
                  </div>
                  <p className="text-lg font-bold text-slate-900">{formatINR(liabilities)}</p>
                  <p className="text-[10px] text-slate-400 mt-0.5">Credit Cards & outstanding debt</p>
                </div>
              </div>
            </div>

            {/* Note on Historical Snapshotting */}
            <div className="flex items-start gap-2 p-3 rounded-xl bg-slate-50 border border-slate-200/60 text-slate-500 text-[11px] leading-relaxed">
              <Info className="w-4 h-4 text-blue-500 shrink-0 mt-0.5" />
              <span>
                Net worth is calculated strictly from active accounts stored in PostgreSQL. Historical balance reconstruction without ledger snapshots is omitted to prevent fabricated numbers.
              </span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
