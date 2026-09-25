import api from '../../../services/api';

export const plaidService = {
  createLinkToken: async () => {
    const response = await api.post('/api/plaid/link-token');
    return response.data;
  },

  exchangePublicToken: async (data) => {
    const response = await api.post('/api/plaid/exchange-token', data);
    return response.data;
  },

  getConnections: async () => {
    const response = await api.get('/api/plaid/connections');
    return response.data;
  },

  getConnection: async (id) => {
    const response = await api.get(`/api/plaid/connections/${id}`);
    return response.data;
  },

  syncConnection: async (connectionId) => {
    const response = await api.post(`/api/plaid/sync/${connectionId}`);
    return response.data;
  },

  disconnect: async (connectionId) => {
    const response = await api.delete(`/api/plaid/connections/${connectionId}`);
    return response.data;
  },
};

export default plaidService;
