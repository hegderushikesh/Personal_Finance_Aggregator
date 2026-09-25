import React, { useState } from 'react';
import {
  Sparkles,
  TrendingDown,
  PieChart,
  Repeat,
  FileText,
  ShieldCheck,
  AlertTriangle,
  ArrowUpRight,
  ArrowDownRight,
  DollarSign,
  Wallet,
  CheckCircle2,
} from 'lucide-react';
import AiChat from '../components/AiChat';
import AiInsightCard from '../components/AiInsightCard';
import {
  useAiHealth,
  useMonthlySummary,
  useSpendingInsights,
  useBudgetInsights,
  useRecurringInsights,
} from '../hooks/useAi';

export default function AiPage() {
  const [activeTab, setActiveTab] = useState('summary'); // summary, spending, budget, recurring

  const { data: health, isLoading: healthLoading } = useAiHealth();
  const { data: monthlySummary, isLoading: summaryLoading } = useMonthlySummary();
  const { data: spendingInsights, isLoading: spendingLoading } = useSpendingInsights();
  const { data: budgetInsights, isLoading: budgetLoading } = useBudgetInsights();
  const { data: recurringInsights, isLoading: recurringLoading } = useRecurringInsights();

  const now = new Date();
  const currentMonthName = now.toLocaleString('default', { month: 'long', year: 'numeric' });

  return (
    <div className="space-y-6 pb-12">
      {/* Top Header Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-gradient-to-r from-[#0A1628] via-[#0F172A] to-[#1E293B] text-white p-6 sm:p-8 rounded-3xl shadow-sm border border-slate-800">
        <div className="space-y-2">
          <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-500/15 border border-emerald-500/30 text-emerald-300 text-xs font-medium">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Local AI Financial Assistant</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight">FinPilot AI</h1>
          <p className="text-sm text-slate-300 max-w-xl leading-relaxed">
            Private, on-device financial analysis powered by your local{' '}
            <span className="font-mono text-emerald-400 font-medium">qwen2.5-coder:7b</span> model.
            Your bank accounts, transactions, and balances never leave your machine.
          </p>
        </div>

        {/* Health status badge */}
        <div className="shrink-0 flex items-center gap-3 bg-white/5 border border-white/10 p-3.5 rounded-2xl backdrop-blur-xs">
          <div
            className={`w-3 h-3 rounded-full ${
              health?.available ? 'bg-emerald-400 animate-pulse' : 'bg-rose-400'
            }`}
          />
          <div className="text-xs">
            <div className="font-semibold text-slate-200">
              {healthLoading
                ? 'Connecting...'
                : health?.available
                ? 'Ollama Online'
                : 'Ollama Offline'}
            </div>
            <div className="text-slate-400 font-mono text-[11px]">
              {health?.model || 'qwen2.5-coder:7b'}
            </div>
          </div>
        </div>
      </div>

      {/* Ollama Offline Warning Banner */}
      {health && !health.available && !healthLoading && (
        <div className="p-4 rounded-2xl bg-amber-50 border border-amber-200 text-amber-800 flex items-start gap-3 text-sm">
          <AlertTriangle className="w-5 h-5 text-amber-600 shrink-0 mt-0.5" />
          <div className="space-y-1">
            <div className="font-semibold">Local AI model is currently unreachable</div>
            <div className="text-xs text-amber-700">
              Ensure Ollama is running locally (<code className="font-mono bg-amber-100/80 px-1 py-0.5 rounded">ollama serve</code>) with model{' '}
              <code className="font-mono bg-amber-100/80 px-1 py-0.5 rounded">qwen2.5-coder:7b</code> available.
            </div>
          </div>
        </div>
      )}

      {/* Main Grid: Left Column (Chat) & Right Column (Insights Panel) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Left 7 Columns: Interactive Assistant */}
        <div className="lg:col-span-7 space-y-4">
          <div className="flex items-center justify-between px-1">
            <h2 className="text-base font-semibold text-slate-900 flex items-center gap-2">
              <span>Ask FinPilot AI</span>
            </h2>
            <span className="text-xs text-slate-400 flex items-center gap-1">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-600" /> Read-only security
            </span>
          </div>
          <AiChat />
        </div>

        {/* Right 5 Columns: AI Financial Insights & Summaries */}
        <div className="lg:col-span-5 space-y-4">
          {/* Insights Tabs Header */}
          <div className="flex items-center gap-1 p-1 bg-slate-100/80 rounded-xl border border-slate-200/80 text-xs font-medium">
            <button
              type="button"
              onClick={() => setActiveTab('summary')}
              className={`flex-1 py-2 px-2.5 rounded-lg transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
                activeTab === 'summary'
                  ? 'bg-white text-slate-900 shadow-xs font-semibold'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <FileText className="w-3.5 h-3.5" />
              <span>Summary</span>
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('spending')}
              className={`flex-1 py-2 px-2.5 rounded-lg transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
                activeTab === 'spending'
                  ? 'bg-white text-slate-900 shadow-xs font-semibold'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <TrendingDown className="w-3.5 h-3.5" />
              <span>Spending</span>
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('budget')}
              className={`flex-1 py-2 px-2.5 rounded-lg transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
                activeTab === 'budget'
                  ? 'bg-white text-slate-900 shadow-xs font-semibold'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <PieChart className="w-3.5 h-3.5" />
              <span>Budget</span>
            </button>
            <button
              type="button"
              onClick={() => setActiveTab('recurring')}
              className={`flex-1 py-2 px-2.5 rounded-lg transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
                activeTab === 'recurring'
                  ? 'bg-white text-slate-900 shadow-xs font-semibold'
                  : 'text-slate-600 hover:text-slate-900'
              }`}
            >
              <Repeat className="w-3.5 h-3.5" />
              <span>Recurring</span>
            </button>
          </div>

          {/* Tab 1: Monthly Summary */}
          {activeTab === 'summary' && (
            <div className="space-y-4">
              <AiInsightCard
                title={`${currentMonthName} Summary`}
                subtitle="Generated from verified PostgreSQL transactions & accounts"
                summary={monthlySummary?.summary}
                insights={monthlySummary?.insights}
                observations={monthlySummary?.observations}
                loading={summaryLoading}
              />
            </div>
          )}

          {/* Tab 2: Spending Insights */}
          {activeTab === 'spending' && (
            <div className="space-y-4">
              <AiInsightCard
                title="Spending Breakdown & Trends"
                subtitle="Aggregated category analysis"
                summary={spendingInsights?.summary}
                insights={spendingInsights?.insights}
                observations={spendingInsights?.observations}
                loading={spendingLoading}
              />
            </div>
          )}

          {/* Tab 3: Budget Insights */}
          {activeTab === 'budget' && (
            <div className="space-y-4">
              <AiInsightCard
                title="Budget Status & Allocation"
                subtitle="Assigned, activity, and targets"
                summary={budgetInsights?.summary}
                insights={budgetInsights?.insights}
                observations={budgetInsights?.observations}
                loading={budgetLoading}
              />
            </div>
          )}

          {/* Tab 4: Recurring Insights */}
          {activeTab === 'recurring' && (
            <div className="space-y-4">
              <AiInsightCard
                title="Recurring Payments & Subscriptions"
                subtitle="Detected ongoing monthly commitments"
                summary={recurringInsights?.summary}
                insights={recurringInsights?.insights}
                observations={recurringInsights?.observations}
                loading={recurringLoading}
              />
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
