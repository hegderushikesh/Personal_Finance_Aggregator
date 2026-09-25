import React, { useState } from 'react';
import { X, Sparkles, CheckCircle2, AlertCircle } from 'lucide-react';
import { budgetService } from '../../services/budgetService';

export default function AutoAssignModal({ isOpen, onClose, budgetId, onApply }) {
  const [mode, setMode] = useState('UNDERFUNDED');
  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleModeChange = async (newMode) => {
    setMode(newMode);
    setLoading(true);
    try {
      const res = await budgetService.autoAssignPreview(budgetId, newMode);
      setPreview(res);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleApply = async () => {
    setLoading(true);
    try {
      await onApply(mode);
      onClose();
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 backdrop-blur-xs p-4">
      <div className="bg-white rounded-2xl shadow-xl border border-slate-200 w-full max-w-lg overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="p-5 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-lg bg-amber-100 text-amber-600 flex items-center justify-center font-bold">
              <Sparkles className="w-4 h-4" />
            </div>
            <h3 className="font-display font-bold text-base text-slate-800">FinPilot Auto-Assign</h3>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 p-1 rounded-lg hover:bg-slate-100">
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-4">
          <p className="text-xs text-slate-600">
            Select an automated allocation strategy. You will see a live preview before applying any changes.
          </p>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            {[
              { id: 'UNDERFUNDED', title: 'Fund Underfunded', desc: 'Fund categories with targets or required balances' },
              { id: 'SAME_AS_LAST_MONTH', title: 'Same as Last Month', desc: 'Copy previous month’s assigned amounts' },
              { id: 'LAST_MONTH_SPENDING', title: 'Based on Spending', desc: 'Assign amounts equal to last month activity' },
              { id: 'REMAINING_TO_SAVINGS', title: 'Remaining to Savings', desc: 'Assign all leftover Ready to Assign to Savings' },
            ].map((opt) => (
              <button
                key={opt.id}
                type="button"
                onClick={() => handleModeChange(opt.id)}
                className={`p-3 rounded-xl border text-left transition-all ${
                  mode === opt.id
                    ? 'border-blue-500 bg-blue-50/50 ring-1 ring-blue-500/30'
                    : 'border-slate-200 hover:border-slate-300 bg-white'
                }`}
              >
                <span className="block text-xs font-bold text-slate-800">{opt.title}</span>
                <span className="block text-[10px] text-slate-500 mt-0.5">{opt.desc}</span>
              </button>
            ))}
          </div>

          {/* Preview Box */}
          <div className="bg-slate-50 p-4 rounded-xl border border-slate-200/80 space-y-3">
            <div className="flex items-center justify-between text-xs font-bold text-slate-700">
              <span>Auto Assign Preview</span>
              {loading && <span className="text-[10px] text-blue-600 animate-pulse">Calculating preview...</span>}
            </div>

            {preview && preview.items && preview.items.length > 0 ? (
              <div className="space-y-1.5 max-h-40 overflow-y-auto pr-1">
                {preview.items.map((item) => (
                  <div key={item.categoryId} className="flex items-center justify-between text-xs">
                    <span className="text-slate-600 font-medium truncate max-w-[200px]">{item.categoryName}</span>
                    <span className="font-bold text-emerald-600">+₹{item.additionalAmount?.toLocaleString()}</span>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-xs text-slate-500 italic py-2 text-center">
                {!loading && 'No additional allocations required for this strategy.'}
              </div>
            )}

            {preview && (
              <div className="pt-2 border-t border-slate-200 flex justify-between text-xs font-bold">
                <span className="text-slate-600">Total Required:</span>
                <span className="text-slate-900">₹{(preview.totalRequired || 0).toLocaleString()}</span>
              </div>
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
              type="button"
              onClick={handleApply}
              disabled={loading || !preview || preview.items?.length === 0}
              className="px-5 py-2.5 text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 disabled:opacity-50 rounded-xl shadow-xs transition-all flex items-center gap-1.5"
            >
              <CheckCircle2 className="w-4 h-4" />
              <span>Apply Auto-Assign</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
