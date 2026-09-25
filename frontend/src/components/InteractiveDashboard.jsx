import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';
import { TrendingUp, Wallet, Target, PieChart as PieIcon } from 'lucide-react';

const overviewData = [
  { month: 'Jan', netWorth: 1240000 }, { month: 'Feb', netWorth: 1320000 },
  { month: 'Mar', netWorth: 1390000 }, { month: 'Apr', netWorth: 1480000 },
  { month: 'May', netWorth: 1610000 }, { month: 'Jun', netWorth: 1720000 },
  { month: 'Jul', netWorth: 1840000 },
];
const spendingData = [
  { category: 'Housing', amount: 35000 }, { category: 'Investments', amount: 45000 },
  { category: 'Dining', amount: 12000 }, { category: 'Shopping', amount: 15000 },
  { category: 'Travel', amount: 8000 },
];
const allocationData = [
  { name: 'Equity Funds', value: 45, color: '#D4AF37' }, { name: 'Debt & Vaults', value: 30, color: '#6366F1' },
  { name: 'Gold Bonds', value: 15, color: '#E5C158' }, { name: 'Cash', value: 10, color: '#10B981' },
];
const goalsData = [
  { name: 'Emergency Fund (6 Mo)', target: 600000, current: 520000, percent: 86 },
  { name: 'House Downpayment', target: 2500000, current: 1750000, percent: 70 },
  { name: 'Retirement Corpus 2040', target: 10000000, current: 3840000, percent: 38 },
];
const tabs = [
  { id: 'overview', label: 'Overview', icon: TrendingUp }, { id: 'spending', label: 'Spending', icon: Wallet },
  { id: 'investments', label: 'Investments', icon: PieIcon }, { id: 'goals', label: 'Goals', icon: Target },
];
const chartTooltip = { backgroundColor: '#1C2541', borderColor: '#D4AF37', borderRadius: '12px', color: '#fff' };

export default function InteractiveDashboard() {
  const [activeTab, setActiveTab] = useState('overview');
  return (
    <section id="dashboard" className="relative overflow-hidden bg-[#0B132B] py-20 text-white">
      <div className="pointer-events-none absolute left-1/2 top-1/2 h-[600px] w-[600px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-[#D4AF37]/10 blur-[120px]" />
      <div className="relative z-10 mx-auto w-full max-w-[1920px] px-4 sm:px-8 lg:px-12 xl:px-16">
        <div className="mx-auto mb-12 max-w-3xl text-center">
          <div className="mb-3 inline-flex items-center gap-2"><span className="h-[2px] w-8 bg-[#D4AF37]" /><span className="text-xs font-bold uppercase tracking-[0.22em] text-[#E5C158]">Live Interactive Suite</span><span className="h-[2px] w-8 bg-[#D4AF37]" /></div>
          <h2 className="mb-4 text-3xl font-extrabold tracking-tight text-white sm:text-4xl lg:text-5xl font-display">Command center for <span className="text-gold-gradient">your net worth.</span></h2>
          <p className="text-base text-slate-300 sm:text-lg">Real-time analytics, automated asset rebalancing, and goal tracking—all in one Swiss-grade dashboard.</p>
        </div>
        <div className="relative rounded-[2.5rem] border border-[#D4AF37]/30 bg-[#0A1128] p-6 shadow-gold-ring sm:p-10">
          <div className="mx-auto mb-10 flex max-w-xl flex-wrap items-center justify-center gap-2 rounded-full border border-white/10 bg-[#1C2541]/90 p-1.5">
            {tabs.map(({ id, label, icon: Icon }) => { const isActive = activeTab === id; return (
              <button key={id} type="button" onClick={() => setActiveTab(id)} aria-selected={isActive} role="tab" className={`relative flex items-center gap-2 rounded-full px-5 py-2.5 text-xs font-bold transition-colors sm:text-sm ${isActive ? 'text-[#0A1128]' : 'text-slate-300 hover:text-white'}`}>
                {isActive && <motion.div layoutId="activeTabPill" className="absolute inset-0 rounded-full bg-gold-gradient shadow-md" transition={{ type: 'spring', stiffness: 380, damping: 30 }} />}
                <span className="relative z-10 flex items-center gap-2"><Icon className="h-4 w-4" />{label}</span>
              </button>
            ); })}
          </div>
          <div className="min-h-[380px]">
            <AnimatePresence mode="wait">
              {activeTab === 'overview' && <motion.div key="overview" initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -15 }} className="grid grid-cols-1 items-center gap-8 lg:grid-cols-12">
                <div className="h-80 lg:col-span-8"><ResponsiveContainer width="100%" height="100%"><AreaChart data={overviewData}><defs><linearGradient id="netWorthGrad" x1="0" y1="0" x2="0" y2="1"><stop offset="5%" stopColor="#D4AF37" stopOpacity={0.4} /><stop offset="95%" stopColor="#D4AF37" stopOpacity={0} /></linearGradient></defs><XAxis dataKey="month" stroke="#94A3B8" fontSize={12} tickLine={false} /><YAxis stroke="#94A3B8" fontSize={12} tickLine={false} tickFormatter={(value) => `₹${value / 100000}L`} /><Tooltip contentStyle={chartTooltip} formatter={(value) => [`₹${value.toLocaleString()}`, 'Net Worth']} /><Area type="monotone" dataKey="netWorth" stroke="#D4AF37" strokeWidth={3} fill="url(#netWorthGrad)" /></AreaChart></ResponsiveContainer></div>
                <div className="space-y-4 lg:col-span-4"><Summary title="Total Net Worth" value="₹18,40,000" note="↑ +₹1,20,000 this month" /><Summary title="Portfolio CAGR" value="16.8%" note="Outperforming Nifty 50 by +4.2%" accent /></div>
              </motion.div>}
              {activeTab === 'spending' && <motion.div key="spending" initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -15 }} className="grid grid-cols-1 items-center gap-8 lg:grid-cols-12"><div className="h-80 lg:col-span-8"><ResponsiveContainer width="100%" height="100%"><BarChart data={spendingData}><XAxis dataKey="category" stroke="#94A3B8" fontSize={12} tickLine={false} /><YAxis stroke="#94A3B8" fontSize={12} tickLine={false} tickFormatter={(value) => `₹${value / 1000}k`} /><Tooltip contentStyle={chartTooltip} formatter={(value) => [`₹${value.toLocaleString()}`, 'Amount']} /><Bar dataKey="amount" fill="#6366F1" radius={[8, 8, 0, 0]} /></BarChart></ResponsiveContainer></div><div className="space-y-4 lg:col-span-4"><Summary title="Monthly Spends" value="₹1,15,000" note="12% under budget" /><Summary title="Largest Category" value="Investments (39%)" note="Primary wealth building allocation" accent /></div></motion.div>}
              {activeTab === 'investments' && <motion.div key="investments" initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -15 }} className="grid grid-cols-1 items-center gap-8 lg:grid-cols-12"><div className="flex h-80 items-center justify-center lg:col-span-6"><ResponsiveContainer width="100%" height="100%"><PieChart><Pie data={allocationData} cx="50%" cy="50%" innerRadius={65} outerRadius={95} paddingAngle={5} dataKey="value">{allocationData.map((entry) => <Cell key={entry.name} fill={entry.color} />)}</Pie><Tooltip contentStyle={chartTooltip} formatter={(value) => [`${value}%`, 'Allocation']} /></PieChart></ResponsiveContainer></div><div className="space-y-3 lg:col-span-6">{allocationData.map((item) => <div key={item.name} className="flex items-center justify-between rounded-xl border border-white/10 bg-[#1C2541] p-3.5"><span className="flex items-center gap-3 text-sm font-semibold"><span className="h-3.5 w-3.5 rounded-full" style={{ backgroundColor: item.color }} />{item.name}</span><span className="text-sm font-bold text-slate-200">{item.value}%</span></div>)}</div></motion.div>}
              {activeTab === 'goals' && <motion.div key="goals" initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -15 }} className="mx-auto max-w-3xl space-y-6 py-4">{goalsData.map((goal) => <div key={goal.name} className="space-y-2 rounded-2xl border border-white/10 bg-[#1C2541] p-5"><div className="flex justify-between text-sm font-semibold"><span>{goal.name}</span><span className="text-[#E5C158]">₹{goal.current.toLocaleString()} / ₹{goal.target.toLocaleString()}</span></div><div className="h-3 w-full overflow-hidden rounded-full bg-slate-800"><motion.div initial={{ width: 0 }} animate={{ width: `${goal.percent}%` }} className="h-full rounded-full bg-gold-gradient" /></div><div className="flex justify-between text-xs text-slate-400"><span>{goal.percent}% completed</span><span className="text-emerald-400">On track for 2027 target</span></div></div>)}</motion.div>}
            </AnimatePresence>
          </div>
        </div>
      </div>
    </section>
  );
}

function Summary({ title, value, note, accent = false }) {
  return <div className="rounded-2xl border border-white/10 bg-[#1C2541] p-5"><div className="text-xs text-slate-400">{title}</div><div className={`mt-1 text-2xl font-extrabold font-display ${accent ? 'text-[#E5C158]' : 'text-white'}`}>{value}</div><div className={`mt-1 text-xs font-semibold ${accent ? 'text-slate-400' : 'text-emerald-400'}`}>{note}</div></div>;
}
