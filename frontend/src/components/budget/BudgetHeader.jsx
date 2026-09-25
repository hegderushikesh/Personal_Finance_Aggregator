import React from 'react';
import {
  ChevronLeft,
  ChevronRight,
  Plus,
  FolderPlus,
  Tag,
  Sparkles,
  ArrowRightLeft,
  Copy,
  Activity,
  Wallet,
  CheckCircle,
  AlertTriangle,
  Layers
} from 'lucide-react';

export default function BudgetHeader({
  year,
  month,
  startingBalance,
  readyToAssign,
  budgetHealth,
  onPrevMonth,
  onNextMonth,
  onToday,
  onOpenStartingBalance,
  onOpenNewGroup,
  onOpenNewCategory,
  onOpenAutoAssign,
  onOpenMoveMoney,
  onCopyPreviousMonth,
  onOpenRecentMoves,
}) {
  const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  const monthLabel = `${monthNames[month - 1]} ${year}`;

  const isPositive = (readyToAssign || 0) >= 0;

  return (
    <div className="space-y-6">
      {/* Top Controls: Month Selector & Core Hero Section */}
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs">
        {/* Month Navigation */}
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-1 bg-slate-100 p-1 rounded-xl">
            <button
              onClick={onPrevMonth}
              className="p-2 text-slate-600 hover:text-slate-900 hover:bg-white rounded-lg transition-all"
              title="Previous Month"
            >
              <ChevronLeft className="w-5 h-5" />
            </button>
            <span className="font-display font-bold text-lg text-slate-800 px-3 min-w-[150px] text-center">
              {monthLabel}
            </span>
            <button
              onClick={onNextMonth}
              className="p-2 text-slate-600 hover:text-slate-900 hover:bg-white rounded-lg transition-all"
              title="Next Month"
            >
              <ChevronRight className="w-5 h-5" />
            </button>
          </div>

          <button
            onClick={onToday}
            className="px-3 py-2 text-xs font-bold text-blue-600 hover:bg-blue-50 rounded-xl transition-all border border-blue-200/60"
          >
            Today
          </button>
        </div>

        {/* Hero Ready To Assign Banner */}
        <div className="flex items-center gap-4">
          {/* Starting Cash Card */}
          <div
            onClick={onOpenStartingBalance}
            className="cursor-pointer p-3.5 rounded-xl border border-slate-200 hover:border-slate-300 bg-slate-50/50 hover:bg-slate-50 transition-all flex items-center gap-3 group"
          >
            <div className="w-9 h-9 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center font-bold">
              <Wallet className="w-4 h-4" />
            </div>
            <div>
              <span className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                Starting Cash
              </span>
              <span className="block text-sm font-bold text-slate-800 group-hover:text-blue-600 transition-colors">
                ₹{(startingBalance || 0).toLocaleString()}
              </span>
            </div>
          </div>

          {/* Ready To Assign Hero Card */}
          <div
            className={`px-6 py-3.5 rounded-xl border flex items-center gap-4 shadow-sm transition-all ${
              isPositive
                ? 'bg-emerald-500 text-white border-emerald-600 shadow-emerald-500/10'
                : 'bg-rose-500 text-white border-rose-600 shadow-rose-500/10'
            }`}
          >
            <div className="flex flex-col">
              <span className="text-[10px] font-bold uppercase tracking-widest text-white/80">
                Ready to Assign
              </span>
              <span className="font-display font-black text-2xl tracking-tight leading-tight">
                ₹{(readyToAssign || 0).toLocaleString()}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Budget Health Bar & Quick Actions Toolbar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-slate-100/70 p-3 rounded-2xl border border-slate-200/60">
        {/* Health Pills */}
        <div className="flex items-center gap-2 overflow-x-auto text-xs font-bold text-slate-600 px-1 py-0.5">
          <span className="text-slate-400 text-[11px] uppercase tracking-wider font-semibold mr-1">
            Budget Health:
          </span>
          <span className="px-2.5 py-1 rounded-lg bg-emerald-100 text-emerald-800 flex items-center gap-1.5">
            <CheckCircle className="w-3.5 h-3.5" />
            {budgetHealth?.fundedCount || 0} Funded
          </span>
          {budgetHealth?.underfundedCount > 0 && (
            <span className="px-2.5 py-1 rounded-lg bg-amber-100 text-amber-800 flex items-center gap-1.5">
              <AlertTriangle className="w-3.5 h-3.5" />
              {budgetHealth.underfundedCount} Underfunded
            </span>
          )}
          {budgetHealth?.overfundedCount > 0 && (
            <span className="px-2.5 py-1 rounded-lg bg-blue-100 text-blue-800 flex items-center gap-1.5">
              <Layers className="w-3.5 h-3.5" />
              {budgetHealth.overfundedCount} Overfunded
            </span>
          )}
          {budgetHealth?.targetsMetCount > 0 && (
            <span className="px-2.5 py-1 rounded-lg bg-indigo-100 text-indigo-800">
              ✓ {budgetHealth.targetsMetCount} Targets Met
            </span>
          )}
        </div>

        {/* Action Buttons */}
        <div className="flex items-center gap-2 flex-wrap">
          <button
            onClick={onOpenNewGroup}
            className="px-3 py-2 rounded-xl text-xs font-bold text-slate-700 bg-white hover:bg-slate-50 border border-slate-200 shadow-2xs transition-all flex items-center gap-1.5"
          >
            <FolderPlus className="w-3.5 h-3.5 text-slate-500" />
            <span>+ Group</span>
          </button>

          <button
            onClick={onOpenNewCategory}
            className="px-3 py-2 rounded-xl text-xs font-bold text-slate-700 bg-white hover:bg-slate-50 border border-slate-200 shadow-2xs transition-all flex items-center gap-1.5"
          >
            <Tag className="w-3.5 h-3.5 text-slate-500" />
            <span>+ Category</span>
          </button>

          <button
            onClick={onOpenMoveMoney}
            className="px-3 py-2 rounded-xl text-xs font-bold text-slate-700 bg-white hover:bg-slate-50 border border-slate-200 shadow-2xs transition-all flex items-center gap-1.5"
          >
            <ArrowRightLeft className="w-3.5 h-3.5 text-emerald-600" />
            <span>Move Money</span>
          </button>

          <button
            onClick={onOpenAutoAssign}
            className="px-3 py-2 rounded-xl text-xs font-bold text-blue-700 bg-blue-50 hover:bg-blue-100 border border-blue-200 shadow-2xs transition-all flex items-center gap-1.5"
          >
            <Sparkles className="w-3.5 h-3.5 text-blue-600" />
            <span>Auto Assign</span>
          </button>

          <button
            onClick={onCopyPreviousMonth}
            className="px-3 py-2 rounded-xl text-xs font-bold text-slate-700 bg-white hover:bg-slate-50 border border-slate-200 shadow-2xs transition-all flex items-center gap-1.5"
            title="Copy planned amounts from last month"
          >
            <Copy className="w-3.5 h-3.5 text-slate-500" />
            <span>Copy Prev</span>
          </button>

          <button
            onClick={onOpenRecentMoves}
            className="px-3 py-2 rounded-xl text-xs font-bold text-slate-700 bg-white hover:bg-slate-50 border border-slate-200 shadow-2xs transition-all flex items-center gap-1.5"
          >
            <Activity className="w-3.5 h-3.5 text-slate-500" />
            <span>Activity</span>
          </button>
        </div>
      </div>
    </div>
  );
}
