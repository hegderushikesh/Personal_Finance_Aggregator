import React, { useState } from 'react';
import { Landmark, RefreshCw, Unlink, AlertCircle, CheckCircle2, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import {
  usePlaidConnections,
  useSyncPlaidConnection,
  useDisconnectPlaidConnection,
} from '../hooks/usePlaid';

function formatRelativeTime(dateString) {
  if (!dateString) return 'Never';
  const date = new Date(dateString);
  const now = new Date();
  const diffInSeconds = Math.floor((now - date) / 1000);

  if (diffInSeconds < 60) return 'Just now';
  if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds / 60)}m ago`;
  if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds / 3600)}h ago`;
  return `${Math.floor(diffInSeconds / 86400)}d ago`;
}

export default function ConnectedInstitutions({ onSyncComplete }) {
  const { data: connections = [], isLoading } = usePlaidConnections();
  const syncMutation = useSyncPlaidConnection();
  const disconnectMutation = useDisconnectPlaidConnection();

  const [syncingId, setSyncingId] = useState(null);
  const [disconnectingId, setDisconnectingId] = useState(null);

  const handleSync = async (connectionId) => {
    setSyncingId(connectionId);
    try {
      const res = await syncMutation.mutateAsync(connectionId);
      toast.success(
        `Sync completed: ${res.added} added, ${res.modified} modified, ${res.removed} removed.`
      );
      if (onSyncComplete) onSyncComplete();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to sync bank connection.');
    } finally {
      setSyncingId(null);
    }
  };

  const handleDisconnect = async (connectionId, name) => {
    if (!window.confirm(`Are you sure you want to disconnect ${name}? Your existing transactions will be preserved.`)) {
      return;
    }

    setDisconnectingId(connectionId);
    try {
      await disconnectMutation.mutateAsync(connectionId);
      toast.success(`Disconnected ${name}.`);
    } catch (err) {
      toast.error('Failed to disconnect bank.');
    } finally {
      setDisconnectingId(null);
    }
  };

  if (isLoading || connections.length === 0) {
    return null;
  }

  return (
    <div className="bg-white p-5 sm:p-6 rounded-2xl border border-slate-200/80 shadow-xs mb-8">
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2.5">
          <div className="w-8 h-8 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
            <Landmark className="w-4 h-4" />
          </div>
          <div>
            <h3 className="text-sm font-bold text-slate-900 font-display">
              Connected Institutions
            </h3>
            <p className="text-xs text-slate-500">
              Live Plaid Sandbox banking integrations
            </p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {connections.map((conn) => {
          const isSyncing = syncingId === conn.id;
          const isDisconnecting = disconnectingId === conn.id;
          const isActionRequired = conn.status === 'LOGIN_REQUIRED';

          return (
            <div
              key={conn.id}
              className="p-4 rounded-xl bg-slate-50/70 border border-slate-200/80 flex flex-col justify-between space-y-3"
            >
              <div>
                <div className="flex items-center justify-between">
                  <span className="font-bold text-sm text-slate-800 truncate">
                    {conn.institutionName}
                  </span>
                  <span
                    className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-md text-[10px] font-semibold ${
                      conn.status === 'ACTIVE'
                        ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                        : conn.status === 'LOGIN_REQUIRED'
                        ? 'bg-amber-50 text-amber-700 border border-amber-200'
                        : 'bg-slate-100 text-slate-600'
                    }`}
                  >
                    {conn.status === 'ACTIVE' ? (
                      <>
                        <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                        <span>Connected</span>
                      </>
                    ) : conn.status === 'LOGIN_REQUIRED' ? (
                      <>
                        <AlertCircle className="w-3 h-3 text-amber-600" />
                        <span>Action Required</span>
                      </>
                    ) : (
                      <span>{conn.status}</span>
                    )}
                  </span>
                </div>

                <div className="mt-2 text-xs text-slate-500 space-y-1">
                  <p>
                    <span className="font-medium text-slate-700">{conn.accountCount}</span> account
                    {conn.accountCount !== 1 ? 's' : ''} linked
                  </p>
                  <p className="text-[11px] text-slate-400">
                    Last synced: {formatRelativeTime(conn.lastSyncedAt)}
                  </p>
                </div>
              </div>

              <div className="flex items-center justify-between gap-2 pt-2 border-t border-slate-200/60">
                <button
                  type="button"
                  onClick={() => handleSync(conn.id)}
                  disabled={isSyncing || conn.status !== 'ACTIVE'}
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold bg-white border border-slate-200 text-slate-700 hover:bg-slate-50 disabled:opacity-50 transition-colors shadow-2xs"
                >
                  <RefreshCw className={`w-3.5 h-3.5 ${isSyncing ? 'animate-spin text-blue-600' : 'text-slate-500'}`} />
                  <span>{isSyncing ? 'Syncing...' : 'Sync'}</span>
                </button>

                <button
                  type="button"
                  onClick={() => handleDisconnect(conn.id, conn.institutionName)}
                  disabled={isDisconnecting}
                  className="inline-flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-medium text-rose-600 hover:bg-rose-50 hover:text-rose-700 transition-colors"
                >
                  {isDisconnecting ? (
                    <Loader2 className="w-3.5 h-3.5 animate-spin" />
                  ) : (
                    <Unlink className="w-3.5 h-3.5" />
                  )}
                  <span>Disconnect</span>
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
