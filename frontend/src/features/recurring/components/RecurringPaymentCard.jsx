import React, { useState } from 'react';
import {
  Calendar,
  CreditCard,
  Tag,
  Clock,
  ExternalLink,
  Edit2,
  Trash2,
  CheckCircle2,
  PauseCircle,
  XCircle,
  ShieldCheck,
} from 'lucide-react';
import { useNavigate } from 'react-router-dom';

export default function RecurringPaymentCard({ payment, onEdit, onDelete }) {
  const navigate = useNavigate();
  const [confirmDelete, setConfirmDelete] = useState(false);

  const formatCurrency = (amount) => {
    return `₹${Number(amount || 0).toLocaleString('en-IN')}`;
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    const d = new Date(dateStr);
    return d.toLocaleDateString('en-IN', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  const getFrequencyLabel = (freq) => {
    switch (freq) {
      case 'WEEKLY':
        return '/ week';
      case 'BIWEEKLY':
        return '/ 2 weeks';
      case 'MONTHLY':
        return '/ month';
      case 'QUARTERLY':
        return '/ quarter';
      case 'YEARLY':
        return '/ year';
      default:
        return '';
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'ACTIVE':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200/60">
            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
            Active
          </span>
        );
      case 'PAUSED':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-amber-50 text-amber-700 border border-amber-200/60">
            <span className="w-1.5 h-1.5 rounded-full bg-amber-500" />
            Paused
          </span>
        );
      case 'CANCELLED':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-semibold bg-slate-100 text-slate-600 border border-slate-200">
            <span className="w-1.5 h-1.5 rounded-full bg-slate-400" />
            Cancelled
          </span>
        );
      default:
        return null;
    }
  };

  const handleViewTransactions = () => {
    // Navigate to transactions filtered by merchant and all-time range
    navigate(`/transactions?search=${encodeURIComponent(payment.merchantName)}&allTime=true`);
  };

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 p-5 shadow-xs hover:shadow-md transition-all flex flex-col justify-between group">
      <div>
        {/* Card Header: Merchant & Status */}
        <div className="flex items-start justify-between gap-3 mb-3">
          <div className="min-w-0 flex-1">
            <h3 className="font-bold text-slate-900 text-base truncate" title={payment.merchantName}>
              {payment.merchantName}
            </h3>
            <div className="flex items-center gap-2 mt-1 flex-wrap">
              <span className="text-[11px] font-medium text-slate-500 bg-slate-100 px-2 py-0.5 rounded-md flex items-center gap-1">
                <Tag className="w-3 h-3 text-slate-400" />
                {payment.categoryName || 'Uncategorized'}
              </span>
              <span className="text-[11px] font-medium text-slate-500 bg-slate-100 px-2 py-0.5 rounded-md flex items-center gap-1">
                <CreditCard className="w-3 h-3 text-slate-400" />
                {payment.accountName || 'Multiple accounts'}
              </span>
            </div>
          </div>
          <div className="shrink-0">{getStatusBadge(payment.status)}</div>
        </div>

        {/* Pricing / Cadence */}
        <div className="my-4 p-3.5 rounded-xl bg-slate-50/70 border border-slate-100">
          <div className="flex items-baseline gap-1.5">
            <span className="text-xl font-bold font-display text-slate-900">
              {formatCurrency(payment.averageAmount)}
            </span>
            <span className="text-xs font-semibold text-slate-500">
              {getFrequencyLabel(payment.frequency)}
            </span>
          </div>
          <div className="flex items-center justify-between text-[11px] text-slate-400 mt-1">
            <span>Last paid: {formatCurrency(payment.lastAmount)}</span>
            <span>{payment.occurrenceCount} historical charges</span>
          </div>
        </div>

        {/* Key Dates */}
        <div className="space-y-2 text-xs text-slate-600 mb-4">
          <div className="flex items-center justify-between">
            <span className="text-slate-400 flex items-center gap-1.5">
              <Calendar className="w-3.5 h-3.5 text-blue-500" />
              Next Expected:
            </span>
            <span className="font-semibold text-slate-800">
              {formatDate(payment.nextExpectedDate)}
            </span>
          </div>

          <div className="flex items-center justify-between">
            <span className="text-slate-400 flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5 text-slate-400" />
              Last Paid:
            </span>
            <span className="text-slate-600">{formatDate(payment.lastTransactionDate)}</span>
          </div>

          <div className="flex items-center justify-between pt-1">
            <span className="text-slate-400 flex items-center gap-1.5 text-[11px]">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-500" />
              Rule Match:
            </span>
            <span className="text-[11px] font-semibold text-slate-700">
              {payment.confidence}% confidence
            </span>
          </div>
        </div>
      </div>

      {/* Card Actions Footer */}
      <div className="pt-3 border-t border-slate-100 flex items-center justify-between gap-2">
        <button
          onClick={handleViewTransactions}
          className="text-xs font-semibold text-blue-600 hover:text-blue-700 flex items-center gap-1 py-1.5 px-2.5 rounded-lg hover:bg-blue-50 transition-colors"
        >
          <span>View Transactions</span>
          <ExternalLink className="w-3 h-3" />
        </button>

        <div className="flex items-center gap-1">
          <button
            onClick={() => onEdit(payment)}
            className="p-1.5 text-slate-400 hover:text-slate-700 hover:bg-slate-100 rounded-lg transition-colors"
            title="Edit Recurring Details"
          >
            <Edit2 className="w-3.5 h-3.5" />
          </button>

          {confirmDelete ? (
            <div className="flex items-center gap-1 bg-rose-50 px-1.5 py-0.5 rounded-lg border border-rose-200">
              <button
                onClick={() => onDelete(payment.id)}
                className="text-[10px] font-bold text-rose-600 hover:text-rose-700"
              >
                Delete?
              </button>
              <button
                onClick={() => setConfirmDelete(false)}
                className="text-[10px] text-slate-400 hover:text-slate-600"
              >
                ✕
              </button>
            </div>
          ) : (
            <button
              onClick={() => setConfirmDelete(true)}
              className="p-1.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors"
              title="Remove Detection Record"
            >
              <Trash2 className="w-3.5 h-3.5" />
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
