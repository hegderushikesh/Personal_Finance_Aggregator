import React from 'react';
import { useNavigate } from 'react-router-dom';
import { CreditCard, PiggyBank, Landmark, Wallet, Edit2, Trash2, ArrowRight } from 'lucide-react';

export default function AccountCard({ account, onEdit, onDelete }) {
  const navigate = useNavigate();

  const isCredit = account.type === 'CREDIT_CARD';
  const isSavings = account.type === 'SAVINGS';
  const balance = parseFloat(account.balance || 0);

  const getAccountIcon = () => {
    switch (account.type) {
      case 'CREDIT_CARD':
        return <CreditCard className="w-5 h-5 text-rose-600" />;
      case 'SAVINGS':
        return <PiggyBank className="w-5 h-5 text-indigo-600" />;
      case 'CASH':
        return <Wallet className="w-5 h-5 text-emerald-600" />;
      default:
        return <Landmark className="w-5 h-5 text-blue-600" />;
    }
  };

  const getBadgeColor = () => {
    if (!account.isActive) {
      return 'bg-slate-100 text-slate-500 border-slate-200';
    }
    switch (account.type) {
      case 'CREDIT_CARD':
        return 'bg-rose-50 text-rose-700 border-rose-200/60';
      case 'SAVINGS':
        return 'bg-indigo-50 text-indigo-700 border-indigo-200/60';
      case 'CASH':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200/60';
      default:
        return 'bg-blue-50 text-blue-700 border-blue-200/60';
    }
  };

  return (
    <div className={`p-5 rounded-2xl bg-white border transition-all duration-200 hover:shadow-md flex flex-col justify-between group ${
      account.isActive ? 'border-slate-200/80' : 'border-slate-200 bg-slate-50/50 opacity-75'
    }`}>
      {/* Top Details & Action Buttons */}
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-center gap-3 min-w-0">
          <div className="w-10 h-10 rounded-xl bg-slate-50 border border-slate-100 flex items-center justify-center shrink-0">
            {getAccountIcon()}
          </div>
          <div className="min-w-0">
            <h4 className="font-bold text-sm text-slate-900 truncate flex items-center gap-2">
              <span>{account.name}</span>
              {account.source === 'PLAID' ? (
                <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200/60 shrink-0">
                  Plaid
                </span>
              ) : (
                <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-slate-100 text-slate-500 border border-slate-200/60 shrink-0">
                  Manual
                </span>
              )}
              {!account.isActive && (
                <span className="text-[10px] font-semibold px-2 py-0.5 rounded-full bg-slate-200 text-slate-600 shrink-0">
                  Inactive
                </span>
              )}
            </h4>
            <p className="text-xs text-slate-400 mt-0.5 truncate">
              {account.institutionName || 'Self-Managed'}
              {account.accountNumberLast4 ? ` ••••${account.accountNumberLast4}` : ''}
            </p>
          </div>
        </div>

        {/* Action icons */}
        <div className="flex items-center gap-1 opacity-80 group-hover:opacity-100 transition-opacity shrink-0">
          <button
            onClick={() => onEdit(account)}
            className="p-1.5 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
            title="Edit Account"
          >
            <Edit2 className="w-3.5 h-3.5" />
          </button>
          <button
            onClick={() => onDelete(account)}
            className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
            title="Delete or Deactivate"
          >
            <Trash2 className="w-3.5 h-3.5" />
          </button>
        </div>
      </div>

      {/* Balance & Type Pill */}
      <div className="mt-6 pt-4 border-t border-slate-100 flex items-end justify-between">
        <div>
          <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">
            {isCredit ? 'Balance Owed' : 'Current Balance'}
          </span>
          <div className={`font-display font-black text-xl mt-0.5 ${
            isCredit ? 'text-rose-600' : 'text-slate-900'
          }`}>
            ₹{balance.toLocaleString()}
          </div>
        </div>

        <button
          onClick={() => navigate(`/transactions?accountId=${account.id}`)}
          className="text-[11px] font-bold text-slate-500 hover:text-blue-600 flex items-center gap-1 transition-colors cursor-pointer"
          title="View transactions for this account"
        >
          <span>Activity</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  );
}
