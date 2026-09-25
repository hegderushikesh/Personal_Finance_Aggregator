import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronDown, ChevronRight, Target, Plus, FolderPlus, Tag } from 'lucide-react';

export default function CategoryTable({
  groups = [],
  selectedCategory,
  onSelectCategory,
  onAssignMoney,
  onToggleGroupCollapse,
  activeFilter = 'ALL',
  searchQuery = '',
  onOpenNewCategory,
  year,
  month,
}) {
  const navigate = useNavigate();
  const [editingCategoryId, setEditingCategoryId] = useState(null);
  const [editingValue, setEditingValue] = useState('');

  const handleStartEdit = (e, cat) => {
    e.stopPropagation();
    setEditingCategoryId(cat.categoryId);
    setEditingValue(cat.assigned ? cat.assigned.toString() : '0');
  };

  const handleSaveEdit = (cat) => {
    const val = parseFloat(editingValue);
    if (!isNaN(val) && val >= 0) {
      onAssignMoney(cat.categoryId, val);
    }
    setEditingCategoryId(null);
  };

  const handleKeyDown = (e, cat) => {
    if (e.key === 'Enter') {
      handleSaveEdit(cat);
    } else if (e.key === 'Escape') {
      setEditingCategoryId(null);
    }
  };

  // Filter groups and categories based on search & filter tabs
  const filteredGroups = groups
    .map((group) => {
      const filteredCategories = (group.categories || []).filter((cat) => {
        // Search Filter
        if (searchQuery.trim()) {
          const query = searchQuery.toLowerCase();
          const matchName = cat.categoryName.toLowerCase().includes(query);
          const matchGroup = group.name.toLowerCase().includes(query);
          if (!matchName && !matchGroup) return false;
        }

        // Tab Filters
        if (activeFilter === 'UNDERFUNDED') {
          return cat.status === 'UNDERFUNDED' || (cat.target && cat.available < cat.target.monthlyRequired);
        }
        if (activeFilter === 'OVERFUNDED') {
          return cat.status === 'OVERFUNDED' || (cat.target && cat.available > cat.target.amount);
        }
        if (activeFilter === 'MONEY_AVAILABLE') {
          return (cat.available || 0) > 0;
        }
        if (activeFilter === 'SNOOZED') {
          return cat.target && cat.target.snoozed;
        }
        return true;
      });

      return {
        ...group,
        categories: filteredCategories,
      };
    })
    .filter((group) => group.categories.length > 0 || !searchQuery.trim());

  return (
    <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
      {/* Table Header */}
      <div className="grid grid-cols-12 gap-2 px-6 py-3 bg-slate-50/80 border-b border-slate-200/80 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
        <div className="col-span-5 sm:col-span-5">Category</div>
        <div className="col-span-2 sm:col-span-2 text-right">Assigned</div>
        <div className="col-span-2 sm:col-span-2 text-right">Activity</div>
        <div className="col-span-3 sm:col-span-3 text-right">Available</div>
      </div>

      {/* Groups & Category Rows */}
      <div className="divide-y divide-slate-100">
        {filteredGroups.length === 0 ? (
          <div className="p-12 text-center text-slate-400 text-xs">
            No categories match your search or filter.
          </div>
        ) : (
          filteredGroups.map((group) => {
            const isCollapsed = Boolean(group.isCollapsed);
            return (
              <div key={group.id} className="group-wrapper">
                {/* Category Group Header */}
                <div
                  onClick={() => onToggleGroupCollapse(group.id)}
                  className="grid grid-cols-12 gap-2 px-6 py-3.5 bg-slate-50/40 hover:bg-slate-50 cursor-pointer transition-all border-b border-slate-100/80 items-center select-none"
                >
                  <div className="col-span-5 sm:col-span-5 flex items-center gap-2">
                    <button className="text-slate-400 hover:text-slate-600 p-0.5">
                      {isCollapsed ? (
                        <ChevronRight className="w-4 h-4" />
                      ) : (
                        <ChevronDown className="w-4 h-4" />
                      )}
                    </button>
                    <span className="font-display font-bold text-sm text-slate-800">
                      {group.name}
                    </span>
                    <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-slate-200/60 text-slate-600">
                      {group.categories.length}
                    </span>
                  </div>

                  <div className="col-span-2 sm:col-span-2 text-right text-xs font-bold text-slate-700">
                    ₹{(group.assigned || 0).toLocaleString()}
                  </div>
                  <div className="col-span-2 sm:col-span-2 text-right text-xs font-bold text-slate-500">
                    ₹{(group.activity || 0).toLocaleString()}
                  </div>
                  <div className="col-span-3 sm:col-span-3 text-right text-xs font-bold text-slate-900">
                    ₹{(group.available || 0).toLocaleString()}
                  </div>
                </div>

                {/* Category Rows inside Group */}
                {!isCollapsed && (
                  <div className="divide-y divide-slate-100/60">
                    {group.categories.map((cat) => {
                      const isSelected = selectedCategory?.categoryId === cat.categoryId;
                      const availableVal = cat.available || 0;
                      const assignedVal = cat.assigned || 0;
                      const activityVal = cat.activity || 0;

                      // Available status pill color
                      let pillClass = 'bg-slate-100 text-slate-700 border-slate-200';
                      if (cat.status === 'UNDERFUNDED') {
                        pillClass = 'bg-amber-50 text-amber-800 border-amber-300';
                      } else if (availableVal > 0) {
                        pillClass = 'bg-emerald-50 text-emerald-800 border-emerald-300';
                      } else if (availableVal < 0) {
                        pillClass = 'bg-rose-50 text-rose-800 border-rose-300';
                      }

                      // Progress percentage calculation
                      let progressPct = 0;
                      if (cat.target && cat.target.amount > 0) {
                        progressPct = cat.target.progressPercentage || 0;
                      } else if (assignedVal > 0) {
                        progressPct = Math.min(100, Math.round((activityVal / assignedVal) * 100));
                      }

                      return (
                        <div
                          key={cat.categoryId}
                          onClick={() => onSelectCategory(cat)}
                          className={`grid grid-cols-12 gap-2 px-6 py-3 items-center cursor-pointer transition-all hover:bg-blue-50/30 ${
                            isSelected ? 'bg-blue-50/70 border-l-4 border-blue-600' : 'pl-8'
                          }`}
                        >
                          {/* Category Name & Progress Bar */}
                          <div className="col-span-5 sm:col-span-5 flex flex-col min-w-0 pr-2">
                            <div className="flex items-center gap-2">
                              <span className="font-semibold text-xs text-slate-800 truncate">
                                {cat.categoryName}
                              </span>
                              {cat.target && (
                                <Target className="w-3 h-3 text-indigo-500 shrink-0" title="Category has target" />
                              )}
                            </div>

                            {/* Progress bar */}
                            <div className="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden mt-1.5 max-w-[180px]">
                              <div
                                className={`h-full rounded-full transition-all duration-300 ${
                                  cat.status === 'UNDERFUNDED'
                                    ? 'bg-amber-500'
                                    : availableVal < 0
                                    ? 'bg-rose-500'
                                    : 'bg-emerald-500'
                                }`}
                                style={{ width: `${Math.min(100, Math.max(0, progressPct))}%` }}
                              />
                            </div>
                          </div>

                          {/* Assigned Amount Cell (Inline Editable) */}
                          <div
                            className="col-span-2 sm:col-span-2 text-right"
                            onClick={(e) => handleStartEdit(e, cat)}
                          >
                            {editingCategoryId === cat.categoryId ? (
                              <input
                                type="number"
                                step="0.01"
                                value={editingValue}
                                onChange={(e) => setEditingValue(e.target.value)}
                                onBlur={() => handleSaveEdit(cat)}
                                onKeyDown={(e) => handleKeyDown(e, cat)}
                                className="w-20 h-7 text-right px-1.5 rounded border border-blue-500 text-xs font-bold bg-white outline-none"
                                autoFocus
                              />
                            ) : (
                              <span className="text-xs font-bold text-slate-700 hover:text-blue-600 hover:underline px-1 py-0.5 rounded">
                                ₹{assignedVal.toLocaleString()}
                              </span>
                            )}
                          </div>

                          {/* Activity Cell (Navigates to filtered transactions) */}
                          <div
                            onClick={(e) => {
                              e.stopPropagation();
                              const query = new URLSearchParams();
                              query.set('categoryId', cat.categoryId);
                              if (month) query.set('month', month);
                              if (year) query.set('year', year);
                              navigate(`/transactions?${query.toString()}`);
                            }}
                            className="col-span-2 sm:col-span-2 text-right text-xs font-medium text-slate-500 hover:text-blue-600 hover:underline cursor-pointer select-none transition-colors"
                            title={`View ${cat.categoryName} transactions`}
                          >
                            {activityVal > 0 ? `-₹${activityVal.toLocaleString()}` : '₹0'}
                          </div>

                          {/* Available Cell Pill */}
                          <div className="col-span-3 sm:col-span-3 flex justify-end">
                            <span
                              className={`px-3 py-1 rounded-full text-xs font-bold border shadow-2xs ${pillClass}`}
                            >
                              ₹{availableVal.toLocaleString()}
                            </span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}
