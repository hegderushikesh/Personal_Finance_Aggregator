import api from '../../../services/api';

export const recurringPaymentService = {
  getRecurringPayments: async (params = {}) => {
    const response = await api.get('/api/recurring-payments', { params });
    return response.data;
  },

  getRecurringPayment: async (id) => {
    const response = await api.get(`/api/recurring-payments/${id}`);
    return response.data;
  },

  detectRecurringPayments: async (params = {}) => {
    const response = await api.post('/api/recurring-payments/detect', params);
    return response.data;
  },

  updateRecurringPayment: async (id, data) => {
    const response = await api.put(`/api/recurring-payments/${id}`, data);
    return response.data;
  },

  deleteRecurringPayment: async (id) => {
    const response = await api.delete(`/api/recurring-payments/${id}`);
    return response.data;
  },
};

export default recurringPaymentService;
