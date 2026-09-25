import React from 'react';
import { Sparkles, AlertCircle, Info, AlertTriangle, CheckCircle2 } from 'lucide-react';

export default function AiInsightCard({ title, subtitle, summary, insights = [], observations = [], loading = false }) {
  if (loading) {
    return (
      <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-xs animate-pulse space-y-4">
        <div className="h-5 bg-slate-200 rounded w-1/3"></div>
        <div className="h-4 bg-slate-100 rounded w-3/4"></div>
        <div className="space-y-2 pt-2">
          <div className="h-12 bg-slate-50 rounded-xl border border-slate-100"></div>
          <div className="h-12 bg-slate-50 rounded-xl border border-slate-100"></div>
        </div>
      </div>
    );
  }

  const getSeverityBadge = (severity) => {
    switch (severity) {
      case 'WARNING':
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-medium px-2 py-0.5 rounded-full bg-amber-50 text-amber-700 border border-amber-200">
            <AlertTriangle className="w-3 h-3" /> Warning
          </span>
        );
      case 'NOTICE':
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-medium px-2 py-0.5 rounded-full bg-sky-50 text-sky-700 border border-sky-200">
            <AlertCircle className="w-3 h-3" /> Notice
          </span>
        );
      case 'INFO':
      default:
        return (
          <span className="inline-flex items-center gap-1 text-[11px] font-medium px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 border border-emerald-200">
            <Info className="w-3 h-3" /> Info
          </span>
        );
    }
  };

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-xs space-y-5">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div>
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 rounded-lg bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <Sparkles className="w-4 h-4" />
            </div>
            <h3 className="font-semibold text-slate-900 text-base">{title}</h3>
          </div>
          {subtitle && <p className="text-xs text-slate-500 mt-1">{subtitle}</p>}
        </div>
      </div>

      {/* Summary Paragraph */}
      {summary && (
        <div className="p-4 rounded-xl bg-slate-50 border border-slate-200/60 text-sm text-slate-700 leading-relaxed font-sans">
          {summary}
        </div>
      )}

      {/* Insights items */}
      {insights && insights.length > 0 && (
        <div className="space-y-2.5">
          <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Key Observations</h4>
          <div className="space-y-2">
            {insights.map((item, idx) => (
              <div
                key={idx}
                className="p-3 rounded-xl border border-slate-100 bg-white hover:border-slate-200 transition-colors flex items-start justify-between gap-3"
              >
                <div className="space-y-0.5">
                  <div className="text-sm font-semibold text-slate-800">{item.title}</div>
                  <div className="text-xs text-slate-600 leading-relaxed">{item.description}</div>
                </div>
                <div className="shrink-0">{getSeverityBadge(item.severity)}</div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Observations list */}
      {observations && observations.length > 0 && (
        <div className="space-y-2 pt-2 border-t border-slate-100">
          <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Additional Highlights</h4>
          <ul className="space-y-1.5 text-xs text-slate-600">
            {observations.map((obs, idx) => (
              <li key={idx} className="flex items-start gap-2">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600 mt-0.5 shrink-0" />
                <span>{obs}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
