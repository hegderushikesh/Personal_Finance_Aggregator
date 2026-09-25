import api from './api';

export const accountService = {
  getAccounts: async () => {
    const response = await api.get('/api/accounts');
    return response.data;
  },

  getAccountSummary: async () => {
    const response = await api.get('/api/accounts/summary');
    return response.data;
  },

  getAccountById: async (id) => {
    const response = await api.get(`/api/accounts/${id}`);
    return response.data;
  },

  createAccount: async (accountData) => {
    const response = await api.post('/api/accounts', accountData);
    return response.data;
  },

  updateAccount: async (id, accountData) => {
    const response = await api.put(`/api/accounts/${id}`, accountData);
    return response.data;
  },

  deleteAccount: async (id) => {
    const response = await api.delete(`/api/accounts/${id}`);
    return response.data;
  },
};

export default accountService;
