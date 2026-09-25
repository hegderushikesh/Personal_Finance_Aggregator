import api from './api';

export const transactionService = {
  getTransactions: async (params = {}) => {
    const response = await api.get('/api/transactions', { params });
    return response.data;
  },

  getTransactionById: async (id) => {
    const response = await api.get(`/api/transactions/${id}`);
    return response.data;
  },

  createTransaction: async (data) => {
    const response = await api.post('/api/transactions', data);
    return response.data;
  },

  updateTransaction: async (id, data) => {
    const response = await api.put(`/api/transactions/${id}`, data);
    return response.data;
  },

  deleteTransaction: async (id) => {
    const response = await api.delete(`/api/transactions/${id}`);
    return response.data;
  },

  getSummary: async (params = {}) => {
    const response = await api.get('/api/transactions/summary', { params });
    return response.data;
  },

  getRecentTransactions: async (limit = 5) => {
    const response = await api.get('/api/transactions/recent', { params: { limit } });
    return response.data;
  },
};

export default transactionService;
