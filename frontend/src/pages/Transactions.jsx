import React, { useState, useEffect, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import {
  Plus,
  Search,
  Filter,
  ArrowDownRight,
  ArrowUpRight,
  ChevronLeft,
  ChevronRight,
  Edit2,
  Trash2,
  CheckCircle,
  Circle,
  Calendar,
  X,
  CreditCard,
  RefreshCw,
} from 'lucide-react';
import { transactionService } from '../services/transactionService';
import { accountService } from '../services/accountService';
import { categoryService } from '../services/categoryService';
import TransactionModal from '../components/transactions/TransactionModal';
import DeleteTransactionDialog from '../components/transactions/DeleteTransactionDialog';

const MONTH_NAMES = [
  'January', 'February', 'March', 'April', 'May', 'June',
  'July', 'August', 'September', 'October', 'November', 'December'
];

export default function Transactions() {
  const [searchParams, setSearchParams] = useSearchParams();

  // Date/Month state
  const initialMonth = searchParams.get('month')
    ? parseInt(searchParams.get('month'), 10)
    : new Date().getMonth() + 1;
  const initialYear = searchParams.get('year')
    ? parseInt(searchParams.get('year'), 10)
    : new Date().getFullYear();

  const [currentMonth, setCurrentMonth] = useState(initialMonth);
  const [currentYear, setCurrentYear] = useState(initialYear);
  const [isAllTime, setIsAllTime] = useState(searchParams.get('allTime') === 'true');

  // Filters
  const [searchQuery, setSearchQuery] = useState(searchParams.get('search') || '');
  const [selectedAccountId, setSelectedAccountId] = useState(searchParams.get('accountId') || '');
  const [selectedCategoryId, setSelectedCategoryId] = useState(searchParams.get('categoryId') || '');
  const [selectedType, setSelectedType] = useState(searchParams.get('type') || '');
  const [selectedSource, setSelectedSource] = useState('');

  // Pagination & Data state
  const [transactions, setTransactions] = useState([]);
  const [summary, setSummary] = useState({
    totalIncome: 0,
    totalExpense: 0,
    netCashFlow: 0,
    transactionCount: 0,
  });
  const [page, setPage] = useState(0);
  const [pageSize] = useState(15);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);

  // Options
  const [accounts, setAccounts] = useState([]);
  const [categoryGroups, setCategoryGroups] = useState([]);

  // Modals state
  const [modalOpen, setModalOpen] = useState(false);
  const [editingTransaction, setEditingTransaction] = useState(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [transactionToDelete, setTransactionToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  // Sync URL search params if changed from outside
  useEffect(() => {
    const urlCat = searchParams.get('categoryId');
    if (urlCat && urlCat !== selectedCategoryId) {
      setSelectedCategoryId(urlCat);
    }
    const urlMonth = searchParams.get('month');
    const urlYear = searchParams.get('year');
    if (urlMonth && parseInt(urlMonth, 10) !== currentMonth) {
      setCurrentMonth(parseInt(urlMonth, 10));
    }
    const urlSearch = searchParams.get('search');
    if (urlSearch !== null && urlSearch !== searchQuery) {
      setSearchQuery(urlSearch);
    }
    const urlAllTime = searchParams.get('allTime');
    if (urlAllTime !== null && (urlAllTime === 'true') !== isAllTime) {
      setIsAllTime(urlAllTime === 'true');
    }
  }, [searchParams, searchQuery, isAllTime]);

  // Load accounts and categories once
  useEffect(() => {
    const loadMetadata = async () => {
      try {
        const [accs, cats] = await Promise.all([
          accountService.getAccounts(),
          categoryService.getCategoryGroups(),
        ]);
        setAccounts(accs || []);
        setCategoryGroups(cats || []);
      } catch (err) {
        console.error('Failed to load accounts/categories', err);
      }
    };
    loadMetadata();
  }, []);

  // Fetch transactions and summary
  const fetchData = useCallback(async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: pageSize,
      };

      if (!isAllTime) {
        params.month = currentMonth;
        params.year = currentYear;
      }
      if (selectedAccountId) params.accountId = selectedAccountId;
      if (selectedCategoryId) params.categoryId = selectedCategoryId;
      if (selectedType) params.type = selectedType;
      if (searchQuery.trim()) params.search = searchQuery.trim();

      const [txPage, txSummary] = await Promise.all([
        transactionService.getTransactions(params),
        transactionService.getSummary({
          month: !isAllTime ? currentMonth : undefined,
          year: !isAllTime ? currentYear : undefined,
          accountId: selectedAccountId || undefined,
        }),
      ]);

      setTransactions(txPage?.content || []);
      setTotalPages(txPage?.totalPages || 0);
      setTotalElements(txPage?.totalElements || 0);
      setSummary(txSummary || {
        totalIncome: 0,
        totalExpense: 0,
        netCashFlow: 0,
        transactionCount: 0,
      });
    } catch (err) {
      console.error('Failed to load transactions', err);
    } finally {
      setLoading(false);
    }
  }, [
    page,
    pageSize,
    isAllTime,
    currentMonth,
    currentYear,
    selectedAccountId,
    selectedCategoryId,
    selectedType,
    searchQuery,
  ]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  // Month navigation
  const handlePrevMonth = () => {
    if (currentMonth === 1) {
      setCurrentMonth(12);
      setCurrentYear((y) => y - 1);
    } else {
      setCurrentMonth((m) => m - 1);
    }
    setPage(0);
  };

  const handleNextMonth = () => {
    if (currentMonth === 12) {
      setCurrentMonth(1);
      setCurrentYear((y) => y + 1);
    } else {
      setCurrentMonth((m) => m + 1);
    }
    setPage(0);
  };

  // Reset all filters
  const handleResetFilters = () => {
    setSearchQuery('');
    setSelectedAccountId('');
    setSelectedCategoryId('');
    setSelectedType('');
    setSelectedSource('');
    setIsAllTime(false);
    setPage(0);
    setSearchParams({});
  };

  const hasActiveFilters =
    searchQuery.trim() !== '' ||
    selectedAccountId !== '' ||
    selectedCategoryId !== '' ||
    selectedType !== '' ||
    selectedSource !== '' ||
    isAllTime;

  // Modals handlers
  const handleOpenAddModal = () => {
    setEditingTransaction(null);
    setModalOpen(true);
  };

  const handleOpenEditModal = (tx) => {
    setEditingTransaction(tx);
    setModalOpen(true);
  };

  const handleOpenDeleteDialog = (tx) => {
    setTransactionToDelete(tx);
    setDeleteDialogOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!transactionToDelete) return;
    setDeleting(true);
    try {
      await transactionService.deleteTransaction(transactionToDelete.id);
      setDeleteDialogOpen(false);
      setTransactionToDelete(null);
      fetchData();
    } catch (err) {
      console.error('Failed to delete transaction', err);
    } finally {
      setDeleting(false);
    }
  };

  const displayedTransactions = selectedSource
    ? transactions.filter((t) => (t.source || 'MANUAL') === selectedSource)
    : transactions;

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="font-display font-bold text-2xl text-slate-900">
            Transactions
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Track, filter, and manage your financial activity.
          </p>
        </div>

        <div className="flex items-center gap-3">
          {/* Month Switcher */}
          {!isAllTime ? (
            <div className="flex items-center bg-white border border-slate-200/80 rounded-2xl p-1 shadow-2xs">
              <button
                onClick={handlePrevMonth}
                className="p-1.5 hover:bg-slate-100 rounded-xl text-slate-500 hover:text-slate-800 transition-colors"
                title="Previous Month"
              >
                <ChevronLeft className="w-4 h-4" />
              </button>
              <div className="px-3 py-1 text-xs font-bold text-slate-800 min-w-[130px] text-center select-none">
                {MONTH_NAMES[currentMonth - 1]} {currentYear}
              </div>
              <button
                onClick={handleNextMonth}
                className="p-1.5 hover:bg-slate-100 rounded-xl text-slate-500 hover:text-slate-800 transition-colors"
                title="Next Month"
              >
                <ChevronRight className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <div className="px-4 py-2 bg-slate-100 text-slate-700 font-bold text-xs rounded-2xl">
              All Time View
            </div>
          )}

          <button
            onClick={() => setIsAllTime(!isAllTime)}
            className={`px-3 py-2 rounded-2xl text-xs font-semibold border transition-all ${
              isAllTime
                ? 'bg-blue-50 border-blue-200 text-blue-600'
                : 'bg-white border-slate-200/80 text-slate-600 hover:bg-slate-50'
            }`}
          >
            {isAllTime ? 'Current Month' : 'All Time'}
          </button>

          {/* Add Transaction Button */}
          <button
            onClick={handleOpenAddModal}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white rounded-2xl text-xs font-bold shadow-md shadow-blue-500/20 transition-all cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            <span>Add Transaction</span>
          </button>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Income */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
              Total Income
            </span>
            <div className="font-display font-black text-2xl text-emerald-600 mt-1">
              +₹{parseFloat(summary.totalIncome || 0).toLocaleString()}
            </div>
          </div>
          <div className="w-11 h-11 rounded-2xl bg-emerald-50 text-emerald-600 flex items-center justify-center border border-emerald-100">
            <ArrowUpRight className="w-6 h-6" />
          </div>
        </div>

        {/* Total Expenses */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
              Total Expenses
            </span>
            <div className="font-display font-black text-2xl text-rose-600 mt-1">
              -₹{parseFloat(summary.totalExpense || 0).toLocaleString()}
            </div>
          </div>
          <div className="w-11 h-11 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center border border-rose-100">
            <ArrowDownRight className="w-6 h-6" />
          </div>
        </div>

        {/* Net Cash Flow */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
              Net Cash Flow
            </span>
            <div
              className={`font-display font-black text-2xl mt-1 ${
                (summary.netCashFlow || 0) >= 0 ? 'text-indigo-600' : 'text-rose-600'
              }`}
            >
              {(summary.netCashFlow || 0) >= 0 ? '+' : ''}₹
              {parseFloat(summary.netCashFlow || 0).toLocaleString()}
            </div>
          </div>
          <div
            className={`w-11 h-11 rounded-2xl flex items-center justify-center border ${
              (summary.netCashFlow || 0) >= 0
                ? 'bg-indigo-50 text-indigo-600 border-indigo-100'
                : 'bg-rose-50 text-rose-600 border-rose-100'
            }`}
          >
            <CreditCard className="w-6 h-6" />
          </div>
        </div>

        {/* Transaction Count */}
        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs flex items-center justify-between">
          <div>
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
              Activity Count
            </span>
            <div className="font-display font-black text-2xl text-slate-800 mt-1">
              {summary.transactionCount || 0}{' '}
              <span className="text-xs font-normal text-slate-400">records</span>
            </div>
          </div>
          <div className="w-11 h-11 rounded-2xl bg-slate-50 text-slate-600 flex items-center justify-center border border-slate-200/70">
            <Calendar className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* Filter & Search Bar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs flex flex-wrap items-center justify-between gap-3">
        {/* Search */}
        <div className="relative min-w-[240px] flex-1">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            placeholder="Search payee or notes..."
            value={searchQuery}
            onChange={(e) => {
              setSearchQuery(e.target.value);
              setPage(0);
            }}
            className="w-full h-10 pl-10 pr-4 rounded-xl text-xs bg-slate-50 border border-slate-200/80 focus:bg-white focus:border-blue-500 outline-none transition-all placeholder:text-slate-400 font-medium"
          />
        </div>

        {/* Filter Dropdowns */}
        <div className="flex flex-wrap items-center gap-2">
          {/* Account Filter */}
          <select
            value={selectedAccountId}
            onChange={(e) => {
              setSelectedAccountId(e.target.value);
              setPage(0);
            }}
            className="h-10 px-3 rounded-xl border border-slate-200/80 bg-slate-50 text-xs font-semibold text-slate-700 outline-none focus:border-blue-500 cursor-pointer"
          >
            <option value="">All Accounts</option>
            {accounts.map((acc) => (
              <option key={acc.id} value={acc.id}>
                {acc.name}
              </option>
            ))}
          </select>

          {/* Category Filter */}
          <select
            value={selectedCategoryId}
            onChange={(e) => {
              setSelectedCategoryId(e.target.value);
              setPage(0);
            }}
            className="h-10 px-3 rounded-xl border border-slate-200/80 bg-slate-50 text-xs font-semibold text-slate-700 outline-none focus:border-blue-500 cursor-pointer max-w-[180px]"
          >
            <option value="">All Categories</option>
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

          {/* Type Filter */}
          <select
            value={selectedType}
            onChange={(e) => {
              setSelectedType(e.target.value);
              setPage(0);
            }}
            className="h-10 px-3 rounded-xl border border-slate-200/80 bg-slate-50 text-xs font-semibold text-slate-700 outline-none focus:border-blue-500 cursor-pointer"
          >
            <option value="">All Types</option>
            <option value="EXPENSE">Expense</option>
            <option value="INCOME">Income</option>
          </select>

          {/* Source Filter */}
          <select
            value={selectedSource}
            onChange={(e) => {
              setSelectedSource(e.target.value);
              setPage(0);
            }}
            className="h-10 px-3 rounded-xl border border-slate-200/80 bg-slate-50 text-xs font-semibold text-slate-700 outline-none focus:border-blue-500 cursor-pointer"
          >
            <option value="">All Sources</option>
            <option value="MANUAL">Manual</option>
            <option value="PLAID">Plaid</option>
          </select>

          {/* Clear Filters */}
          {hasActiveFilters && (
            <button
              onClick={handleResetFilters}
              className="h-10 px-3 rounded-xl border border-slate-200 bg-slate-100 hover:bg-slate-200 text-xs font-bold text-slate-600 transition-colors flex items-center gap-1.5"
            >
              <X className="w-3.5 h-3.5" />
              <span>Reset</span>
            </button>
          )}

          {/* Refresh */}
          <button
            onClick={() => fetchData()}
            className="h-10 w-10 flex items-center justify-center rounded-xl border border-slate-200/80 text-slate-500 hover:text-slate-800 hover:bg-slate-50 transition-colors"
            title="Refresh Transactions"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin text-blue-600' : ''}`} />
          </button>
        </div>
      </div>

      {/* Transactions Table Container */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-slate-200/80 bg-slate-50/70 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                <th className="px-6 py-3.5">Date</th>
                <th className="px-6 py-3.5">Payee / Merchant</th>
                <th className="px-6 py-3.5">Category</th>
                <th className="px-6 py-3.5">Account</th>
                <th className="px-6 py-3.5">Notes</th>
                <th className="px-6 py-3.5 text-right">Amount</th>
                <th className="px-6 py-3.5 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-xs font-medium text-slate-700">
              {loading ? (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center gap-2">
                      <RefreshCw className="w-5 h-5 animate-spin text-blue-600" />
                      <span>Loading transactions...</span>
                    </div>
                  </td>
                </tr>
              ) : displayedTransactions.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-16 text-center text-slate-400">
                    <div className="flex flex-col items-center justify-center space-y-2">
                      <div className="w-12 h-12 rounded-2xl bg-slate-100 flex items-center justify-center text-slate-400">
                        <CreditCard className="w-6 h-6" />
                      </div>
                      <p className="font-semibold text-slate-700 text-sm">No transactions found</p>
                      <p className="text-xs text-slate-400 max-w-sm">
                        {hasActiveFilters
                          ? 'No transactions match your active filters. Try clearing or relaxing the criteria.'
                          : 'You have not added any transactions for this period. Click "+ Add Transaction" to start recording activity.'}
                      </p>
                      {hasActiveFilters ? (
                        <button
                          onClick={handleResetFilters}
                          className="mt-2 text-xs font-bold text-blue-600 hover:underline"
                        >
                          Clear all filters
                        </button>
                      ) : (
                        <button
                          onClick={handleOpenAddModal}
                          className="mt-2 px-4 py-2 bg-blue-600 text-white rounded-xl text-xs font-bold shadow-xs hover:bg-blue-700"
                        >
                          Add your first transaction
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ) : (
                displayedTransactions.map((tx) => {
                  const isExpense = tx.type === 'EXPENSE';
                  return (
                    <tr
                      key={tx.id}
                      className="hover:bg-slate-50/60 transition-colors group"
                    >
                      {/* Date */}
                      <td className="px-6 py-4 whitespace-nowrap text-slate-600 font-semibold">
                        {tx.transactionDate}
                      </td>

                      {/* Merchant & Source */}
                      <td className="px-6 py-4 font-bold text-slate-900">
                        <div className="flex items-center gap-2">
                          <span className="truncate max-w-[200px]">{tx.merchantName || tx.merchant || '—'}</span>
                          {tx.source === 'PLAID' ? (
                            <span className="text-[10px] font-semibold px-1.5 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200/60 shrink-0">
                              Plaid
                            </span>
                          ) : (
                            <span className="text-[10px] font-semibold px-1.5 py-0.5 rounded-full bg-slate-100 text-slate-500 border border-slate-200/60 shrink-0">
                              Manual
                            </span>
                          )}
                        </div>
                      </td>

                      {/* Category */}
                      <td className="px-6 py-4">
                        {tx.categoryName ? (
                          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-[11px] font-bold bg-slate-100 text-slate-700 border border-slate-200/60">
                            {tx.categoryName}
                          </span>
                        ) : isExpense ? (
                          <span className="text-amber-600 font-semibold text-[11px]">
                            Uncategorized
                          </span>
                        ) : (
                          <span className="inline-flex items-center px-2.5 py-1 rounded-full text-[11px] font-bold bg-emerald-50 text-emerald-700 border border-emerald-200/60">
                            Ready to Assign
                          </span>
                        )}
                      </td>

                      {/* Account */}
                      <td className="px-6 py-4 whitespace-nowrap text-slate-600">
                        <span className="font-semibold text-slate-800">{tx.accountName || '—'}</span>
                      </td>

                      {/* Notes / Description */}
                      <td className="px-6 py-4 text-slate-500 max-w-xs truncate">
                        {tx.description || '—'}
                      </td>

                      {/* Amount */}
                      <td className="px-6 py-4 whitespace-nowrap text-right font-display font-black text-sm">
                        <span
                          className={
                            isExpense
                              ? 'text-slate-900'
                              : 'text-emerald-600'
                          }
                        >
                          {isExpense ? '-₹' : '+₹'}
                          {parseFloat(tx.amount || 0).toLocaleString()}
                        </span>
                      </td>

                      {/* Actions */}
                      <td className="px-6 py-4 whitespace-nowrap text-center">
                        <div className="flex items-center justify-center gap-1 opacity-80 group-hover:opacity-100 transition-opacity">
                          <button
                            onClick={() => handleOpenEditModal(tx)}
                            className="p-1.5 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                            title="Edit Transaction"
                          >
                            <Edit2 className="w-3.5 h-3.5" />
                          </button>
                          <button
                            onClick={() => handleOpenDeleteDialog(tx)}
                            className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
                            title="Delete Transaction"
                          >
                            <Trash2 className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        {totalPages > 1 && (
          <div className="px-6 py-3.5 border-t border-slate-100 bg-slate-50/50 flex items-center justify-between text-xs text-slate-500 font-semibold">
            <span>
              Showing {page * pageSize + 1}–
              {Math.min((page + 1) * pageSize, totalElements)} of {totalElements} transactions
            </span>
            <div className="flex items-center gap-2">
              <button
                disabled={page === 0}
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                className="px-3 py-1.5 rounded-lg border border-slate-200 bg-white font-bold text-slate-700 disabled:opacity-40 hover:bg-slate-50 transition-all flex items-center gap-1 cursor-pointer disabled:cursor-not-allowed"
              >
                <ChevronLeft className="w-3.5 h-3.5" />
                <span>Previous</span>
              </button>
              <span className="px-2 font-bold text-slate-700">
                {page + 1} / {totalPages}
              </span>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
                className="px-3 py-1.5 rounded-lg border border-slate-200 bg-white font-bold text-slate-700 disabled:opacity-40 hover:bg-slate-50 transition-all flex items-center gap-1 cursor-pointer disabled:cursor-not-allowed"
              >
                <span>Next</span>
                <ChevronRight className="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Transaction Add / Edit Modal */}
      <TransactionModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        onSuccess={() => fetchData()}
        transactionToEdit={editingTransaction}
        initialCategoryId={selectedCategoryId}
        defaultMonth={currentMonth}
        defaultYear={currentYear}
      />

      {/* Delete Confirmation Dialog */}
      <DeleteTransactionDialog
        isOpen={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        onConfirm={handleDeleteConfirm}
        transaction={transactionToDelete}
        loading={deleting}
      />
    </div>
  );
}
