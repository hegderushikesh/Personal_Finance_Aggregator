import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import useAuth from '../hooks/useAuth';
import {
  Wallet,
  ArrowDownRight,
  PieChart,
  Plus,
  ArrowRight,
  CreditCard,
  RefreshCw,
  TrendingUp,
  PiggyBank,
  Sparkles,
  Bot,
} from 'lucide-react';
import { accountService } from '../services/accountService';
import { transactionService } from '../services/transactionService';
import { budgetService } from '../services/budgetService';
import { useMonthlySummary } from '../features/ai/hooks/useAi';
import TransactionModal from '../components/transactions/TransactionModal';

export default function Dashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const now = new Date();
  const currentMonth = now.getMonth() + 1;
  const currentYear = now.getFullYear();

  const [loading, setLoading] = useState(true);
  const [accountSummary, setAccountSummary] = useState(null);
  const [totalBalance, setTotalBalance] = useState(0);
  const [monthlySpending, setMonthlySpending] = useState(0);
  const [monthlyBudget, setMonthlyBudget] = useState(0);
  const [recentTransactions, setRecentTransactions] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const { data: aiSummary, isLoading: aiLoading } = useMonthlySummary({ month: currentMonth, year: currentYear });

  const fetchDashboardData = async () => {
    setLoading(true);
    try {
      const [accounts, accSummary, summary, budget, recents] = await Promise.all([
        accountService.getAccounts().catch(() => []),
        accountService.getAccountSummary().catch(() => null),
        transactionService.getSummary({ month: currentMonth, year: currentYear }).catch(() => null),
        budgetService.getBudget(currentYear, currentMonth).catch(() => null),
        transactionService.getRecentTransactions(6).catch(() => []),
      ]);

      setAccountSummary(accSummary);

      // Calculate total balance from all active accounts
      const balance = (accounts || []).reduce(
        (sum, acc) => sum + (parseFloat(acc.balance) || 0),
        0
      );
      setTotalBalance(balance);

      // Monthly spending from summary
      setMonthlySpending(summary?.totalExpense || 0);

      // Monthly budget (total assigned across all categories or budget starting balance)
      let totalAssigned = 0;
      if (budget?.groups) {
        totalAssigned = budget.groups.reduce(
          (sum, g) => sum + (parseFloat(g.assigned) || 0),
          0
        );
      }
      setMonthlyBudget(totalAssigned);

      // Recent transactions
      setRecentTransactions(recents || []);
    } catch (err) {
      console.error('Failed to load dashboard metrics', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const hour = now.getHours();
  const greeting = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening';
  const firstName = user?.name?.split(' ')[0] || 'there';

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">
            {greeting}, {firstName} 👋
          </h1>
          <p className="mt-1 text-sm text-slate-500">Here&apos;s your financial overview.</p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => fetchDashboardData()}
            className="p-2.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-600 transition-all shadow-2xs"
            title="Refresh"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin text-blue-600' : ''}`} />
          </button>
          <button
            onClick={() => setIsModalOpen(true)}
            className="flex items-center gap-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white rounded-xl text-xs font-bold shadow-md shadow-blue-500/20 transition-all"
          >
            <Plus className="w-4 h-4" />
            <span>Add Transaction</span>
          </button>
        </div>
      </div>

      {/* Account Overview Metric Cards */}
      <div>
        <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-3">
          Account Overview & Net Worth
        </h3>
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {/* Total Cash */}
          <div className="rounded-2xl border border-slate-200/80 bg-white p-5 shadow-xs flex items-center justify-between">
            <div>
              <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                Total Cash
              </p>
              <p className="mt-1 text-xl font-black text-slate-900">
                ₹{(parseFloat(accountSummary?.totalCash ?? totalBalance)).toLocaleString()}
              </p>
              <span className="text-[10px] text-slate-400">Checking & cash</span>
            </div>
            <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center border border-blue-100">
              <Wallet className="w-5 h-5" />
            </div>
          </div>

          {/* Total Savings */}
          <div className="rounded-2xl border border-slate-200/80 bg-white p-5 shadow-xs flex items-center justify-between">
            <div>
              <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                Total Savings
              </p>
              <p className="mt-1 text-xl font-black text-indigo-600">
                ₹{(parseFloat(accountSummary?.totalSavings ?? 0)).toLocaleString()}
              </p>
              <span className="text-[10px] text-slate-400">Savings & deposits</span>
            </div>
            <div className="w-10 h-10 rounded-xl bg-indigo-50 text-indigo-600 flex items-center justify-center border border-indigo-100">
              <PiggyBank className="w-5 h-5" />
            </div>
          </div>

          {/* Total Credit */}
          <div className="rounded-2xl border border-slate-200/80 bg-white p-5 shadow-xs flex items-center justify-between">
            <div>
              <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                Credit Liabilities
              </p>
              <p className="mt-1 text-xl font-black text-rose-600">
                ₹{(parseFloat(accountSummary?.totalCredit ?? 0)).toLocaleString()}
              </p>
              <span className="text-[10px] text-slate-400">Balance owed</span>
            </div>
            <div className="w-10 h-10 rounded-xl bg-rose-50 text-rose-600 flex items-center justify-center border border-rose-100">
              <CreditCard className="w-5 h-5" />
            </div>
          </div>

          {/* Net Worth */}
          <div className="rounded-2xl border border-slate-200/80 bg-white p-5 shadow-xs flex items-center justify-between">
            <div>
              <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                Net Worth
              </p>
              <p className="mt-1 text-xl font-black text-emerald-600">
                ₹{(parseFloat(accountSummary?.netWorth ?? totalBalance)).toLocaleString()}
              </p>
              <span className="text-[10px] text-slate-400">Assets - Liabilities</span>
            </div>
            <div className="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center border border-emerald-100">
              <TrendingUp className="w-5 h-5" />
            </div>
          </div>
        </div>
      </div>

      {/* Monthly Budget & Spending Cards */}
      <div className="grid gap-4 sm:grid-cols-2">
        {/* Monthly Spending */}
        <div className="rounded-2xl border border-slate-200/80 bg-white p-5 shadow-xs flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
              Monthly Spending (Activity)
            </p>
            <p className="mt-2 text-2xl font-black text-rose-600">
              ₹{monthlySpending.toLocaleString()}
            </p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center border border-rose-100">
            <ArrowDownRight className="w-6 h-6" />
          </div>
        </div>

        {/* Monthly Budget */}
        <div className="rounded-2xl border border-slate-200/80 bg-white p-5 shadow-xs flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
              Monthly Budget (Assigned)
            </p>
            <p className="mt-2 text-2xl font-black text-slate-900">
              ₹{monthlyBudget.toLocaleString()}
            </p>
          </div>
          <div className="w-12 h-12 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center border border-indigo-100">
            <PieChart className="w-6 h-6" />
          </div>
        </div>
      </div>

      {/* FinPilot AI Assistant Quick Insights Banner */}
      <div className="rounded-2xl border border-slate-200/80 bg-gradient-to-r from-slate-900 via-[#0F172A] to-slate-800 text-white p-5 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-start sm:items-center gap-3.5">
          <div className="w-10 h-10 rounded-xl bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 flex items-center justify-center shrink-0">
            <Sparkles className="w-5 h-5" />
          </div>
          <div className="space-y-0.5">
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold text-emerald-400">FinPilot AI</span>
              <span className="text-[10px] text-slate-400 font-mono bg-white/10 px-1.5 py-0.5 rounded">qwen2.5-coder:7b</span>
            </div>
            <p className="text-sm font-medium text-slate-200 line-clamp-1">
              {aiLoading
                ? 'Analyzing your finances with local AI...'
                : aiSummary?.summary || 'Your monthly financial overview is ready for analysis.'}
            </p>
          </div>
        </div>
        <button
          onClick={() => navigate('/ai')}
          className="self-start sm:self-center px-4 py-2 bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold rounded-xl text-xs flex items-center gap-1.5 transition-all shadow-xs shrink-0 cursor-pointer"
        >
          <span>Ask AI</span>
          <ArrowRight className="w-3.5 h-3.5" />
        </button>
      </div>

      {/* Recent Transactions & Activity Section */}
      <section className="rounded-2xl border border-slate-200/80 bg-white shadow-xs overflow-hidden">
        <div className="p-6 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h2 className="text-base font-bold text-slate-900">Recent Transactions</h2>
            <p className="text-xs text-slate-400 mt-0.5">Your latest account activities</p>
          </div>
          <button
            onClick={() => navigate('/transactions')}
            className="text-xs font-bold text-blue-600 hover:text-blue-700 flex items-center gap-1.5 hover:underline"
          >
            <span>View all</span>
            <ArrowRight className="w-3.5 h-3.5" />
          </button>
        </div>

        {loading ? (
          <div className="p-12 text-center text-slate-400 text-xs">
            <RefreshCw className="w-5 h-5 animate-spin mx-auto mb-2 text-blue-600" />
            <span>Loading recent activity...</span>
          </div>
        ) : recentTransactions.length === 0 ? (
          <div className="p-12 text-center text-sm text-slate-400">
            <CreditCard className="w-10 h-10 mx-auto mb-2 text-slate-300" />
            <p className="font-semibold text-slate-700">No transactions yet</p>
            <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
              Start by adding your first transaction to see your spending activity reflected here.
            </p>
            <button
              onClick={() => setIsModalOpen(true)}
              className="mt-4 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-bold shadow-xs transition-all"
            >
              Add First Transaction
            </button>
          </div>
        ) : (
          <div className="divide-y divide-slate-100">
            {recentTransactions.map((tx) => {
              const isExpense = tx.type === 'EXPENSE';
              return (
                <div
                  key={tx.id}
                  onClick={() => navigate('/transactions')}
                  className="p-4 sm:px-6 hover:bg-slate-50/70 transition-colors flex items-center justify-between cursor-pointer"
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div
                      className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 border ${
                        isExpense
                          ? 'bg-rose-50 text-rose-600 border-rose-100'
                          : 'bg-emerald-50 text-emerald-600 border-emerald-100'
                      }`}
                    >
                      {isExpense ? (
                        <ArrowDownRight className="w-5 h-5" />
                      ) : (
                        <TrendingUp className="w-5 h-5" />
                      )}
                    </div>
                    <div className="min-w-0">
                      <p className="text-xs font-bold text-slate-900 truncate">
                        {tx.merchantName || tx.merchant || (isExpense ? 'Expense' : 'Income')}
                      </p>
                      <p className="text-[11px] text-slate-400 mt-0.5 truncate">
                        {tx.categoryName || (isExpense ? 'Uncategorized' : 'Ready to Assign')} •{' '}
                        {tx.accountName || 'Account'} • {tx.transactionDate}
                      </p>
                    </div>
                  </div>

                  <div className="text-right pl-4 whitespace-nowrap">
                    <span
                      className={`font-display font-black text-sm ${
                        isExpense ? 'text-slate-900' : 'text-emerald-600'
                      }`}
                    >
                      {isExpense ? '-₹' : '+₹'}
                      {parseFloat(tx.amount || 0).toLocaleString()}
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>

      {/* Transaction Modal */}
      <TransactionModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSuccess={() => fetchDashboardData()}
      />
    </div>
  );
}
