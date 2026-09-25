import api from '../../../services/api';

export const aiService = {
  getHealth: async () => {
    const response = await api.get('/api/ai/health');
    return response.data;
  },

  chat: async (message) => {
    const response = await api.post('/api/ai/chat', { message });
    return response.data;
  },

  getSpendingInsights: async (params = {}) => {
    const response = await api.get('/api/ai/insights/spending', { params });
    return response.data;
  },

  getBudgetInsights: async (params = {}) => {
    const response = await api.get('/api/ai/insights/budget', { params });
    return response.data;
  },

  getRecurringInsights: async () => {
    const response = await api.get('/api/ai/insights/recurring');
    return response.data;
  },

  getMonthlySummary: async (params = {}) => {
    const response = await api.get('/api/ai/summary/monthly', { params });
    return response.data;
  },

  suggestCategory: async (data) => {
    const response = await api.post('/api/ai/suggest-category', data);
    return response.data;
  },
};
