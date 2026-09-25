import React, { useState, useEffect } from 'react';
import { X, DollarSign, Wallet } from 'lucide-react';

export default function StartingBalanceModal({ isOpen, onClose, currentBalance, onSave }) {
  const [balance, setBalance] = useState('');

  useEffect(() => {
    if (currentBalance !== undefined && currentBalance !== null) {
      setBalance(currentBalance.toString());
    }
  }, [currentBalance, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    const val = parseFloat(balance);
    if (isNaN(val) || val < 0) {
      alert('Please enter a valid starting balance');
      return;
    }
    onSave(val);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
      <div className="bg-white rounded-2xl shadow-xl border border-slate-200 w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="p-5 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center font-bold">
              <Wallet className="w-4 h-4" />
            </div>
            <h3 className="font-display font-bold text-base text-slate-800">Set Starting Available Money</h3>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 p-1 rounded-lg hover:bg-slate-100">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">
              Starting Cash / Income Available
            </label>
            <div className="relative">
              <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 font-bold text-sm">₹</span>
              <input
                type="number"
                step="0.01"
                min="0"
                value={balance}
                onChange={(e) => setBalance(e.target.value)}
                placeholder="48800"
                className="w-full h-11 pl-8 pr-4 rounded-xl border border-slate-200 text-slate-800 text-base font-bold focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all"
                autoFocus
              />
            </div>
            <p className="text-[11px] text-slate-500 mt-1.5">
              This amount is your starting unassigned fund for the month ("Ready to Assign").
            </p>
          </div>

          <div className="flex justify-end gap-2.5 pt-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition-all"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-5 py-2.5 text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 rounded-xl shadow-xs transition-all"
            >
              Save Balance
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
