import api from '../../../services/api';

export const analyticsService = {
  getSpending: async (params = {}) => {
    const response = await api.get('/api/analytics/spending', { params });
    return response.data;
  },

  getSpendingTrends: async (params = {}) => {
    const response = await api.get('/api/analytics/spending/trends', { params });
    return response.data;
  },

  getIncomeExpense: async (params = {}) => {
    const response = await api.get('/api/analytics/income-expense', { params });
    return response.data;
  },

  getCashFlow: async (params = {}) => {
    const response = await api.get('/api/analytics/cash-flow', { params });
    return response.data;
  },

  getCategorySpending: async (params = {}) => {
    const response = await api.get('/api/analytics/categories', { params });
    return response.data;
  },

  getNetWorth: async (params = {}) => {
    const response = await api.get('/api/analytics/net-worth', { params });
    return response.data;
  },

  getNetWorthTrend: async (params = {}) => {
    const response = await api.get('/api/analytics/net-worth/trend', { params });
    return response.data;
  },
};

export default analyticsService;
