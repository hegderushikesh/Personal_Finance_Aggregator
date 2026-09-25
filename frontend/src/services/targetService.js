import api from './api';

export const targetService = {
  getTargets: async () => {
    const response = await api.get('/api/targets');
    return response.data;
  },

  getTargetByCategoryId: async (categoryId) => {
    const response = await api.get(`/api/targets/category/${categoryId}`);
    return response.data;
  },

  createOrUpdateTarget: async (data) => {
    const response = await api.post('/api/targets', data);
    return response.data;
  },

  deleteTarget: async (id) => {
    const response = await api.delete(`/api/targets/${id}`);
    return response.data;
  },

  snoozeTarget: async (id) => {
    const response = await api.post(`/api/targets/${id}/snooze`);
    return response.data;
  },

  unsnoozeTarget: async (id) => {
    const response = await api.post(`/api/targets/${id}/unsnooze`);
    return response.data;
  },
};
