import React from 'react';
import { X, RotateCcw, Activity } from 'lucide-react';

export default function RecentMovesDrawer({ isOpen, onClose, activities = [], onUndo }) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-hidden bg-slate-900/40 backdrop-blur-xs">
      <div className="absolute inset-y-0 right-0 max-w-full flex pl-10">
        <div className="w-screen max-w-md bg-white shadow-2xl border-l border-slate-200 flex flex-col">
          {/* Header */}
          <div className="p-5 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center font-bold">
                <Activity className="w-4 h-4" />
              </div>
              <h3 className="font-display font-bold text-base text-slate-800">Recent Budget Activities</h3>
            </div>
            <button onClick={onClose} className="text-slate-400 hover:text-slate-600 p-1 rounded-lg hover:bg-slate-100">
              <X className="w-5 h-5" />
            </button>
          </div>

          {/* Undo Action Bar */}
          <div className="p-4 bg-slate-100/70 border-b border-slate-200/60 flex items-center justify-between">
            <span className="text-xs font-semibold text-slate-600">Quick Reversal</span>
            <button
              onClick={onUndo}
              disabled={activities.length === 0}
              className="px-3 py-1.5 rounded-lg text-xs font-bold text-rose-600 bg-rose-50 hover:bg-rose-100 border border-rose-200 transition-all flex items-center gap-1.5 disabled:opacity-50"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>Undo Last Move</span>
            </button>
          </div>

          {/* Activity List */}
          <div className="flex-1 overflow-y-auto p-5 space-y-3">
            {activities.length === 0 ? (
              <div className="text-center py-12 text-slate-400 text-xs">
                No recent moves or budget changes recorded for this month.
              </div>
            ) : (
              activities.map((act) => (
                <div key={act.id} className="p-3.5 rounded-xl border border-slate-200/80 bg-white space-y-1 shadow-2xs">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-bold uppercase tracking-wider px-2 py-0.5 rounded-md bg-blue-50 text-blue-600 border border-blue-100">
                      {act.type}
                    </span>
                    <span className="text-[10px] text-slate-400 font-medium">
                      {act.createdAt ? new Date(act.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : ''}
                    </span>
                  </div>
                  <p className="text-xs font-semibold text-slate-800 pt-1">{act.description}</p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
