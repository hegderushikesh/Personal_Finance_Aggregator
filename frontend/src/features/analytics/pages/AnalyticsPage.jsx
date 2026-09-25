import React, { useState } from 'react';
import {
  useSpending,
  useSpendingTrends,
  useIncomeExpense,
  useCashFlow,
  useCategorySpending,
  useNetWorth,
} from '../hooks/useAnalytics';
import DateRangeSelector, { computeRangeFromPreset } from '../components/DateRangeSelector';
import SummaryCards from '../components/SummaryCards';
import SpendingBreakdown from '../components/SpendingBreakdown';
import SpendingTrend from '../components/SpendingTrend';
import IncomeExpenseChart from '../components/IncomeExpenseChart';
import CashFlowChart from '../components/CashFlowChart';
import CategorySpending from '../components/CategorySpending';
import NetWorthCard from '../components/NetWorthCard';
import { RefreshCw, AlertTriangle } from 'lucide-react';

export default function AnalyticsPage() {
  // Default range: This Month
  const initialRange = computeRangeFromPreset('THIS_MONTH');
  const [range, setRange] = useState(initialRange);

  // TanStack Query hooks with parameters
  const queryParams = {
    startDate: range.startDate,
    endDate: range.endDate,
  };

  const {
    data: spendingData,
    isLoading: spendingLoading,
    isError: spendingError,
    refetch: refetchSpending,
  } = useSpending(queryParams);

  const {
    data: spendingTrendsData,
    isLoading: trendsLoading,
    isError: trendsError,
    refetch: refetchTrends,
  } = useSpendingTrends(queryParams);

  const {
    data: incomeExpenseData,
    isLoading: incomeExpenseLoading,
    isError: incomeExpenseError,
    refetch: refetchIncomeExpense,
  } = useIncomeExpense(queryParams);

  const {
    data: cashFlowData,
    isLoading: cashFlowLoading,
    isError: cashFlowError,
    refetch: refetchCashFlow,
  } = useCashFlow(queryParams);

  const {
    data: categoriesData,
    isLoading: categoriesLoading,
    isError: categoriesError,
    refetch: refetchCategories,
  } = useCategorySpending(queryParams);

  const {
    data: netWorthData,
    isLoading: netWorthLoading,
    isError: netWorthError,
    refetch: refetchNetWorth,
  } = useNetWorth();

  const isAnyError =
    spendingError ||
    trendsError ||
    incomeExpenseError ||
    cashFlowError ||
    categoriesError ||
    netWorthError;

  const handleRetryAll = () => {
    refetchSpending();
    refetchTrends();
    refetchIncomeExpense();
    refetchCashFlow();
    refetchCategories();
    refetchNetWorth();
  };

  const isSummaryLoading =
    spendingLoading || incomeExpenseLoading || cashFlowLoading || netWorthLoading;

  return (
    <div className="space-y-6 pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-black font-display text-slate-900 tracking-tight">
            Analytics
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Financial overview and spending insights across all accounts.
          </p>
        </div>

        <button
          onClick={handleRetryAll}
          className="self-start sm:self-auto flex items-center gap-2 px-3.5 py-2 rounded-xl text-xs font-semibold bg-white border border-slate-200/80 text-slate-700 hover:bg-slate-50 transition-colors shadow-xs"
        >
          <RefreshCw className="w-3.5 h-3.5 text-slate-500" />
          <span>Refresh Data</span>
        </button>
      </div>

      {/* Date Range Selector */}
      <DateRangeSelector range={range} onRangeChange={setRange} />

      {/* Error Alert if any API call fails */}
      {isAnyError && (
        <div className="p-4 rounded-2xl bg-rose-50 border border-rose-200 flex items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <AlertTriangle className="w-5 h-5 text-rose-600 shrink-0" />
            <div>
              <p className="text-sm font-semibold text-rose-900">Unable to load analytics data.</p>
              <p className="text-xs text-rose-600 mt-0.5">
                Some analytics endpoints returned an error. Please try again.
              </p>
            </div>
          </div>
          <button
            onClick={handleRetryAll}
            className="px-3 py-1.5 rounded-xl bg-rose-600 text-white text-xs font-semibold hover:bg-rose-700 transition-colors shrink-0"
          >
            Retry
          </button>
        </div>
      )}

      {/* Row 1: Summary Cards */}
      <SummaryCards
        incomeData={incomeExpenseData}
        expenseData={spendingData}
        cashFlowData={cashFlowData}
        netWorthData={netWorthData}
        isLoading={isSummaryLoading}
      />

      {/* Row 2: Spending Breakdown & Spending Trends */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <SpendingBreakdown data={spendingData || []} isLoading={spendingLoading} />
        <SpendingTrend data={spendingTrendsData || []} isLoading={trendsLoading} />
      </div>

      {/* Row 3: Income vs Expense & Monthly Cash Flow */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <IncomeExpenseChart
          data={incomeExpenseData || []}
          isLoading={incomeExpenseLoading}
        />
        <CashFlowChart data={cashFlowData || []} isLoading={cashFlowLoading} />
      </div>

      {/* Row 4: Category Spending & Net Worth Overview */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <CategorySpending
          data={categoriesData || []}
          isLoading={categoriesLoading}
        />
        <NetWorthCard data={netWorthData} isLoading={netWorthLoading} />
      </div>
    </div>
  );
}
