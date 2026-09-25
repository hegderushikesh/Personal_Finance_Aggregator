import api from './api';

export const categoryService = {
  getCategoryGroups: async () => {
    const response = await api.get('/api/category-groups');
    return response.data;
  },

  createCategoryGroup: async (data) => {
    const response = await api.post('/api/category-groups', data);
    return response.data;
  },

  updateCategoryGroup: async (id, data) => {
    const response = await api.put(`/api/category-groups/${id}`, data);
    return response.data;
  },

  deleteCategoryGroup: async (id) => {
    const response = await api.delete(`/api/category-groups/${id}`);
    return response.data;
  },

  toggleCollapseGroup: async (id) => {
    const response = await api.post(`/api/category-groups/${id}/toggle-collapse`);
    return response.data;
  },

  getCategories: async () => {
    const response = await api.get('/api/categories');
    return response.data;
  },

  createCategory: async (data) => {
    const response = await api.post('/api/categories', data);
    return response.data;
  },

  updateCategory: async (id, data) => {
    const response = await api.put(`/api/categories/${id}`, data);
    return response.data;
  },

  deleteCategory: async (id) => {
    const response = await api.delete(`/api/categories/${id}`);
    return response.data;
  },

  moveCategoryToGroup: async (id, targetGroupId) => {
    const response = await api.post(`/api/categories/${id}/move`, { targetGroupId });
    return response.data;
  },
};
