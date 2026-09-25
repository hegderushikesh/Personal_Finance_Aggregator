import React, { useState, useEffect } from 'react';
import { X, Target as TargetIcon, Calendar } from 'lucide-react';

export default function TargetModal({ isOpen, onClose, category, currentTarget, onSave }) {
  const [type, setType] = useState('MONTHLY_AMOUNT');
  const [frequency, setFrequency] = useState('MONTHLY');
  const [amount, setAmount] = useState('');
  const [targetDate, setTargetDate] = useState('');
  const [monthlyAmount, setMonthlyAmount] = useState('');

  useEffect(() => {
    if (currentTarget) {
      setType(currentTarget.type || 'MONTHLY_AMOUNT');
      setFrequency(currentTarget.frequency || 'MONTHLY');
      setAmount(currentTarget.amount ? currentTarget.amount.toString() : '');
      setTargetDate(currentTarget.targetDate || '');
      setMonthlyAmount(currentTarget.monthlyAmount ? currentTarget.monthlyAmount.toString() : '');
    } else {
      setType('MONTHLY_AMOUNT');
      setFrequency('MONTHLY');
      setAmount('');
      setTargetDate('');
      setMonthlyAmount('');
    }
  }, [currentTarget, isOpen]);

  if (!isOpen || !category) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      alert('Please enter a valid target amount');
      return;
    }

    const payload = {
      categoryId: category.categoryId || category.id,
      type,
      frequency,
      amount: parsedAmount,
      targetDate: type === 'TARGET_BY_DATE' ? targetDate : null,
      monthlyAmount: monthlyAmount ? parseFloat(monthlyAmount) : parsedAmount,
    };

    onSave(payload);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
      <div className="bg-white rounded-2xl shadow-xl border border-slate-200 w-full max-w-lg overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="p-5 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-indigo-100 text-indigo-600 flex items-center justify-center font-bold">
              <TargetIcon className="w-4 h-4" />
            </div>
            <div>
              <h3 className="font-display font-bold text-base text-slate-800">Set Category Target</h3>
              <p className="text-[11px] font-medium text-slate-500">For {category.categoryName || category.name}</p>
            </div>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 p-1 rounded-lg hover:bg-slate-100">
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {/* Target Frequency / Cadence Tabs */}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5">Target Cadence</label>
            <div className="grid grid-cols-4 gap-1 p-1 bg-slate-100 rounded-xl">
              {['WEEKLY', 'MONTHLY', 'YEARLY', 'CUSTOM'].map((freq) => (
                <button
                  key={freq}
                  type="button"
                  onClick={() => setFrequency(freq)}
                  className={`py-1.5 text-xs font-semibold rounded-lg transition-all ${
                    frequency === freq
                      ? 'bg-white text-slate-800 shadow-xs'
                      : 'text-slate-500 hover:text-slate-700'
                  }`}
                >
                  {freq.charAt(0) + freq.slice(1).toLowerCase()}
                </button>
              ))}
            </div>
          </div>

          {/* Target Strategy Type */}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1.5">Target Strategy</label>
            <div className="space-y-2">
              <label className="flex items-start gap-3 p-3 rounded-xl border border-slate-200 hover:border-slate-300 cursor-pointer transition-all">
                <input
                  type="radio"
                  name="targetType"
                  value="MONTHLY_AMOUNT"
                  checked={type === 'MONTHLY_AMOUNT'}
                  onChange={() => setType('MONTHLY_AMOUNT')}
                  className="mt-0.5 text-blue-600 focus:ring-blue-500"
                />
                <div>
                  <span className="block text-xs font-bold text-slate-800">Set Aside Amount Periodically</span>
                  <span className="block text-[11px] text-slate-500">
                    Need a fixed amount (e.g. ₹10,000) assigned every month.
                  </span>
                </div>
              </label>

              <label className="flex items-start gap-3 p-3 rounded-xl border border-slate-200 hover:border-slate-300 cursor-pointer transition-all">
                <input
                  type="radio"
                  name="targetType"
                  value="TARGET_BALANCE"
                  checked={type === 'TARGET_BALANCE'}
                  onChange={() => setType('TARGET_BALANCE')}
                  className="mt-0.5 text-blue-600 focus:ring-blue-500"
                />
                <div>
                  <span className="block text-xs font-bold text-slate-800">Target Available Balance</span>
                  <span className="block text-[11px] text-slate-500">
                    Reach and maintain a balance (e.g. Emergency Fund ₹100,000).
                  </span>
                </div>
              </label>

              <label className="flex items-start gap-3 p-3 rounded-xl border border-slate-200 hover:border-slate-300 cursor-pointer transition-all">
                <input
                  type="radio"
                  name="targetType"
                  value="TARGET_BY_DATE"
                  checked={type === 'TARGET_BY_DATE'}
                  onChange={() => setType('TARGET_BY_DATE')}
                  className="mt-0.5 text-blue-600 focus:ring-blue-500"
                />
                <div>
                  <span className="block text-xs font-bold text-slate-800">Target Balance By Date</span>
                  <span className="block text-[11px] text-slate-500">
                    Save a specific total amount by a future deadline (e.g. Vacation ₹60,000 by June 2027).
                  </span>
                </div>
              </label>
            </div>
          </div>

          {/* Amount Field */}
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">I Need Amount (₹)</label>
            <div className="relative">
              <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400 font-bold text-sm">₹</span>
              <input
                type="number"
                step="0.01"
                min="1"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="10000"
                className="w-full h-11 pl-8 pr-4 rounded-xl border border-slate-200 text-slate-800 text-base font-bold focus:border-blue-500 outline-none"
                required
              />
            </div>
          </div>

          {/* Target Date (if TARGET_BY_DATE) */}
          {type === 'TARGET_BY_DATE' && (
            <div>
              <label className="block text-xs font-semibold text-slate-600 mb-1">Target Date</label>
              <input
                type="date"
                value={targetDate}
                onChange={(e) => setTargetDate(e.target.value)}
                className="w-full h-10 px-3 rounded-xl border border-slate-200 text-xs font-semibold text-slate-800 bg-white focus:border-blue-500 outline-none"
                required
              />
            </div>
          )}

          <div className="flex justify-end gap-2.5 pt-3 border-t border-slate-100">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 text-xs font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition-all"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-5 py-2.5 text-xs font-bold text-white bg-indigo-600 hover:bg-indigo-700 rounded-xl shadow-xs transition-all"
            >
              Save Target
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
