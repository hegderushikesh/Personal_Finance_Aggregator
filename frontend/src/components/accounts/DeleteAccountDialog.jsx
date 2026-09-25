import React from 'react';
import { AlertTriangle, Trash2, X, EyeOff } from 'lucide-react';

export default function DeleteAccountDialog({
  isOpen,
  onClose,
  onConfirmDelete,
  onDeactivate,
  account,
  errorMessage = '',
  loading = false,
}) {
  if (!isOpen || !account) return null;

  const hasConflict = errorMessage.toLowerCase().includes('transaction');

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
            {hasConflict ? 'Cannot Delete Account' : 'Delete Account?'}
          </h3>
          <p className="text-xs text-slate-500 mt-1.5 leading-relaxed">
            {hasConflict ? (
              'This account cannot be deleted because it contains transactions. You can deactivate it instead to hide it while preserving your transaction history.'
            ) : (
              'This account will be permanently removed if it has no transactions. Are you sure you want to proceed?'
            )}
          </p>
        </div>

        {/* Account Info Box */}
        <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 space-y-1.5 text-xs">
          <div className="flex justify-between text-slate-600">
            <span>Account:</span>
            <span className="font-bold text-slate-900">{account.name}</span>
          </div>
          <div className="flex justify-between text-slate-600">
            <span>Institution:</span>
            <span className="font-bold text-slate-900">{account.institutionName || 'Self-Managed'}</span>
          </div>
          <div className="flex justify-between text-slate-600">
            <span>Balance:</span>
            <span className="font-bold text-slate-900">
              ₹{parseFloat(account.balance || 0).toLocaleString()}
            </span>
          </div>
        </div>

        {errorMessage && !hasConflict && (
          <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs font-semibold rounded-xl">
            {errorMessage}
          </div>
        )}

        <div className="flex items-center justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={onClose}
            disabled={loading}
            className="px-5 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-600 hover:bg-slate-50 transition-all"
          >
            Cancel
          </button>

          {hasConflict ? (
            <button
              type="button"
              onClick={() => onDeactivate(account)}
              disabled={loading}
              className="px-5 py-2.5 rounded-xl bg-amber-600 hover:bg-amber-700 active:scale-98 text-xs font-bold text-white shadow-md shadow-amber-500/20 transition-all flex items-center gap-2 disabled:opacity-50"
            >
              <EyeOff className="w-4 h-4" />
              <span>{loading ? 'Deactivating...' : 'Deactivate Account'}</span>
            </button>
          ) : (
            <button
              type="button"
              onClick={() => onConfirmDelete(account)}
              disabled={loading}
              className="px-5 py-2.5 rounded-xl bg-rose-600 hover:bg-rose-700 active:scale-98 text-xs font-bold text-white shadow-md shadow-rose-500/20 transition-all flex items-center gap-2 disabled:opacity-50"
            >
              <Trash2 className="w-4 h-4" />
              <span>{loading ? 'Deleting...' : 'Delete Account'}</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
