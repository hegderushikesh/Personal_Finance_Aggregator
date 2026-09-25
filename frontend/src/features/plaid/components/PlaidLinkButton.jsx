import React, { useState, useCallback, useEffect } from 'react';
import { usePlaidLink } from 'react-plaid-link';
import { Landmark, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { useCreateLinkToken, useExchangePublicToken } from '../hooks/usePlaid';

function PlaidLinkLauncher({ token, onSuccess, onExit }) {
  const { open, ready } = usePlaidLink({
    token,
    onSuccess,
    onExit,
  });

  useEffect(() => {
    if (ready) {
      open();
    }
  }, [ready, open]);

  return null;
}

export default function PlaidLinkButton({ className = '', onComplete }) {
  const [linkToken, setLinkToken] = useState(null);
  const [isInitializing, setIsInitializing] = useState(false);

  const createLinkTokenMutation = useCreateLinkToken();
  const exchangeTokenMutation = useExchangePublicToken();

  const handleSuccess = useCallback(
    async (publicToken, metadata) => {
      setLinkToken(null);
      setIsInitializing(false);

      try {
        const institution = metadata?.institution;
        const res = await exchangeTokenMutation.mutateAsync({
          publicToken,
          institutionId: institution?.institution_id,
          institutionName: institution?.name,
        });

        toast.success(
          `Connected to ${res.institutionName || 'bank'} successfully! Synced ${res.accountsCount} accounts and ${res.transactionsCount} transactions.`
        );

        if (onComplete) {
          onComplete();
        }
      } catch (err) {
        toast.error('Failed to complete bank synchronization. Please try again.');
      }
    },
    [exchangeTokenMutation, onComplete]
  );

  const handleExit = useCallback((error, metadata) => {
    setLinkToken(null);
    setIsInitializing(false);
    if (error) {
      toast.error(error.display_message || 'Bank connection exited.');
    }
  }, []);

  const handleConnectClick = async () => {
    setIsInitializing(true);
    try {
      const res = await createLinkTokenMutation.mutateAsync();
      setLinkToken(res.linkToken);
    } catch (err) {
      setIsInitializing(false);
      toast.error('Unable to start bank connection. Please check Plaid configuration.');
    }
  };

  const isLoading =
    isInitializing ||
    createLinkTokenMutation.isPending ||
    exchangeTokenMutation.isPending;

  return (
    <>
      <button
        type="button"
        onClick={handleConnectClick}
        disabled={isLoading}
        className={`inline-flex items-center gap-2 px-4 py-2.5 rounded-xl font-semibold text-xs transition-all shadow-xs ${
          isLoading
            ? 'bg-blue-100 text-blue-400 cursor-not-allowed'
            : 'bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white active:scale-95 shadow-blue-500/20'
        } ${className}`}
      >
        {isLoading ? (
          <Loader2 className="w-4 h-4 animate-spin" />
        ) : (
          <Landmark className="w-4 h-4" />
        )}
        <span>{isLoading ? 'Connecting...' : '+ Connect Bank'}</span>
      </button>

      {linkToken && (
        <PlaidLinkLauncher
          token={linkToken}
          onSuccess={handleSuccess}
          onExit={handleExit}
        />
      )}
    </>
  );
}
