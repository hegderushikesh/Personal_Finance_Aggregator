import React, { useState, useEffect } from 'react';
import { X, ArrowDownRight, ArrowUpRight, Calendar, CreditCard, Tag, DollarSign, FileText, Sparkles, Loader2 } from 'lucide-react';
import { transactionService } from '../../services/transactionService';
import { accountService } from '../../services/accountService';
import { categoryService } from '../../services/categoryService';
import { useSuggestCategory } from '../../features/ai/hooks/useAi';

export default function TransactionModal({
  isOpen,
  onClose,
  onSuccess,
  transactionToEdit = null,
  initialCategoryId = null,
  defaultMonth = null,
  defaultYear = null,
}) {
  const [type, setType] = useState('EXPENSE');
  const [amount, setAmount] = useState('');
  const [transactionDate, setTransactionDate] = useState(
    new Date().toISOString().split('T')[0]
  );
  const [merchant, setMerchant] = useState('');
  const [accountId, setAccountId] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [description, setDescription] = useState('');
  const [isCleared, setIsCleared] = useState(true);

  const [accounts, setAccounts] = useState([]);
  const [categoryGroups, setCategoryGroups] = useState([]);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [aiSuggestion, setAiSuggestion] = useState(null);
  const suggestCategoryMutation = useSuggestCategory();

  const handleAskAiCategory = async () => {
    if (!merchant && !description) return;
    try {
      const res = await suggestCategoryMutation.mutateAsync({
        merchantName: merchant,
        description,
        amount: parseFloat(amount) || 0,
      });
      if (res?.suggestedCategory) {
        setAiSuggestion(res);
      }
    } catch (err) {
      console.warn('AI category suggestion unavailable', err);
    }
  };

  const handleApplyAiCategory = (categoryName) => {
    if (!categoryName) return;
    const cleanName = categoryName.trim().toLowerCase();
    for (const group of categoryGroups) {
      if (group.categories) {
        const found = group.categories.find(
          (c) => c.name.trim().toLowerCase() === cleanName || c.name.trim().toLowerCase().includes(cleanName)
        );
        if (found) {
          setCategoryId(found.id.toString());
          setAiSuggestion(null);
          return;
        }
      }
    }
    setAiSuggestion(null);
  };

  // Fetch accounts and category groups when modal opens
  useEffect(() => {
    if (!isOpen) return;

    const fetchData = async () => {
      setLoading(true);
      setErrorMessage('');
      try {
        const [fetchedAccounts, fetchedGroups] = await Promise.all([
          accountService.getAccounts(),
          categoryService.getCategoryGroups(),
        ]);
        setAccounts(fetchedAccounts || []);
        setCategoryGroups(fetchedGroups || []);

        // Default account if none selected
        if (fetchedAccounts && fetchedAccounts.length > 0 && !transactionToEdit && !accountId) {
          setAccountId(fetchedAccounts[0].id.toString());
        }
      } catch (err) {
        console.error('Failed to load accounts or categories', err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [isOpen]);

  // Populate form for edit mode or initial presets
  useEffect(() => {
    if (transactionToEdit) {
      setType(transactionToEdit.type || 'EXPENSE');
      setAmount(transactionToEdit.amount ? transactionToEdit.amount.toString() : '');
      setTransactionDate(transactionToEdit.transactionDate || new Date().toISOString().split('T')[0]);
      setMerchant(transactionToEdit.merchantName || transactionToEdit.merchant || '');
      setAccountId(transactionToEdit.accountId ? transactionToEdit.accountId.toString() : '');
      setCategoryId(transactionToEdit.categoryId ? transactionToEdit.categoryId.toString() : '');
      setDescription(transactionToEdit.description || '');
      setIsCleared(transactionToEdit.isCleared ?? true);
    } else {
      // Create mode
      setType('EXPENSE');
      setAmount('');
      // If defaultMonth/Year given and current month doesn't match, pick 1st of that month
      if (defaultMonth && defaultYear) {
        const mm = String(defaultMonth).padStart(2, '0');
        const today = new Date();
        if (today.getMonth() + 1 === defaultMonth && today.getFullYear() === defaultYear) {
          setTransactionDate(today.toISOString().split('T')[0]);
        } else {
          setTransactionDate(`${defaultYear}-${mm}-01`);
        }
      } else {
        setTransactionDate(new Date().toISOString().split('T')[0]);
      }
      setMerchant('');
      setCategoryId(initialCategoryId ? initialCategoryId.toString() : '');
      setDescription('');
      setIsCleared(true);
    }
    setErrorMessage('');
  }, [transactionToEdit, isOpen, initialCategoryId, defaultMonth, defaultYear]);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    const parsedAmount = parseFloat(amount);
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      setErrorMessage('Please enter a valid amount greater than 0.');
      return;
    }

    if (!merchant.trim()) {
      setErrorMessage('Please enter a payee or merchant name.');
      return;
    }

    if (!accountId) {
      setErrorMessage('Please select an account.');
      return;
    }

    if (type === 'EXPENSE' && !categoryId) {
      setErrorMessage('Please select a category for this expense.');
      return;
    }

    const payload = {
      accountId: parseInt(accountId, 10),
      categoryId: categoryId ? parseInt(categoryId, 10) : null,
      type,
      amount: parsedAmount,
      transactionDate,
      merchantName: merchant.trim(),
      description: description.trim() || null,
    };

    setSubmitting(true);
    try {
      if (transactionToEdit) {
        await transactionService.updateTransaction(transactionToEdit.id, payload);
      } else {
        await transactionService.createTransaction(payload);
      }
      onSuccess?.();
      onClose();
    } catch (err) {
      console.error('Failed to save transaction', err);
      setErrorMessage(
        err.response?.data?.message || err.userMessage || 'Failed to save transaction. Please try again.'
      );
    } finally {
      setSubmitting(false);
    }
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
            <h3 className="font-display font-bold text-lg text-slate-900">
              {transactionToEdit ? 'Edit Transaction' : 'New Transaction'}
            </h3>
            <p className="text-xs text-slate-500 mt-0.5">
              {transactionToEdit ? 'Update transaction details' : 'Record an expense or income'}
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
            <div className="p-3.5 bg-rose-50 border border-rose-200 text-rose-700 text-xs font-semibold rounded-xl flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-rose-500 shrink-0" />
              <span>{errorMessage}</span>
            </div>
          )}

          {/* Type Toggle (Expense / Income) */}
          <div className="flex bg-slate-100 p-1 rounded-2xl">
            <button
              type="button"
              onClick={() => setType('EXPENSE')}
              className={`flex-1 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-2 ${
                type === 'EXPENSE'
                  ? 'bg-white text-rose-600 shadow-xs'
                  : 'text-slate-500 hover:text-slate-800'
              }`}
            >
              <ArrowDownRight className="w-4 h-4 text-rose-500" />
              <span>Expense</span>
            </button>
            <button
              type="button"
              onClick={() => setType('INCOME')}
              className={`flex-1 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center justify-center gap-2 ${
                type === 'INCOME'
                  ? 'bg-white text-emerald-600 shadow-xs'
                  : 'text-slate-500 hover:text-slate-800'
              }`}
            >
              <ArrowUpRight className="w-4 h-4 text-emerald-500" />
              <span>Income</span>
            </button>
          </div>

          {/* Amount Input */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-slate-700">Amount</label>
            <div className="relative">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 font-bold text-lg">
                ₹
              </span>
              <input
                type="number"
                step="0.01"
                min="0.01"
                placeholder="0.00"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                required
                className="w-full h-13 pl-9 pr-4 rounded-2xl border border-slate-200 text-xl font-black text-slate-900 focus:border-blue-600 focus:ring-2 focus:ring-blue-100 outline-none transition-all placeholder:text-slate-300"
                autoFocus
              />
            </div>
          </div>

          {/* Date & Account */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-slate-700 flex items-center gap-1.5">
                <Calendar className="w-3.5 h-3.5 text-slate-400" />
                <span>Date</span>
              </label>
              <input
                type="date"
                value={transactionDate}
                onChange={(e) => setTransactionDate(e.target.value)}
                required
                className="w-full h-11 px-3.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-800 focus:border-blue-600 outline-none transition-all"
              />
            </div>

            <div className="space-y-1.5">
              <label className="block text-xs font-bold text-slate-700 flex items-center justify-between">
                <span className="flex items-center gap-1.5">
                  <CreditCard className="w-3.5 h-3.5 text-slate-400" />
                  <span>Account</span>
                </span>
                {accounts.length === 0 && (
                  <a
                    href="/accounts"
                    className="text-blue-600 hover:underline font-bold text-[10px]"
                  >
                    + Add Account
                  </a>
                )}
              </label>
              {accounts.length > 0 ? (
                <select
                  value={accountId}
                  onChange={(e) => setAccountId(e.target.value)}
                  required
                  className="w-full h-11 px-3.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-800 bg-white focus:border-blue-600 outline-none transition-all"
                >
                  <option value="" disabled>Select Account</option>
                  {accounts.map((acc) => (
                    <option key={acc.id} value={acc.id}>
                      {acc.name} (₹{parseFloat(acc.balance || 0).toLocaleString()})
                    </option>
                  ))}
                </select>
              ) : (
                <div className="p-2.5 rounded-xl border border-dashed border-amber-200 bg-amber-50/50 text-[11px] text-amber-800 font-medium">
                  No accounts yet.{' '}
                  <a href="/accounts" className="font-bold underline text-blue-600">
                    Create an account first
                  </a>
                  .
                </div>
              )}
            </div>
          </div>

          {/* Payee / Merchant */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-slate-700">Payee / Merchant</label>
            <input
              type="text"
              placeholder={type === 'EXPENSE' ? 'e.g. Swiggy, Uber, Amazon' : 'e.g. Employer, Client, Refund'}
              value={merchant}
              onChange={(e) => setMerchant(e.target.value)}
              className="w-full h-11 px-3.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-800 focus:border-blue-600 outline-none transition-all placeholder:text-slate-400"
            />
          </div>

          {/* Category (Required for Expense, Optional for Income) */}
          <div className="space-y-1.5">
            <div className="flex items-center justify-between">
              <label className="block text-xs font-bold text-slate-700 flex items-center gap-1.5">
                <Tag className="w-3.5 h-3.5 text-slate-400" />
                <span>Category {type === 'EXPENSE' ? '*' : '(Optional / Ready to Assign)'}</span>
              </label>
              {(merchant || description) && (
                <button
                  type="button"
                  onClick={handleAskAiCategory}
                  disabled={suggestCategoryMutation.isPending}
                  className="inline-flex items-center gap-1 text-[11px] font-semibold text-emerald-600 hover:text-emerald-700 cursor-pointer disabled:opacity-50"
                >
                  {suggestCategoryMutation.isPending ? (
                    <Loader2 className="w-3 h-3 animate-spin text-emerald-600" />
                  ) : (
                    <Sparkles className="w-3 h-3 text-emerald-600" />
                  )}
                  <span>AI Suggest</span>
                </button>
              )}
            </div>

            {/* AI Suggestion Banner */}
            {aiSuggestion && (
              <div className="p-2.5 rounded-xl bg-emerald-50 border border-emerald-200 text-xs flex items-center justify-between gap-2">
                <div className="flex items-center gap-1.5 text-emerald-800">
                  <Sparkles className="w-3.5 h-3.5 text-emerald-600 shrink-0" />
                  <span>
                    AI suggestion: <strong className="font-bold">{aiSuggestion.suggestedCategory}</strong>
                  </span>
                </div>
                <div className="flex items-center gap-1.5">
                  <button
                    type="button"
                    onClick={() => handleApplyAiCategory(aiSuggestion.suggestedCategory)}
                    className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-[11px] font-bold cursor-pointer transition-colors"
                  >
                    Apply
                  </button>
                  <button
                    type="button"
                    onClick={() => setAiSuggestion(null)}
                    className="px-2 py-1 text-slate-500 hover:text-slate-800 text-[11px] font-medium cursor-pointer"
                  >
                    Ignore
                  </button>
                </div>
              </div>
            )}

            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              required={type === 'EXPENSE'}
              className="w-full h-11 px-3.5 rounded-xl border border-slate-200 text-xs font-semibold text-slate-800 bg-white focus:border-blue-600 outline-none transition-all"
            >
              <option value="">
                {type === 'EXPENSE' ? '-- Select a Category --' : 'Ready to Assign (Inflow)'}
              </option>
              {categoryGroups.map((group) => (
                <optgroup key={group.id} label={group.name}>
                  {group.categories?.map((cat) => (
                    <option key={cat.id} value={cat.id}>
                      {cat.name}
                    </option>
                  ))}
                </optgroup>
              ))}
            </select>
          </div>

          {/* Description / Note */}
          <div className="space-y-1.5">
            <label className="block text-xs font-bold text-slate-700 flex items-center gap-1.5">
              <FileText className="w-3.5 h-3.5 text-slate-400" />
              <span>Notes / Description</span>
            </label>
            <textarea
              rows={2}
              placeholder="Add details or receipt reference..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full p-3 rounded-xl border border-slate-200 text-xs font-medium text-slate-800 focus:border-blue-600 outline-none transition-all placeholder:text-slate-400 resize-none"
            />
          </div>

          {/* Cleared Status Checkbox */}
          <div className="flex items-center gap-2 pt-1">
            <input
              type="checkbox"
              id="isClearedCheckbox"
              checked={isCleared}
              onChange={(e) => setIsCleared(e.target.checked)}
              className="w-4 h-4 rounded text-blue-600 border-slate-300 focus:ring-blue-500"
            />
            <label htmlFor="isClearedCheckbox" className="text-xs font-semibold text-slate-600 cursor-pointer select-none">
              Mark transaction as Cleared
            </label>
          </div>

          {/* Submit Actions */}
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
              disabled={submitting}
              className="px-6 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 active:scale-98 text-xs font-bold text-white shadow-md shadow-blue-500/20 transition-all disabled:opacity-50"
            >
              {submitting ? 'Saving...' : transactionToEdit ? 'Save Changes' : 'Add Transaction'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
