import React from 'react';
import AccountCard from './AccountCard';

export default function AccountGroup({ title, accounts = [], icon: Icon, onEdit, onDelete }) {
  if (!accounts || accounts.length === 0) return null;

  const totalBalance = accounts
    .filter((a) => a.isActive)
    .reduce((sum, a) => sum + (parseFloat(a.balance) || 0), 0);

  return (
    <section className="space-y-3">
      {/* Group Header */}
      <div className="flex items-center justify-between px-1">
        <div className="flex items-center gap-2">
          {Icon && <Icon className="w-4 h-4 text-slate-500" />}
          <h3 className="font-display font-bold text-sm text-slate-800 uppercase tracking-wider">
            {title}
          </h3>
          <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-200/70 text-slate-600">
            {accounts.length}
          </span>
        </div>

        <div className="text-xs font-semibold text-slate-500">
          Subtotal:{' '}
          <span className="font-bold text-slate-900">
            ₹{totalBalance.toLocaleString()}
          </span>
        </div>
      </div>

      {/* Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {accounts.map((acc) => (
          <AccountCard
            key={acc.id}
            account={acc}
            onEdit={onEdit}
            onDelete={onDelete}
          />
        ))}
      </div>
    </section>
  );
}
