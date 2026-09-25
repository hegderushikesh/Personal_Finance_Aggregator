import React from 'react';
import { Sparkles, Check, ArrowRight } from 'lucide-react';

export default function OnboardingModal({ isOpen, onClose, onUseDefaultTemplate }) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/60 backdrop-blur-xs p-4">
      <div className="bg-white rounded-3xl shadow-2xl border border-slate-200 w-full max-w-lg overflow-hidden animate-in fade-in zoom-in duration-300">
        <div className="bg-gradient-to-br from-[#0A1628] to-[#1E293B] p-8 text-white text-center relative">
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-tr from-blue-600 to-indigo-500 mx-auto flex items-center justify-center shadow-lg mb-4">
            <Sparkles className="w-7 h-7 text-white" />
          </div>
          <h2 className="font-display font-black text-2xl tracking-tight">Welcome to FinPilot Planning</h2>
          <p className="text-slate-300 text-xs mt-2 max-w-xs mx-auto">
            "Give every rupee a purpose." Modern zero-based monthly planning for your financial independence.
          </p>
        </div>

        <div className="p-8 space-y-6">
          <div className="space-y-3">
            {[
              '1. Start with your available cash or income',
              '2. Organize into Bills, Needs, Wants, and Savings',
              '3. Assign funds and set smart targets',
              '4. Track spending activity and adjust anytime',
            ].map((step, idx) => (
              <div key={idx} className="flex items-center gap-3 text-xs font-semibold text-slate-700">
                <div className="w-5 h-5 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center shrink-0 font-bold text-[10px]">
                  ✓
                </div>
                <span>{step}</span>
              </div>
            ))}
          </div>

          <div className="space-y-3 pt-2">
            <button
              onClick={onUseDefaultTemplate}
              className="w-full h-12 rounded-xl text-xs font-bold text-white bg-blue-600 hover:bg-blue-700 shadow-md transition-all flex items-center justify-center gap-2 group"
            >
              <span>Use FinPilot Default Category Template</span>
              <ArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
            </button>

            <button
              onClick={onClose}
              className="w-full h-10 rounded-xl text-xs font-semibold text-slate-500 hover:text-slate-700 hover:bg-slate-100 transition-all"
            >
              Start From Scratch
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
