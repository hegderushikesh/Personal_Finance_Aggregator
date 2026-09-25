import React from 'react';
import { Sparkles, TrendingDown, PieChart, Repeat, PiggyBank, FileText } from 'lucide-react';

const DEFAULT_QUESTIONS = [
  { text: 'Where did I spend the most?', icon: TrendingDown },
  { text: 'How is my budget?', icon: PieChart },
  { text: 'What are my recurring payments?', icon: Repeat },
  { text: 'How much did I save this month?', icon: PiggyBank },
  { text: 'Give me a summary of this month.', icon: FileText },
];

export default function QuickQuestions({ onSelectQuestion, disabled = false }) {
  return (
    <div className="space-y-2">
      <div className="flex items-center gap-1.5 text-xs font-medium text-slate-500">
        <Sparkles className="w-3.5 h-3.5 text-emerald-600" />
        <span>Quick questions</span>
      </div>
      <div className="flex flex-wrap gap-2">
        {DEFAULT_QUESTIONS.map((q, idx) => {
          const Icon = q.icon;
          return (
            <button
              key={idx}
              type="button"
              disabled={disabled}
              onClick={() => onSelectQuestion(q.text)}
              className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-medium bg-slate-100 hover:bg-slate-200 text-slate-700 transition-colors border border-slate-200/80 disabled:opacity-50 disabled:cursor-not-allowed text-left cursor-pointer"
            >
              <Icon className="w-3.5 h-3.5 text-slate-500 shrink-0" />
              <span>{q.text}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
}
