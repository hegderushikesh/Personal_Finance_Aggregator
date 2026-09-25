import React, { useState } from 'react';
import { Calendar, ChevronDown } from 'lucide-react';

export const PRESETS = [
  { id: 'THIS_MONTH', label: 'This Month' },
  { id: 'LAST_MONTH', label: 'Last Month' },
  { id: 'LAST_3_MONTHS', label: 'Last 3 Months' },
  { id: 'LAST_6_MONTHS', label: 'Last 6 Months' },
  { id: 'THIS_YEAR', label: 'This Year' },
  { id: 'CUSTOM', label: 'Custom Range' },
];

function formatDate(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

export function computeRangeFromPreset(presetId) {
  const now = new Date();
  const y = now.getFullYear();
  const m = now.getMonth();

  switch (presetId) {
    case 'THIS_MONTH': {
      const start = new Date(y, m, 1);
      const end = new Date(y, m + 1, 0);
      return { startDate: formatDate(start), endDate: formatDate(end) };
    }
    case 'LAST_MONTH': {
      const start = new Date(y, m - 1, 1);
      const end = new Date(y, m, 0);
      return { startDate: formatDate(start), endDate: formatDate(end) };
    }
    case 'LAST_3_MONTHS': {
      const start = new Date(y, m - 2, 1);
      const end = new Date(y, m + 1, 0);
      return { startDate: formatDate(start), endDate: formatDate(end) };
    }
    case 'LAST_6_MONTHS': {
      const start = new Date(y, m - 5, 1);
      const end = new Date(y, m + 1, 0);
      return { startDate: formatDate(start), endDate: formatDate(end) };
    }
    case 'THIS_YEAR': {
      const start = new Date(y, 0, 1);
      const end = new Date(y, 11, 31);
      return { startDate: formatDate(start), endDate: formatDate(end) };
    }
    default:
      return null;
  }
}

export default function DateRangeSelector({ range, onRangeChange }) {
  const [selectedPreset, setSelectedPreset] = useState('THIS_MONTH');
  const [customStart, setCustomStart] = useState(range.startDate);
  const [customEnd, setCustomEnd] = useState(range.endDate);

  const handlePresetSelect = (presetId) => {
    setSelectedPreset(presetId);
    if (presetId !== 'CUSTOM') {
      const calculated = computeRangeFromPreset(presetId);
      if (calculated) {
        onRangeChange(calculated);
      }
    }
  };

  const handleApplyCustom = () => {
    if (customStart && customEnd) {
      onRangeChange({ startDate: customStart, endDate: customEnd });
    }
  };

  return (
    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 bg-white p-3 sm:p-4 rounded-2xl border border-slate-200/80 shadow-xs">
      <div className="flex items-center gap-2 text-slate-700 font-semibold text-sm">
        <Calendar className="w-4 h-4 text-blue-600" />
        <span>Date Range:</span>
      </div>

      <div className="flex flex-wrap items-center gap-2">
        <div className="flex bg-slate-100/80 p-1 rounded-xl gap-1">
          {PRESETS.map((preset) => {
            const isActive = selectedPreset === preset.id;
            return (
              <button
                key={preset.id}
                type="button"
                onClick={() => handlePresetSelect(preset.id)}
                className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-xs'
                    : 'text-slate-600 hover:text-slate-900 hover:bg-slate-200/60'
                }`}
              >
                {preset.label}
              </button>
            );
          })}
        </div>

        {selectedPreset === 'CUSTOM' && (
          <div className="flex items-center gap-2 mt-2 sm:mt-0">
            <input
              type="date"
              value={customStart}
              onChange={(e) => setCustomStart(e.target.value)}
              className="h-8 px-2.5 rounded-lg text-xs bg-slate-50 border border-slate-300 text-slate-700 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
            <span className="text-xs text-slate-400">to</span>
            <input
              type="date"
              value={customEnd}
              onChange={(e) => setCustomEnd(e.target.value)}
              className="h-8 px-2.5 rounded-lg text-xs bg-slate-50 border border-slate-300 text-slate-700 focus:outline-none focus:ring-1 focus:ring-blue-500"
            />
            <button
              type="button"
              onClick={handleApplyCustom}
              className="h-8 px-3 rounded-lg text-xs font-semibold bg-[#0A1628] text-white hover:bg-slate-800 transition-colors"
            >
              Apply
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
