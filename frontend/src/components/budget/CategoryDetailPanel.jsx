import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  X,
  Target as TargetIcon,
  ArrowRightLeft,
  CheckCircle2,
  AlertTriangle,
  Clock,
  Edit2,
  Trash2,
  BellOff,
  Save,
  Receipt,
  ExternalLink,
} from 'lucide-react';
import { budgetService } from '../../services/budgetService';
import { targetService } from '../../services/targetService';

export default function CategoryDetailPanel({
  category,
  year,
  month,
  onClose,
  onOpenTargetModal,
  onOpenMoveMoneyModal,
  onAssignMoney,
  onRefresh,
}) {
  const navigate = useNavigate();
  const [details, setDetails] = useState(null);
  const [loading, setLoading] = useState(false);
  const [quickAssignVal, setQuickAssignVal] = useState('');
  const [noteText, setNoteText] = useState('');

  useEffect(() => {
    if (!category) return;
    setLoading(true);
    budgetService
      .getCategoryDetails(category.categoryId, year, month)
      .then((data) => {
        setDetails(data);
        setQuickAssignVal(data.assignedThisMonth ? data.assignedThisMonth.toString() : '0');
        setNoteText(data.note || '');
      })
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [category, year, month]);

  if (!category) return null;

  const handleQuickAssignSubmit = (e) => {
    e.preventDefault();
    const val = parseFloat(quickAssignVal);
    if (!isNaN(val) && val >= 0) {
      onAssignMoney(category.categoryId, val);
    }
  };

  const handleDeleteTarget = async () => {
    if (!details?.target?.id) return;
    if (confirm('Are you sure you want to remove this target?')) {
      await targetService.deleteTarget(details.target.id);
      onRefresh();
    }
  };

  const handleSnoozeToggle = async () => {
    if (!details?.target?.id) return;
    if (details.target.snoozed) {
      await targetService.unsnoozeTarget(details.target.id);
    } else {
      await targetService.snoozeTarget(details.target.id);
    }
    onRefresh();
  };

  const target = details?.target;
  const available = details?.available || 0;

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 shadow-md p-6 space-y-6 animate-in slide-in-from-right duration-200">
      {/* Panel Header */}
      <div className="flex items-center justify-between border-b border-slate-100 pb-4">
        <div>
          <span className="text-[10px] font-bold uppercase tracking-wider text-slate-400">
            {details?.categoryGroupName || category.categoryGroupName}
          </span>
          <h3 className="font-display font-bold text-xl text-slate-800 flex items-center gap-2">
            {category.categoryName}
          </h3>
        </div>
        <button onClick={onClose} className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg hover:bg-slate-100">
          <X className="w-5 h-5" />
        </button>
      </div>

      {/* Available Hero Box */}
      <div className="bg-slate-900 text-white p-5 rounded-2xl shadow-sm text-center space-y-1">
        <span className="text-[11px] font-semibold text-slate-400 uppercase tracking-widest">
          Available Balance
        </span>
        <div className="font-display font-black text-3xl text-emerald-400">
          ₹{available.toLocaleString()}
        </div>
      </div>

      {/* Detailed Financial Breakdown */}
      <div className="bg-slate-50 p-4 rounded-xl border border-slate-200/70 space-y-2 text-xs font-semibold">
        <div className="flex justify-between text-slate-500">
          <span>Cash Left From Last Month</span>
          <span className="font-bold text-slate-700">₹{(details?.cashLeftFromLastMonth || 0).toLocaleString()}</span>
        </div>
        <div className="flex justify-between text-slate-500">
          <span>Assigned This Month</span>
          <span className="font-bold text-emerald-600">+₹{(details?.assignedThisMonth || 0).toLocaleString()}</span>
        </div>
        <div className="flex justify-between text-slate-500">
          <span>Activity</span>
          <span className="font-bold text-rose-600">
            {details?.activity > 0 ? `-₹${details.activity.toLocaleString()}` : '₹0'}
          </span>
        </div>
        <div className="pt-2 border-t border-slate-200 flex justify-between text-slate-900 font-bold text-sm">
          <span>Available Total</span>
          <span>₹{available.toLocaleString()}</span>
        </div>
      </div>

      {/* Assign Money Quick Form */}
      <form onSubmit={handleQuickAssignSubmit} className="space-y-2">
        <label className="block text-xs font-semibold text-slate-700">Set Monthly Assigned Amount</label>
        <div className="flex gap-2">
          <div className="relative flex-1">
            <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 font-bold text-xs">₹</span>
            <input
              type="number"
              step="0.01"
              value={quickAssignVal}
              onChange={(e) => setQuickAssignVal(e.target.value)}
              className="w-full h-9 pl-7 pr-3 rounded-xl border border-slate-200 text-xs font-bold text-slate-800 focus:border-blue-500 outline-none"
            />
          </div>
          <button
            type="submit"
            className="px-4 py-2 text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 rounded-xl transition-all"
          >
            Assign
          </button>
        </div>
      </form>

      {/* Target Section */}
      <div className="space-y-3 pt-2">
        <div className="flex items-center justify-between">
          <h4 className="font-display font-bold text-sm text-slate-800 flex items-center gap-1.5">
            <TargetIcon className="w-4 h-4 text-indigo-600" />
            <span>Target Goal</span>
          </h4>
          {target && (
            <div className="flex items-center gap-1">
              <button
                onClick={() => onOpenTargetModal(category, target)}
                className="p-1 text-slate-400 hover:text-slate-600"
                title="Edit Target"
              >
                <Edit2 className="w-3.5 h-3.5" />
              </button>
              <button
                onClick={handleSnoozeToggle}
                className={`p-1 ${target.snoozed ? 'text-amber-600' : 'text-slate-400 hover:text-slate-600'}`}
                title={target.snoozed ? 'Unsnooze' : 'Snooze Target'}
              >
                <BellOff className="w-3.5 h-3.5" />
              </button>
              <button
                onClick={handleDeleteTarget}
                className="p-1 text-slate-400 hover:text-rose-600"
                title="Delete Target"
              >
                <Trash2 className="w-3.5 h-3.5" />
              </button>
            </div>
          )}
        </div>

        {target ? (
          <div className="p-4 rounded-xl border border-indigo-100 bg-indigo-50/40 space-y-3">
            <div className="flex items-center justify-between text-xs font-bold text-slate-800">
              <span>Monthly Target</span>
              <span>₹{(target.amount || 0).toLocaleString()}</span>
            </div>

            <div className="space-y-1">
              <div className="flex justify-between text-[11px] text-slate-500 font-medium">
                <span>Progress ({target.progressPercentage || 0}%)</span>
                <span>Remaining: ₹{(target.remainingAmount || 0).toLocaleString()}</span>
              </div>
              <div className="w-full h-2 bg-indigo-100 rounded-full overflow-hidden">
                <div
                  className="h-full bg-indigo-600 rounded-full transition-all duration-300"
                  style={{ width: `${Math.min(100, target.progressPercentage || 0)}%` }}
                />
              </div>
            </div>

            {target.status === 'COMPLETED' ? (
              <div className="flex items-center gap-1.5 text-xs font-bold text-emerald-700 bg-emerald-100/80 p-2 rounded-lg">
                <CheckCircle2 className="w-4 h-4" />
                <span>Target Met!</span>
              </div>
            ) : target.status === 'SNOOZED' ? (
              <div className="flex items-center gap-1.5 text-xs font-bold text-amber-700 bg-amber-100/80 p-2 rounded-lg">
                <Clock className="w-4 h-4" />
                <span>Target Snoozed</span>
              </div>
            ) : null}
          </div>
        ) : (
          <button
            onClick={() => onOpenTargetModal(category, null)}
            className="w-full py-2.5 rounded-xl border border-dashed border-slate-300 text-xs font-bold text-slate-600 hover:text-indigo-600 hover:border-indigo-300 hover:bg-indigo-50/30 transition-all flex items-center justify-center gap-2"
          >
            <TargetIcon className="w-4 h-4 text-indigo-500" />
            <span>Set Target Goal</span>
          </button>
        )}
      </div>

      {/* Recent Activity / Transactions */}
      <div className="space-y-3 pt-2">
        <div className="flex items-center justify-between">
          <h4 className="font-display font-bold text-sm text-slate-800 flex items-center gap-1.5">
            <Receipt className="w-4 h-4 text-slate-500" />
            <span>Recent Activity</span>
          </h4>
          <button
            onClick={() => {
              const query = new URLSearchParams();
              query.set('categoryId', category.categoryId);
              if (month) query.set('month', month);
              if (year) query.set('year', year);
              navigate(`/transactions?${query.toString()}`);
            }}
            className="text-[11px] font-bold text-blue-600 hover:text-blue-700 flex items-center gap-1 hover:underline"
          >
            <span>View all</span>
            <ExternalLink className="w-3 h-3" />
          </button>
        </div>

        {details?.recentTransactions && details.recentTransactions.length > 0 ? (
          <div className="space-y-2">
            {details.recentTransactions.map((tx) => (
              <div
                key={tx.id}
                className="p-3 bg-slate-50 rounded-xl border border-slate-100 flex items-center justify-between text-xs"
              >
                <div className="min-w-0 pr-2">
                  <div className="font-bold text-slate-800 truncate">
                    {tx.merchantName || tx.merchant || 'Payee'}
                  </div>
                  <div className="text-[10px] text-slate-400 mt-0.5">
                    {tx.transactionDate} • {tx.accountName || 'Account'}
                  </div>
                </div>
                <div className="text-right font-display font-bold text-slate-900 whitespace-nowrap">
                  -₹{parseFloat(tx.amount || 0).toLocaleString()}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-4 bg-slate-50/50 rounded-xl border border-dashed border-slate-200 text-center text-xs text-slate-400">
            No transactions in this category yet.
          </div>
        )}
      </div>

      {/* Move Money Trigger Button */}
      <button
        onClick={() => onOpenMoveMoneyModal(category)}
        className="w-full py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-700 hover:bg-slate-50 transition-all flex items-center justify-center gap-2 shadow-2xs"
      >
        <ArrowRightLeft className="w-4 h-4 text-emerald-600" />
        <span>Move Money From/To Category</span>
      </button>
    </div>
  );
}
