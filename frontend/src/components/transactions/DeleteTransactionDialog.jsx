import React from 'react';
import { AlertTriangle, Trash2, X } from 'lucide-react';

export default function DeleteTransactionDialog({
  isOpen,
  onClose,
  onConfirm,
  transaction,
  loading = false,
}) {
  if (!isOpen || !transaction) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
      <div
        className="relative w-full max-w-md bg-white rounded-3xl shadow-2xl border border-slate-100 p-6 space-y-5 animate-in fade-in zoom-in-95 duration-200"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-start justify-between">
          <div className="w-12 h-12 rounded-2xl bg-rose-50 border border-rose-100 flex items-center justify-center text-rose-600 shrink-0">
            <AlertTriangle className="w-6 h-6" />
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-slate-600 rounded-xl hover:bg-slate-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div>
          <h3 className="font-display font-bold text-lg text-slate-900">
            Delete Transaction?
          </h3>
          <p className="text-xs text-slate-500 mt-1 leading-relaxed">
            Are you sure you want to delete this transaction? This action will restore your account balance and update your budget activity accordingly.
          </p>
        </div>

        {/* Transaction mini summary */}
        <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 space-y-1.5 text-xs">
          <div className="flex justify-between font-medium text-slate-600">
            <span>Payee:</span>
            <span className="font-bold text-slate-900">{transaction.merchantName || transaction.merchant || 'Uncategorized'}</span>
          </div>
          <div className="flex justify-between font-medium text-slate-600">
            <span>Date:</span>
            <span className="font-bold text-slate-900">{transaction.transactionDate}</span>
          </div>
          <div className="flex justify-between font-medium text-slate-600">
            <span>Amount:</span>
            <span
              className={`font-bold ${
                transaction.type === 'INCOME' ? 'text-emerald-600' : 'text-rose-600'
              }`}
            >
              {transaction.type === 'INCOME' ? '+' : '-'}₹
              {parseFloat(transaction.amount || 0).toLocaleString()}
            </span>
          </div>
        </div>

        <div className="flex items-center justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="px-5 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-600 hover:bg-slate-50 transition-all"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={loading}
            className="px-5 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-700 active:scale-98 text-xs font-bold text-white shadow-md shadow-rose-500/20 transition-all flex items-center gap-2 disabled:opacity-50"
          >
            <Trash2 className="w-4 h-4" />
            <span>{loading ? 'Deleting...' : 'Delete Transaction'}</span>
          </button>
        </div>
      </div>
    </div>
  );
}
