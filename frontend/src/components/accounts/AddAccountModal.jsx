import React, { useState } from 'react';
import { X, Landmark, CreditCard, Wallet, PiggyBank } from 'lucide-react';

const ACCOUNT_TYPES = [
  { value: 'CHECKING', label: 'Checking Account', desc: 'Everyday bank spending account', icon: Landmark },
  { value: 'SAVINGS', label: 'Savings Account', desc: 'Emergency fund or interest-bearing', icon: PiggyBank },
  { value: 'CREDIT_CARD', label: 'Credit Card', desc: 'Liability / borrowed credit limit', icon: CreditCard },
  { value: 'CASH', label: 'Cash Wallet', desc: 'Physical cash or digital pocket money', icon: Wallet },
];

export default function AddAccountModal({ isOpen, onClose, onSave, loading = false }) {
  const [name, setName] = useState('');
  const [type, setType] = useState('CHECKING');
  const [balance, setBalance] = useState('');
  const [institutionName, setInstitutionName] = useState('');
  const [accountNumberLast4, setAccountNumberLast4] = useState('');
  const [currency] = useState('INR');
  const [errorMessage, setErrorMessage] = useState('');

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    setErrorMessage('');

    if (!name.trim()) {
      setErrorMessage('Please enter an account name.');
      return;
    }

    const parsedBalance = parseFloat(balance);
    if (isNaN(parsedBalance) || parsedBalance < 0) {
      setErrorMessage('Initial balance must be 0 or a positive amount.');
      return;
    }

    if (accountNumberLast4.trim() && !/^\d{4}$/.test(accountNumberLast4.trim())) {
      setErrorMessage('Account number last 4 digits must be exactly 4 numbers (e.g. 4821).');
      return;
    }

    onSave({
      name: name.trim(),
      type,
      balance: parsedBalance,
      institutionName: institutionName.trim() || null,
      accountNumberLast4: accountNumberLast4.trim() || null,
      currency,
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
            <h3 className="font-display font-bold text-lg text-slate-900">Add New Account</h3>
            <p className="text-xs text-slate-500 mt-0.5">
              Create a manual cash, savings, checking, or credit card account
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

          {/* Account Type Selector */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-slate-700">Account Type</label>
            <div className="grid grid-cols-2 gap-2">
              {ACCOUNT_TYPES.map((t) => {
                const Icon = t.icon;
                const isSelected = type === t.value;
                return (
                  <button
                    key={t.value}
                    type="button"
                    onClick={() => setType(t.value)}
                    className={`p-3 rounded-xl border text-left flex items-start gap-2.5 transition-all ${
                      isSelected
                        ? 'border-blue-600 bg-blue-50/50 ring-1 ring-blue-600 text-blue-950'
                        : 'border-slate-200 hover:bg-slate-50 text-slate-700'
                    }`}
                  >
                    <Icon className={`w-4 h-4 mt-0.5 shrink-0 ${isSelected ? 'text-blue-600' : 'text-slate-400'}`} />
                    <div>
                      <div className="text-xs font-bold">{t.label}</div>
                      <div className="text-[10px] text-slate-400 line-clamp-1">{t.desc}</div>
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Account Name */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-slate-700">Account Name</label>
            <input
              type="text"
              placeholder="e.g. HDFC Salary, Emergency Savings, Amex Gold"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              maxLength={100}
              className="w-full h-11 px-3.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-800 focus:border-blue-600 outline-none transition-all placeholder:text-slate-400"
              autoFocus
            />
          </div>

          {/* Initial Balance */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-slate-700">
              {type === 'CREDIT_CARD' ? 'Current Balance Owed (Liability)' : 'Initial Balance'}
            </label>
            <div className="relative">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 font-bold text-lg">
                ₹
              </span>
              <input
                type="number"
                step="0.01"
                min="0"
                placeholder="0.00"
                value={balance}
                onChange={(e) => setBalance(e.target.value)}
                required
                className="w-full h-12 pl-9 pr-4 rounded-xl border border-slate-200 text-lg font-black text-slate-900 focus:border-blue-600 outline-none transition-all placeholder:text-slate-300"
              />
            </div>
            <span className="text-[10px] text-slate-400">
              {type === 'CREDIT_CARD'
                ? 'Enter your current credit card statement or outstanding balance.'
                : 'Enter your starting cash balance in this account.'}
            </span>
          </div>

          {/* Institution & Last 4 digits */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-slate-700">Financial Institution</label>
              <input
                type="text"
                placeholder="e.g. HDFC Bank, SBI, ICICI"
                value={institutionName}
                onChange={(e) => setInstitutionName(e.target.value)}
                maxLength={100}
                className="w-full h-11 px-3.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-800 focus:border-blue-600 outline-none transition-all placeholder:text-slate-400"
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-slate-700">Account Last 4 Digits</label>
              <input
                type="text"
                placeholder="e.g. 4821"
                value={accountNumberLast4}
                onChange={(e) => setAccountNumberLast4(e.target.value.replace(/\D/g, '').slice(0, 4))}
                maxLength={4}
                className="w-full h-11 px-3.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-800 focus:border-blue-600 outline-none transition-all placeholder:text-slate-400"
              />
            </div>
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
              {loading ? 'Creating...' : 'Create Account'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
