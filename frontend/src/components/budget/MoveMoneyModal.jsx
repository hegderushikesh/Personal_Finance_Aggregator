import React, { useState } from 'react';
import { X, ArrowRightLeft } from 'lucide-react';

export default function MoveMoneyModal({ isOpen, onClose, categories = [], onMove }) {
  const [fromCategoryId, setFromCategoryId] = useState('');
  const [toCategoryId, setToCategoryId] = useState('');
  const [amount, setAmount] = useState('');

  if (!isOpen) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!fromCategoryId || !toCategoryId) {
      alert('Please select both source and destination categories');
      return;
    }
    if (fromCategoryId === toCategoryId) {
      alert('Source and destination categories must be different');
      return;
    }
    const val = parseFloat(amount);
    if (isNaN(val) || val <= 0) {
      alert('Please enter a valid amount greater than 0');
      return;
    }
    onMove(Number(fromCategoryId), Number(toCategoryId), val);
    onClose();
  };

  const selectedFrom = categories.find((c) => c.id === Number(fromCategoryId));

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
      <div className="bg-white rounded-2xl shadow-xl border border-slate-200 w-full max-w-md overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="p-5 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-emerald-100 text-emerald-600 flex items-center justify-center font-bold">
              <ArrowRightLeft className="w-4 h-4" />
            </div>
            <h3 className="font-display font-bold text-base text-slate-800">Move Money Between Categories</h3>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 p-1 rounded-lg hover:bg-slate-100">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">From Category</label>
            <select
              value={fromCategoryId}
              onChange={(e) => setFromCategoryId(e.target.value)}
              className="w-full h-10 px-3 rounded-xl border border-slate-200 text-xs font-semibold text-slate-800 bg-white focus:border-blue-500 outline-none"
            >
              <option value="">Select source category...</option>
              {categories.map((cat) => (
                <option key={cat.id} value={cat.id}>
                  {cat.categoryGroupName} → {cat.name} (Available: ₹{(cat.available || 0).toLocaleString()})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">To Category</label>
            <select
              value={toCategoryId}
              onChange={(e) => setToCategoryId(e.target.value)}
              className="w-full h-10 px-3 rounded-xl border border-slate-200 text-xs font-semibold text-slate-800 bg-white focus:border-blue-500 outline-none"
            >
              <option value="">Select destination category...</option>
              {categories
                .filter((c) => c.id !== Number(fromCategoryId))
                .map((cat) => (
                  <option key={cat.id} value={cat.id}>
                    {cat.categoryGroupName} → {cat.name} (Available: ₹{(cat.available || 0).toLocaleString()})
                  </option>
                ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Amount to Move</label>
            <div className="relative">
              <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 font-bold text-sm">₹</span>
              <input
                type="number"
                step="0.01"
                min="1"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="2000"
                className="w-full h-11 pl-8 pr-4 rounded-xl border border-slate-200 text-slate-800 text-base font-bold focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all"
              />
            </div>
            {selectedFrom && (
              <p className="text-[11px] text-slate-500 mt-1">
                Max available in {selectedFrom.name}: ₹{(selectedFrom.available || 0).toLocaleString()}
              </p>
            )}
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
              className="px-5 py-2.5 text-xs font-bold text-white bg-emerald-600 hover:bg-emerald-700 rounded-xl shadow-xs transition-all"
            >
              Move Money
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
