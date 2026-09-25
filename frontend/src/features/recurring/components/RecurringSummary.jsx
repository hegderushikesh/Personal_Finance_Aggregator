import React from 'react';
import { Repeat, CalendarClock, TrendingUp, Sparkles } from 'lucide-react';

export function calculateNormalizedMonthlyCost(payment) {
  const amount = Number(payment.averageAmount || payment.lastAmount || 0);
  switch (payment.frequency) {
    case 'WEEKLY':
      return (amount * 52) / 12;
    case 'BIWEEKLY':
      return (amount * 26) / 12;
    case 'MONTHLY':
      return amount;
    case 'QUARTERLY':
      return amount / 3;
    case 'YEARLY':
      return amount / 12;
    default:
      return amount;
  }
}

export default function RecurringSummary({ payments = [] }) {
  const activePayments = payments.filter((p) => p.status === 'ACTIVE');

  // Normalized monthly recurring cost for all active payments
  const monthlyRecurring = activePayments.reduce(
    (sum, p) => sum + calculateNormalizedMonthlyCost(p),
    0
  );

  const annualizedCost = monthlyRecurring * 12;

  // Upcoming in the next 30 days
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const thirtyDaysLater = new Date(today);
  thirtyDaysLater.setDate(today.getDate() + 30);

  const upcomingCount = activePayments.filter((p) => {
    if (!p.nextExpectedDate) return false;
    const nextDate = new Date(p.nextExpectedDate);
    return nextDate >= today && nextDate <= thirtyDaysLater;
  }).length;

  const stats = [
    {
      title: 'Active Subscriptions',
      value: activePayments.length.toString(),
      subtext: `${payments.length} total detected`,
      icon: Repeat,
      color: 'from-blue-600 to-indigo-600',
      iconBg: 'bg-blue-500/10 text-blue-600',
    },
    {
      title: 'Monthly Recurring',
      value: `₹${Math.round(monthlyRecurring).toLocaleString('en-IN')}`,
      subtext: 'Normalized monthly run-rate',
      icon: TrendingUp,
      color: 'from-emerald-600 to-teal-600',
      iconBg: 'bg-emerald-500/10 text-emerald-600',
    },
    {
      title: 'Upcoming (30 Days)',
      value: upcomingCount.toString(),
      subtext: 'Expected renewals',
      icon: CalendarClock,
      color: 'from-amber-600 to-orange-600',
      iconBg: 'bg-amber-500/10 text-amber-600',
    },
    {
      title: 'Annualized Cost',
      value: `₹${Math.round(annualizedCost).toLocaleString('en-IN')}`,
      subtext: 'Projected 12-month spend',
      icon: Sparkles,
      color: 'from-purple-600 to-violet-600',
      iconBg: 'bg-purple-500/10 text-purple-600',
    },
  ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      {stats.map((stat, idx) => {
        const Icon = stat.icon;
        return (
          <div
            key={idx}
            className="p-5 bg-white rounded-2xl border border-slate-200/80 shadow-xs hover:shadow-md transition-shadow relative overflow-hidden flex flex-col justify-between"
          >
            <div className="flex items-center justify-between gap-2">
              <span className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                {stat.title}
              </span>
              <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${stat.iconBg}`}>
                <Icon className="w-4 h-4" />
              </div>
            </div>
            <div className="mt-3">
              <div className="text-2xl font-bold font-display text-slate-900 tracking-tight">
                {stat.value}
              </div>
              <p className="text-xs text-slate-400 mt-1">{stat.subtext}</p>
            </div>
          </div>
        );
      })}
    </div>
  );
}
