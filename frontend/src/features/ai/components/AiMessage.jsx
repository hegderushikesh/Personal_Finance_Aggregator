import React from 'react';
import { Bot, User, Sparkles, AlertCircle, Info, AlertTriangle } from 'lucide-react';

export default function AiMessage({ message, isUser, timestamp = 'Just now', model, insights = [] }) {
  const getSeverityIcon = (severity) => {
    switch (severity) {
      case 'WARNING':
        return <AlertTriangle className="w-4 h-4 text-amber-500 shrink-0" />;
      case 'NOTICE':
        return <AlertCircle className="w-4 h-4 text-sky-500 shrink-0" />;
      case 'INFO':
      default:
        return <Info className="w-4 h-4 text-emerald-500 shrink-0" />;
    }
  };

  const getSeverityBadgeClass = (severity) => {
    switch (severity) {
      case 'WARNING':
        return 'bg-amber-50 border-amber-200 text-amber-800';
      case 'NOTICE':
        return 'bg-sky-50 border-sky-200 text-sky-800';
      case 'INFO':
      default:
        return 'bg-emerald-50 border-emerald-200 text-emerald-800';
    }
  };

  return (
    <div className={`flex gap-3.5 my-3 ${isUser ? 'flex-row-reverse' : 'flex-row'}`}>
      {/* Avatar */}
      <div
        className={`w-8 h-8 rounded-full flex items-center justify-center shrink-0 text-white font-medium text-xs shadow-xs ${
          isUser
            ? 'bg-slate-700'
            : 'bg-gradient-to-tr from-[#0F172A] via-[#1E293B] to-[#334155] border border-slate-600'
        }`}
      >
        {isUser ? <User className="w-4 h-4 text-slate-200" /> : <Bot className="w-4 h-4 text-emerald-400" />}
      </div>

      {/* Bubble Container */}
      <div className={`max-w-[82%] sm:max-w-[75%] space-y-2`}>
        <div
          className={`p-4 rounded-2xl text-sm leading-relaxed ${
            isUser
              ? 'bg-[#0F172A] text-white rounded-tr-xs shadow-xs'
              : 'bg-white border border-slate-200 text-slate-800 rounded-tl-xs shadow-xs'
          }`}
        >
          {/* Header info */}
          {!isUser && (
            <div className="flex items-center gap-2 mb-2 pb-1.5 border-b border-slate-100 text-[11px] text-slate-400 font-medium">
              <span className="flex items-center gap-1 text-emerald-600 font-semibold">
                <Sparkles className="w-3 h-3" /> FinPilot AI
              </span>
              <span>•</span>
              <span className="font-mono text-[10px] bg-slate-100 px-1.5 py-0.5 rounded text-slate-600">
                {model || 'qwen2.5-coder:7b'}
              </span>
              <span className="ml-auto text-slate-400">{timestamp}</span>
            </div>
          )}

          {/* Body Content */}
          <div className="whitespace-pre-wrap font-sans text-slate-700 leading-normal">
            {message}
          </div>

          {/* Embedded Insights */}
          {insights && insights.length > 0 && (
            <div className="mt-3 pt-3 border-t border-slate-100 space-y-2">
              {insights.map((insight, idx) => (
                <div
                  key={idx}
                  className={`p-2.5 rounded-lg border text-xs flex items-start gap-2 ${getSeverityBadgeClass(
                    insight.severity
                  )}`}
                >
                  {getSeverityIcon(insight.severity)}
                  <div>
                    <div className="font-semibold">{insight.title}</div>
                    <div className="text-slate-600 mt-0.5">{insight.description}</div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* User timestamp */}
        {isUser && (
          <div className="text-[11px] text-slate-400 text-right pr-1">
            {timestamp}
          </div>
        )}
      </div>
    </div>
  );
}
