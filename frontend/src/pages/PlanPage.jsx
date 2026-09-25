import React, { useState, useEffect, useCallback } from 'react';
import BudgetHeader from '../components/budget/BudgetHeader';
import BudgetFilterBar from '../components/budget/BudgetFilterBar';
import CategoryTable from '../components/budget/CategoryTable';
import CategoryDetailPanel from '../components/budget/CategoryDetailPanel';
import StartingBalanceModal from '../components/budget/StartingBalanceModal';
import MoveMoneyModal from '../components/budget/MoveMoneyModal';
import TargetModal from '../components/budget/TargetModal';
import AutoAssignModal from '../components/budget/AutoAssignModal';
import RecentMovesDrawer from '../components/budget/RecentMovesDrawer';
import CategoryGroupModal from '../components/budget/CategoryGroupModal';
import CategoryModal from '../components/budget/CategoryModal';
import OnboardingModal from '../components/budget/OnboardingModal';
import { budgetService } from '../services/budgetService';
import { categoryService } from '../services/categoryService';
import { targetService } from '../services/targetService';
import { Loader2 } from 'lucide-react';

export default function PlanPage() {
  const now = new Date();
  const [year, setYear] = useState(now.getFullYear());
  const [month, setMonth] = useState(now.getMonth() + 1);

  const [budgetData, setBudgetData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedCategory, setSelectedCategory] = useState(null);

  // Filters & Search
  const [activeFilter, setActiveFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');

  // Modals & Drawers state
  const [isStartingBalanceOpen, setIsStartingBalanceOpen] = useState(false);
  const [isMoveMoneyOpen, setIsMoveMoneyOpen] = useState(false);
  const [isTargetModalOpen, setIsTargetModalOpen] = useState(false);
  const [targetCategory, setTargetCategory] = useState(null);
  const [currentTarget, setCurrentTarget] = useState(null);
  const [isAutoAssignOpen, setIsAutoAssignOpen] = useState(false);
  const [isRecentMovesOpen, setIsRecentMovesOpen] = useState(false);
  const [isGroupModalOpen, setIsGroupModalOpen] = useState(false);
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false);
  const [editingGroup, setEditingGroup] = useState(null);
  const [editingCategoryItem, setEditingCategoryItem] = useState(null);
  const [isOnboardingOpen, setIsOnboardingOpen] = useState(false);

  // Load Budget Data for selected year and month
  const loadBudget = useCallback(async () => {
    setLoading(true);
    try {
      const data = await budgetService.getBudget(year, month);
      setBudgetData(data);
      if (!data.groups || data.groups.length === 0) {
        setIsOnboardingOpen(true);
      }
    } catch (err) {
      console.error('Failed to load budget data:', err);
    } finally {
      setLoading(false);
    }
  }, [year, month]);

  useEffect(() => {
    loadBudget();
  }, [loadBudget]);

  // Month Navigation
  const handlePrevMonth = () => {
    if (month === 1) {
      setYear((y) => y - 1);
      setMonth(12);
    } else {
      setMonth((m) => m - 1);
    }
  };

  const handleNextMonth = () => {
    if (month === 12) {
      setYear((y) => y + 1);
      setMonth(1);
    } else {
      setMonth((m) => m + 1);
    }
  };

  const handleToday = () => {
    const today = new Date();
    setYear(today.getFullYear());
    setMonth(today.getMonth() + 1);
  };

  // Actions
  const handleAssignMoney = async (categoryId, amount) => {
    if (!budgetData?.budgetId) return;
    try {
      const updated = await budgetService.assignMoney(budgetData.budgetId, categoryId, amount);
      setBudgetData(updated);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to assign money');
    }
  };

  const handleMoveMoney = async (fromCatId, toCatId, amount) => {
    if (!budgetData?.budgetId) return;
    try {
      const updated = await budgetService.moveMoney(budgetData.budgetId, fromCatId, toCatId, amount);
      setBudgetData(updated);
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to move money');
    }
  };

  const handleSaveStartingBalance = async (balance) => {
    if (!budgetData?.budgetId) return;
    try {
      const updated = await budgetService.setStartingBalance(budgetData.budgetId, balance);
      setBudgetData(updated);
    } catch (err) {
      alert('Failed to update starting balance');
    }
  };

  const handleSaveTarget = async (payload) => {
    try {
      await targetService.createOrUpdateTarget(payload);
      loadBudget();
    } catch (err) {
      alert('Failed to save target goal');
    }
  };

  const handleApplyAutoAssign = async (mode) => {
    if (!budgetData?.budgetId) return;
    try {
      const updated = await budgetService.autoAssignApply(budgetData.budgetId, mode);
      setBudgetData(updated);
    } catch (err) {
      alert('Failed to apply auto-assign strategy');
    }
  };

  const handleCopyPreviousMonth = async () => {
    if (!budgetData?.budgetId) return;
    try {
      const updated = await budgetService.copyPreviousMonth(budgetData.budgetId);
      setBudgetData(updated);
    } catch (err) {
      alert(err.response?.data?.message || 'No previous month data found to copy');
    }
  };

  const handleUndo = async () => {
    if (!budgetData?.budgetId) return;
    try {
      const updated = await budgetService.undoLastActivity(budgetData.budgetId);
      setBudgetData(updated);
    } catch (err) {
      alert('Nothing left to undo');
    }
  };

  const handleToggleGroupCollapse = async (groupId) => {
    try {
      await categoryService.toggleCollapseGroup(groupId);
      setBudgetData((prev) => {
        if (!prev) return prev;
        return {
          ...prev,
          groups: prev.groups.map((g) =>
            g.id === groupId ? { ...g, isCollapsed: !g.isCollapsed } : g
          ),
        };
      });
    } catch (err) {
      console.error(err);
    }
  };

  const handleSaveGroup = async (groupData) => {
    try {
      if (editingGroup) {
        await categoryService.updateCategoryGroup(editingGroup.id, groupData);
      } else {
        await categoryService.createCategoryGroup(groupData);
      }
      loadBudget();
    } catch (err) {
      alert('Failed to save category group');
    }
  };

  const handleSaveCategory = async (catData) => {
    try {
      if (editingCategoryItem) {
        await categoryService.updateCategory(editingCategoryItem.id, catData);
      } else {
        await categoryService.createCategory(catData);
      }
      loadBudget();
    } catch (err) {
      alert('Failed to save category');
    }
  };

  // Flattened categories list for move money modal dropdowns
  const allCategories = (budgetData?.groups || []).flatMap((g) => g.categories || []);

  const openTargetModalForCategory = (cat, existingTarget) => {
    setTargetCategory(cat);
    setCurrentTarget(existingTarget || cat.target);
    setIsTargetModalOpen(true);
  };

  const openMoveMoneyForCategory = (cat) => {
    setIsMoveMoneyOpen(true);
  };

  return (
    <div className="space-y-6 pb-12 max-w-7xl mx-auto">
      {/* Top Header Controls */}
      <BudgetHeader
        year={year}
        month={month}
        startingBalance={budgetData?.startingBalance || 0}
        readyToAssign={budgetData?.readyToAssign || 0}
        budgetHealth={budgetData?.budgetHealth}
        onPrevMonth={handlePrevMonth}
        onNextMonth={handleNextMonth}
        onToday={handleToday}
        onOpenStartingBalance={() => setIsStartingBalanceOpen(true)}
        onOpenNewGroup={() => {
          setEditingGroup(null);
          setIsGroupModalOpen(true);
        }}
        onOpenNewCategory={() => {
          setEditingCategoryItem(null);
          setIsCategoryModalOpen(true);
        }}
        onOpenAutoAssign={() => setIsAutoAssignOpen(true)}
        onOpenMoveMoney={() => setIsMoveMoneyOpen(true)}
        onCopyPreviousMonth={handleCopyPreviousMonth}
        onOpenRecentMoves={() => setIsRecentMovesOpen(true)}
      />

      {/* Filter and Instant Search Bar */}
      <BudgetFilterBar
        activeFilter={activeFilter}
        onFilterChange={setActiveFilter}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
      />

      {/* Main Budget Workspace Layout: Category Table + Detail Panel */}
      {loading ? (
        <div className="bg-white rounded-2xl border border-slate-200/80 p-16 flex flex-col items-center justify-center gap-3 text-slate-400">
          <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
          <span className="text-xs font-semibold">Loading budget plan...</span>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
          <div className={selectedCategory ? 'lg:col-span-8' : 'lg:col-span-12'}>
            <CategoryTable
              groups={budgetData?.groups || []}
              selectedCategory={selectedCategory}
              onSelectCategory={(cat) => setSelectedCategory(cat)}
              onAssignMoney={handleAssignMoney}
              onToggleGroupCollapse={handleToggleGroupCollapse}
              activeFilter={activeFilter}
              searchQuery={searchQuery}
              year={year}
              month={month}
              onOpenNewCategory={() => {
                setEditingCategoryItem(null);
                setIsCategoryModalOpen(true);
              }}
            />
          </div>

          {selectedCategory && (
            <div className="lg:col-span-4 sticky top-24">
              <CategoryDetailPanel
                category={selectedCategory}
                year={year}
                month={month}
                onClose={() => setSelectedCategory(null)}
                onOpenTargetModal={openTargetModalForCategory}
                onOpenMoveMoneyModal={openMoveMoneyForCategory}
                onAssignMoney={handleAssignMoney}
                onRefresh={loadBudget}
              />
            </div>
          )}
        </div>
      )}

      {/* Modals & Slide-Over Drawers */}
      <StartingBalanceModal
        isOpen={isStartingBalanceOpen}
        onClose={() => setIsStartingBalanceOpen(false)}
        currentBalance={budgetData?.startingBalance || 0}
        onSave={handleSaveStartingBalance}
      />

      <MoveMoneyModal
        isOpen={isMoveMoneyOpen}
        onClose={() => setIsMoveMoneyOpen(false)}
        categories={allCategories}
        onMove={handleMoveMoney}
      />

      <TargetModal
        isOpen={isTargetModalOpen}
        onClose={() => setIsTargetModalOpen(false)}
        category={targetCategory}
        currentTarget={currentTarget}
        onSave={handleSaveTarget}
      />

      <AutoAssignModal
        isOpen={isAutoAssignOpen}
        onClose={() => setIsAutoAssignOpen(false)}
        budgetId={budgetData?.budgetId}
        onApply={handleApplyAutoAssign}
      />

      <RecentMovesDrawer
        isOpen={isRecentMovesOpen}
        onClose={() => setIsRecentMovesOpen(false)}
        activities={budgetData?.recentActivities || []}
        onUndo={handleUndo}
      />

      <CategoryGroupModal
        isOpen={isGroupModalOpen}
        onClose={() => setIsGroupModalOpen(false)}
        group={editingGroup}
        onSave={handleSaveGroup}
      />

      <CategoryModal
        isOpen={isCategoryModalOpen}
        onClose={() => setIsCategoryModalOpen(false)}
        groups={budgetData?.groups || []}
        category={editingCategoryItem}
        onSave={handleSaveCategory}
      />

      <OnboardingModal
        isOpen={isOnboardingOpen}
        onClose={() => setIsOnboardingOpen(false)}
        onUseDefaultTemplate={async () => {
          setIsOnboardingOpen(false);
          await loadBudget();
        }}
      />
    </div>
  );
}
