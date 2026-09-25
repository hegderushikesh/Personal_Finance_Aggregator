import React, { useState } from 'react';
import {
  Sparkles,
  RefreshCw,
  Search,
  Filter,
  AlertTriangle,
  Repeat,
  Plus,
} from 'lucide-react';
import { toast } from 'sonner';
import {
  useRecurringPayments,
  useDetectRecurringPayments,
  useUpdateRecurringPayment,
  useDeleteRecurringPayment,
} from '../hooks/useRecurringPayments';
import RecurringSummary from '../components/RecurringSummary';
import UpcomingPayments from '../components/UpcomingPayments';
import RecurringPaymentCard from '../components/RecurringPaymentCard';
import RecurringEmptyState from '../components/RecurringEmptyState';
import EditRecurringModal from '../components/EditRecurringModal';

export default function RecurringPaymentsPage() {
  const [statusFilter, setStatusFilter] = useState('');
  const [frequencyFilter, setFrequencyFilter] = useState('');
  const [searchQuery, setSearchQuery] = useState('');

  // Modals state
  const [editingPayment, setEditingPayment] = useState(null);

  // TanStack Query
  const queryParams = {};
  if (statusFilter) queryParams.status = statusFilter;
  if (frequencyFilter) queryParams.frequency = frequencyFilter;

  const { data: payments = [], isLoading, isError, refetch } = useRecurringPayments(queryParams);
  const detectMutation = useDetectRecurringPayments();
  const updateMutation = useUpdateRecurringPayment();
  const deleteMutation = useDeleteRecurringPayment();

  const handleScanTransactions = async () => {
    try {
      const res = await detectMutation.mutateAsync({});
      if (res.detected > 0) {
        toast.success(`Scan completed! Detected ${res.detected} recurring payment${res.detected === 1 ? '' : 's'}.`);
      } else {
        toast.info('Scan completed. No recurring payment patterns found with 3+ transactions.');
      }
    } catch (err) {
      console.error(err);
      toast.error('Unable to detect recurring payments. Please try again.');
    }
  };

  const handleUpdatePayment = async (id, updatedData) => {
    try {
      await updateMutation.mutateAsync({ id, data: updatedData });
      toast.success('Recurring payment updated successfully.');
      setEditingPayment(null);
    } catch (err) {
      console.error(err);
      toast.error('Failed to update recurring payment.');
    }
  };

  const handleDeletePayment = async (id) => {
    try {
      await deleteMutation.mutateAsync(id);
      toast.success('Recurring payment record removed.');
    } catch (err) {
      console.error(err);
      toast.error('Failed to delete recurring payment record.');
    }
  };

  // Filter payments by client-side search query
  const filteredPayments = payments.filter((p) => {
    if (!searchQuery.trim()) return true;
    const query = searchQuery.toLowerCase();
    return (
      p.merchantName?.toLowerCase().includes(query) ||
      p.categoryName?.toLowerCase().includes(query)
    );
  });

  return (
    <div className="space-y-8 pb-12">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold font-display text-slate-900 tracking-tight">
            Recurring Payments
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Subscriptions and recurring expenses detected from your transactions.
          </p>
        </div>

        <button
          onClick={handleScanTransactions}
          disabled={detectMutation.isPending || isLoading}
          className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 active:bg-blue-800 text-white font-semibold text-xs shadow-xs hover:shadow-sm transition-all disabled:opacity-50 shrink-0 cursor-pointer"
        >
          <Sparkles className={`w-4 h-4 ${detectMutation.isPending ? 'animate-spin' : ''}`} />
          <span>{detectMutation.isPending ? 'Scanning...' : 'Scan Transactions'}</span>
        </button>
      </div>

      {/* Error State */}
      {isError && (
        <div className="p-4 rounded-2xl bg-rose-50 border border-rose-200 flex items-center justify-between gap-3 text-rose-800">
          <div className="flex items-center gap-3">
            <AlertTriangle className="w-5 h-5 text-rose-600 shrink-0" />
            <span className="text-sm font-medium">Unable to detect recurring payments.</span>
          </div>
          <button
            onClick={() => refetch()}
            className="px-3.5 py-1.5 rounded-xl bg-rose-600 hover:bg-rose-700 text-white font-semibold text-xs transition-colors"
          >
            Retry
          </button>
        </div>
      )}

      {/* Summary Cards */}
      {!isLoading && !isError && payments.length > 0 && (
        <RecurringSummary payments={payments} />
      )}

      {/* Upcoming Payments Section */}
      {!isLoading && !isError && payments.length > 0 && (
        <UpcomingPayments payments={payments} />
      )}

      {/* Filter and Search Bar */}
      {!isLoading && !isError && payments.length > 0 && (
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-white p-3.5 rounded-2xl border border-slate-200/80 shadow-xs">
          <div className="relative flex-1 max-w-xs">
            <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder="Search merchant or category..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-3.5 py-1.5 text-xs bg-slate-50 rounded-xl border border-slate-200 focus:bg-white focus:outline-none focus:border-blue-500"
            />
          </div>

          <div className="flex items-center gap-2 flex-wrap">
            <div className="flex items-center gap-1.5 text-xs text-slate-500 mr-1">
              <Filter className="w-3.5 h-3.5" />
              <span className="font-semibold">Filter:</span>
            </div>

            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-3 py-1.5 text-xs font-semibold rounded-xl border border-slate-200 bg-white text-slate-700 focus:outline-none focus:border-blue-500"
            >
              <option value="">All Statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="PAUSED">Paused</option>
              <option value="CANCELLED">Cancelled</option>
            </select>

            <select
              value={frequencyFilter}
              onChange={(e) => setFrequencyFilter(e.target.value)}
              className="px-3 py-1.5 text-xs font-semibold rounded-xl border border-slate-200 bg-white text-slate-700 focus:outline-none focus:border-blue-500"
            >
              <option value="">All Frequencies</option>
              <option value="MONTHLY">Monthly</option>
              <option value="WEEKLY">Weekly</option>
              <option value="BIWEEKLY">Biweekly</option>
              <option value="QUARTERLY">Quarterly</option>
              <option value="YEARLY">Yearly</option>
            </select>
          </div>
        </div>
      )}

      {/* Loading Skeleton */}
      {isLoading && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div
              key={i}
              className="bg-white rounded-2xl border border-slate-200 p-5 space-y-4 animate-pulse"
            >
              <div className="flex justify-between items-center">
                <div className="h-5 bg-slate-200 rounded-md w-32" />
                <div className="h-5 bg-slate-200 rounded-full w-16" />
              </div>
              <div className="h-16 bg-slate-100 rounded-xl" />
              <div className="space-y-2">
                <div className="h-4 bg-slate-100 rounded-md w-3/4" />
                <div className="h-4 bg-slate-100 rounded-md w-1/2" />
              </div>
              <div className="h-8 bg-slate-100 rounded-lg pt-2" />
            </div>
          ))}
        </div>
      )}

      {/* Empty State */}
      {!isLoading && !isError && payments.length === 0 && (
        <RecurringEmptyState
          onScan={handleScanTransactions}
          isScanning={detectMutation.isPending}
        />
      )}

      {/* Recurring Payment Cards Grid */}
      {!isLoading && !isError && payments.length > 0 && (
        <div>
          {filteredPayments.length === 0 ? (
            <div className="text-center py-12 text-slate-400 text-sm bg-white rounded-2xl border border-slate-200/80">
              No recurring payments match your filters.
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
              {filteredPayments.map((payment) => (
                <RecurringPaymentCard
                  key={payment.id}
                  payment={payment}
                  onEdit={(p) => setEditingPayment(p)}
                  onDelete={handleDeletePayment}
                />
              ))}
            </div>
          )}
        </div>
      )}

      {/* Edit Modal */}
      {editingPayment && (
        <EditRecurringModal
          isOpen={!!editingPayment}
          payment={editingPayment}
          onClose={() => setEditingPayment(null)}
          onSave={handleUpdatePayment}
          isSaving={updateMutation.isPending}
        />
      )}
    </div>
  );
}
