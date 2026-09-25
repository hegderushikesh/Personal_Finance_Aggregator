import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Landmark, PiggyBank, CreditCard, Wallet, RefreshCw, AlertCircle } from 'lucide-react';
import { toast } from 'sonner';
import { accountService } from '../services/accountService';
import AccountSummaryCards from '../components/accounts/AccountSummaryCards';
import AccountGroup from '../components/accounts/AccountGroup';
import AddAccountModal from '../components/accounts/AddAccountModal';
import EditAccountModal from '../components/accounts/EditAccountModal';
import DeleteAccountDialog from '../components/accounts/DeleteAccountDialog';
import PlaidLinkButton from '../features/plaid/components/PlaidLinkButton';
import ConnectedInstitutions from '../features/plaid/components/ConnectedInstitutions';

export default function Accounts() {
  const queryClient = useQueryClient();

  // Modals state
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [editingAccount, setEditingAccount] = useState(null);
  const [deletingAccount, setDeletingAccount] = useState(null);
  const [deleteErrorMessage, setDeleteErrorMessage] = useState('');

  // 1. Fetch accounts
  const {
    data: accounts = [],
    isLoading: isAccountsLoading,
    isError: isAccountsError,
    error: accountsError,
    refetch: refetchAccounts,
  } = useQuery({
    queryKey: ['accounts'],
    queryFn: accountService.getAccounts,
    retry: 1,
  });

  // 2. Fetch account summary
  const {
    data: summary,
    isLoading: isSummaryLoading,
    isError: isSummaryError,
    error: summaryError,
    refetch: refetchSummary,
  } = useQuery({
    queryKey: ['account-summary'],
    queryFn: accountService.getAccountSummary,
    retry: 1,
  });

  // Invalidate related queries
  const invalidateAll = () => {
    queryClient.invalidateQueries({ queryKey: ['accounts'] });
    queryClient.invalidateQueries({ queryKey: ['account-summary'] });
    queryClient.invalidateQueries({ queryKey: ['transactions'] });
    queryClient.invalidateQueries({ queryKey: ['dashboard'] });
    queryClient.invalidateQueries({ queryKey: ['plaid-connections'] });
    queryClient.invalidateQueries({ queryKey: ['analytics'] });
    queryClient.invalidateQueries({ queryKey: ['budget'] });
  };

  // 3. Create account mutation
  const createMutation = useMutation({
    mutationFn: accountService.createAccount,
    onSuccess: () => {
      toast.success('Account created successfully');
      invalidateAll();
      setIsAddModalOpen(false);
    },
    onError: (err) => {
      toast.error(err.response?.data?.message || err.userMessage || 'Failed to create account');
    },
  });

  // 4. Update account mutation
  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => accountService.updateAccount(id, data),
    onSuccess: () => {
      toast.success('Account updated successfully');
      invalidateAll();
      setEditingAccount(null);
      setDeletingAccount(null);
    },
    onError: (err) => {
      toast.error(err.response?.data?.message || err.userMessage || 'Failed to update account');
    },
  });

  // 5. Delete account mutation
  const deleteMutation = useMutation({
    mutationFn: accountService.deleteAccount,
    onSuccess: () => {
      toast.success('Account deleted successfully');
      invalidateAll();
      setDeletingAccount(null);
      setDeleteErrorMessage('');
    },
    onError: (err) => {
      const msg = err.response?.data?.message || err.userMessage || 'Failed to delete account';
      setDeleteErrorMessage(msg);
      toast.error(msg);
    },
  });

  // Categorize accounts by type
  const cashAccounts = accounts.filter(
    (a) => a.type === 'CASH' || a.type === 'CHECKING'
  );
  const savingsAccounts = accounts.filter((a) => a.type === 'SAVINGS');
  const creditAccounts = accounts.filter((a) => a.type === 'CREDIT_CARD');
  const otherAccounts = accounts.filter(
    (a) => a.type !== 'CASH' && a.type !== 'CHECKING' && a.type !== 'SAVINGS' && a.type !== 'CREDIT_CARD'
  );

  const handleOpenDelete = (account) => {
    setDeletingAccount(account);
    setDeleteErrorMessage('');
  };

  const handleConfirmDelete = (account) => {
    deleteMutation.mutate(account.id);
  };

  const handleDeactivate = (account) => {
    updateMutation.mutate({
      id: account.id,
      data: { isActive: false },
    });
  };

  const isAnyLoading = isAccountsLoading || isSummaryLoading;

  return (
    <div className="space-y-8">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="font-display font-bold text-2xl text-slate-900">
            Accounts
          </h1>
          <p className="text-xs text-slate-500 mt-1">
            Manage your cash, savings, and credit accounts.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => {
              refetchAccounts();
              refetchSummary();
            }}
            className="p-2.5 rounded-xl border border-slate-200 bg-white hover:bg-slate-50 text-slate-600 transition-all shadow-2xs"
            title="Refresh Accounts"
          >
            <RefreshCw className={`w-4 h-4 ${isAnyLoading ? 'animate-spin text-blue-600' : ''}`} />
          </button>

          <PlaidLinkButton onSuccess={invalidateAll} />

          <button
            onClick={() => setIsAddModalOpen(true)}
            className="flex items-center gap-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-700 active:scale-98 text-white rounded-xl text-xs font-bold shadow-md shadow-blue-500/20 transition-all cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            <span>Add Account</span>
          </button>
        </div>
      </div>

      {/* Account Summary Cards */}
      <AccountSummaryCards summary={summary} loading={isSummaryLoading} />

      {/* Connected Plaid Institutions */}
      <ConnectedInstitutions onSyncComplete={invalidateAll} />

      {/* Grouped Accounts Sections */}
      {isAccountsLoading ? (
        <div className="p-16 bg-white rounded-2xl border border-slate-200/80 text-center text-xs text-slate-400">
          <RefreshCw className="w-6 h-6 animate-spin mx-auto mb-2 text-blue-600" />
          <span>Loading your accounts...</span>
        </div>
      ) : isAccountsError ? (
        <div className="p-16 bg-white rounded-2xl border border-rose-200 text-center space-y-3">
          <div className="w-12 h-12 rounded-2xl bg-rose-50 text-rose-600 flex items-center justify-center mx-auto">
            <AlertCircle className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-800">Unable to load accounts</h3>
            <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
              {accountsError?.response?.data?.message || accountsError?.message || 'A server error occurred while retrieving your accounts.'}
            </p>
          </div>
          <button
            onClick={() => {
              refetchAccounts();
              refetchSummary();
            }}
            className="px-4 py-2 bg-blue-600 text-white rounded-xl text-xs font-bold shadow-xs hover:bg-blue-700 transition-all cursor-pointer"
          >
            Retry
          </button>
        </div>
      ) : accounts.length === 0 ? (
        <div className="p-16 bg-white rounded-2xl border border-dashed border-slate-200 text-center space-y-3">
          <div className="w-12 h-12 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center mx-auto">
            <Landmark className="w-6 h-6" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-800">No accounts yet</h3>
            <p className="text-xs text-slate-400 mt-1 max-w-sm mx-auto">
              Add your cash, checking, savings, or credit card accounts to start tracking your net worth and spending.
            </p>
          </div>
          <button
            onClick={() => setIsAddModalOpen(true)}
            className="px-4 py-2 bg-blue-600 text-white rounded-xl text-xs font-bold shadow-xs hover:bg-blue-700 transition-all"
          >
            Create your first account
          </button>
        </div>
      ) : (
        <div className="space-y-8">
          {/* Cash & Checking */}
          <AccountGroup
            title="Cash & Checking"
            accounts={cashAccounts}
            icon={Wallet}
            onEdit={(acc) => setEditingAccount(acc)}
            onDelete={handleOpenDelete}
          />

          {/* Savings */}
          <AccountGroup
            title="Savings"
            accounts={savingsAccounts}
            icon={PiggyBank}
            onEdit={(acc) => setEditingAccount(acc)}
            onDelete={handleOpenDelete}
          />

          {/* Credit Cards */}
          <AccountGroup
            title="Credit Cards"
            accounts={creditAccounts}
            icon={CreditCard}
            onEdit={(acc) => setEditingAccount(acc)}
            onDelete={handleOpenDelete}
          />

          {/* Other accounts if any */}
          {otherAccounts.length > 0 && (
            <AccountGroup
              title="Other Accounts"
              accounts={otherAccounts}
              icon={Landmark}
              onEdit={(acc) => setEditingAccount(acc)}
              onDelete={handleOpenDelete}
            />
          )}
        </div>
      )}

      {/* Add Account Modal */}
      <AddAccountModal
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        onSave={(data) => createMutation.mutate(data)}
        loading={createMutation.isPending}
      />

      {/* Edit Account Modal */}
      <EditAccountModal
        isOpen={Boolean(editingAccount)}
        account={editingAccount}
        onClose={() => setEditingAccount(null)}
        onSave={(data) => updateMutation.mutate({ id: editingAccount.id, data })}
        loading={updateMutation.isPending}
      />

      {/* Delete / Deactivate Dialog */}
      <DeleteAccountDialog
        isOpen={Boolean(deletingAccount)}
        account={deletingAccount}
        onClose={() => {
          setDeletingAccount(null);
          setDeleteErrorMessage('');
        }}
        onConfirmDelete={handleConfirmDelete}
        onDeactivate={handleDeactivate}
        errorMessage={deleteErrorMessage}
        loading={deleteMutation.isPending || updateMutation.isPending}
      />
    </div>
  );
}
