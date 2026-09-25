import React from 'react';
import { Search } from 'lucide-react';

export default function BudgetFilterBar({ activeFilter, onFilterChange, searchQuery, onSearchChange }) {
  const filters = [
    { id: 'ALL', label: 'All' },
    { id: 'UNDERFUNDED', label: 'Underfunded' },
    { id: 'OVERFUNDED', label: 'Overfunded' },
    { id: 'MONEY_AVAILABLE', label: 'Money Available' },
    { id: 'SNOOZED', label: 'Snoozed' },
  ];

  return (
    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-2 pb-1">
      {/* Filter Tabs */}
      <div className="flex items-center gap-1 overflow-x-auto pb-1 sm:pb-0">
        {filters.map((f) => (
          <button
            key={f.id}
            onClick={() => onFilterChange(f.id)}
            className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all ${
              activeFilter === f.id
                ? 'bg-slate-900 text-white shadow-xs'
                : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      {/* Instant Search Bar */}
      <div className="relative w-full sm:w-64">
        <Search className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => onSearchChange(e.target.value)}
          placeholder="Search category..."
          className="w-full h-9 pl-9 pr-3 rounded-xl text-xs bg-white border border-slate-200 focus:border-blue-500 focus:outline-none transition-all placeholder:text-slate-400 font-medium"
        />
      </div>
    </div>
  );
}
