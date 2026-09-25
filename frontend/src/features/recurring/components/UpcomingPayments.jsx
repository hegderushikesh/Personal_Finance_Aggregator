import React from 'react';
import { CalendarClock, AlertCircle, ArrowUpRight } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function UpcomingPayments({ payments = [] }) {
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  // Filter active payments with future or recent expected date and sort ASC
  const upcomingList = payments
    .filter((p) => p.status === 'ACTIVE' && p.nextExpectedDate)
    .sort((a, b) => new Date(a.nextExpectedDate) - new Date(b.nextExpectedDate))
    .slice(0, 6);

  if (upcomingList.length === 0) {
    return null;
  }

  const formatExpectedDate = (dateStr) => {
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  const getDueBadge = (dateStr) => {
    const d = new Date(dateStr);
    d.setHours(0, 0, 0, 0);
    const diffDays = Math.ceil((d - today) / (1000 * 60 * 60 * 24));

    if (diffDays < 0) {
      return (
        <span className="px-2 py-0.5 text-[11px] font-semibold rounded-md bg-amber-100 text-amber-700">
          Overdue by {Math.abs(diffDays)}d
        </span>
      );
    }
    if (diffDays === 0) {
      return (
        <span className="px-2 py-0.5 text-[11px] font-semibold rounded-md bg-rose-100 text-rose-700 animate-pulse">
          Expected Today
        </span>
      );
    }
    if (diffDays <= 7) {
      return (
        <span className="px-2 py-0.5 text-[11px] font-semibold rounded-md bg-blue-100 text-blue-700">
          In {diffDays} {diffDays === 1 ? 'day' : 'days'}
        </span>
      );
    }
    return (
      <span className="px-2 py-0.5 text-[11px] font-medium rounded-md bg-slate-100 text-slate-600">
        In {diffDays} days
      </span>
    );
  };

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center">
            <CalendarClock className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-800">Upcoming Payments</h3>
            <p className="text-[11px] text-slate-400">
              Estimated renewal dates based on payment cadence
            </p>
          </div>
        </div>
        <div className="flex items-center gap-1.5 text-[11px] text-slate-400 bg-slate-50 px-2.5 py-1 rounded-lg border border-slate-200/50">
          <AlertCircle className="w-3 h-3 text-slate-400" />
          <span>Expected date, not guaranteed</span>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
        {upcomingList.map((item) => (
          <div
            key={item.id}
            className="p-3.5 rounded-xl border border-slate-100 bg-slate-50/50 hover:bg-slate-50 transition-colors flex flex-col justify-between gap-2.5"
          >
            <div className="flex items-start justify-between gap-2">
              <div className="min-w-0">
                <span className="text-xs font-bold text-slate-800 truncate block">
                  {item.merchantName}
                </span>
                <span className="text-[11px] text-slate-400">
                  {item.categoryName || 'Subscription'}
                </span>
              </div>
              <div className="text-right shrink-0">
                <div className="text-xs font-bold text-slate-900">
                  ₹{Number(item.averageAmount).toLocaleString('en-IN')}
                </div>
                <span className="text-[10px] uppercase font-semibold text-slate-400">
                  {item.frequency?.toLowerCase()}
                </span>
              </div>
            </div>

            <div className="flex items-center justify-between pt-1 border-t border-slate-200/50 text-xs">
              <div className="flex items-center gap-1.5">
                <span className="text-[11px] text-slate-500 font-medium">
                  {formatExpectedDate(item.nextExpectedDate)}
                </span>
              </div>
              {getDueBadge(item.nextExpectedDate)}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
