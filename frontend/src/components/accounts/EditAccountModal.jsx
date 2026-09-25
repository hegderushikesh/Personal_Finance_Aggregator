import React, { useState, useEffect } from 'react';
import { X, Landmark, CreditCard, Wallet, PiggyBank, Info } from 'lucide-react';

const ACCOUNT_TYPES = [
  { value: 'CHECKING', label: 'Checking Account', icon: Landmark },
  { value: 'SAVINGS', label: 'Savings Account', icon: PiggyBank },
  { value: 'CREDIT_CARD', label: 'Credit Card', icon: CreditCard },
  { value: 'CASH', label: 'Cash Wallet', icon: Wallet },
];

export default function EditAccountModal({ isOpen, onClose, onSave, account, loading = false }) {
  const [name, setName] = useState('');
  const [type, setType] = useState('CHECKING');
  const [institutionName, setInstitutionName] = useState('');
  const [accountNumberLast4, setAccountNumberLast4] = useState('');
  const [isActive, setIsActive] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    if (account) {
      setName(account.name || '');
      setType(account.type || 'CHECKING');
      setInstitutionName(account.institutionName || '');
      setAccountNumberLast4(account.accountNumberLast4 || '');
      setIsActive(account.isActive ?? true);
      setErrorMessage('');
    }
  }, [account, isOpen]);

  if (!isOpen || !account) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    setErrorMessage('');

    if (!name.trim()) {
      setErrorMessage('Please enter an account name.');
      return;
    }

    if (accountNumberLast4.trim() && !/^\d{4}$/.test(accountNumberLast4.trim())) {
      setErrorMessage('Account number last 4 digits must be exactly 4 numbers.');
      return;
    }

    onSave({
      name: name.trim(),
      type,
      institutionName: institutionName.trim() || null,
      accountNumberLast4: accountNumberLast4.trim() || null,
      isActive,
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-xs">
      <div
        className="relative w-full max-w-lg bg-white rounded-3xl shadow-2xl border border-slate-100 overflow-hidden flex flex-col max-h-[90vh] animate-in fade-in zoom-in-95 duration-200"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="px-6 py-5 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div>
            <h3 className="font-display font-bold text-lg text-slate-900">Edit Account</h3>
            <p className="text-xs text-slate-500 mt-0.5">
              Update account details or toggle active status
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-slate-600 rounded-xl hover:bg-slate-100 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto p-6 space-y-5">
          {errorMessage && (
            <div className="p-3 bg-rose-50 border border-rose-200 text-rose-700 text-xs font-semibold rounded-xl flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-rose-500 shrink-0" />
              <span>{errorMessage}</span>
            </div>
          )}

          {/* Current Balance Notice */}
          <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100 flex items-center justify-between">
            <div>
              <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">
                Current Balance
              </span>
              <span className="font-display font-black text-xl text-slate-900">
                ₹{parseFloat(account.balance || 0).toLocaleString()}
              </span>
            </div>
            <div className="flex items-center gap-1.5 text-[11px] text-slate-400 max-w-[200px] text-right">
              <Info className="w-4 h-4 shrink-0 text-blue-500" />
              <span>Balance is maintained automatically by transactions</span>
            </div>
          </div>

          {/* Account Name */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-slate-700">Account Name</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              maxLength={100}
              className="w-full h-11 px-3.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-800 focus:border-blue-600 outline-none transition-all"
            />
          </div>

          {/* Account Type */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-slate-700">Account Type</label>
            <select
              value={type}
              onChange={(e) => setType(e.target.value)}
              className="w-full h-11 px-3.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-800 bg-white focus:border-blue-600 outline-none transition-all"
            >
              {ACCOUNT_TYPES.map((t) => (
                <option key={t.value} value={t.value}>
                  {t.label}
                </option>
              ))}
            </select>
          </div>

          {/* Institution & Last 4 digits */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-slate-700">Financial Institution</label>
              <input
                type="text"
                placeholder="e.g. HDFC Bank, SBI"
                value={institutionName}
                onChange={(e) => setInstitutionName(e.target.value)}
                maxLength={100}
                className="w-full h-11 px-3.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-800 focus:border-blue-600 outline-none transition-all"
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-slate-700">Last 4 Digits</label>
              <input
                type="text"
                placeholder="e.g. 4821"
                value={accountNumberLast4}
                onChange={(e) => setAccountNumberLast4(e.target.value.replace(/\D/g, '').slice(0, 4))}
                maxLength={4}
                className="w-full h-11 px-3.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-800 focus:border-blue-600 outline-none transition-all"
              />
            </div>
          </div>

          {/* Active / Inactive status toggle */}
          <div className="flex items-center gap-3 pt-2">
            <input
              type="checkbox"
              id="isActiveCheckbox"
              checked={isActive}
              onChange={(e) => setIsActive(e.target.checked)}
              className="w-4 h-4 rounded text-blue-600 border-slate-300 focus:ring-blue-500"
            />
            <label htmlFor="isActiveCheckbox" className="text-xs font-semibold text-slate-700 cursor-pointer select-none">
              Account is Active (uncheck to deactivate/archive this account)
            </label>
          </div>

          {/* Footer Actions */}
          <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-5 py-2.5 rounded-xl border border-slate-200 text-xs font-bold text-slate-600 hover:bg-slate-50 transition-all"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-6 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 active:scale-98 text-xs font-bold text-white shadow-md shadow-blue-500/20 transition-all disabled:opacity-50"
            >
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
