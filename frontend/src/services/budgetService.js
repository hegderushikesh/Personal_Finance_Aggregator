import api from './api';

export const budgetService = {
  getCurrentBudget: async () => {
    const response = await api.get('/api/budgets/current');
    return response.data;
  },

  getBudget: async (year, month) => {
    const response = await api.get('/api/budgets', { params: { year, month } });
    return response.data;
  },

  setStartingBalance: async (budgetId, startingBalance) => {
    const response = await api.post(`/api/budgets/${budgetId}/starting-balance`, { startingBalance });
    return response.data;
  },

  assignMoney: async (budgetId, categoryId, amount) => {
    const response = await api.post(`/api/budgets/${budgetId}/assign`, { categoryId, amount });
    return response.data;
  },

  moveMoney: async (budgetId, fromCategoryId, toCategoryId, amount) => {
    const response = await api.post(`/api/budgets/${budgetId}/move-money`, {
      fromCategoryId,
      toCategoryId,
      amount,
    });
    return response.data;
  },

  copyPreviousMonth: async (budgetId) => {
    const response = await api.post(`/api/budgets/${budgetId}/copy-previous-month`);
    return response.data;
  },

  autoAssignPreview: async (budgetId, mode) => {
    const response = await api.post(`/api/budgets/${budgetId}/auto-assign/preview`, { mode });
    return response.data;
  },

  autoAssignApply: async (budgetId, mode) => {
    const response = await api.post(`/api/budgets/${budgetId}/auto-assign/apply`, { mode });
    return response.data;
  },

  undoLastActivity: async (budgetId) => {
    const response = await api.post(`/api/budgets/${budgetId}/undo`);
    return response.data;
  },

  getCategoryDetails: async (categoryId, year, month) => {
    const response = await api.get(`/api/categories/${categoryId}/details`, {
      params: { year, month },
    });
    return response.data;
  },
};
