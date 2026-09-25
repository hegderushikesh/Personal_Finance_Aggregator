import api, { TOKEN_KEY } from './api';

export const authService = {
  getToken: () => localStorage.getItem(TOKEN_KEY),

  setToken: (token) => {
    if (token) {
      localStorage.setItem(TOKEN_KEY, token);
    } else {
      localStorage.removeItem(TOKEN_KEY);
    }
  },

  removeToken: () => {
    localStorage.removeItem(TOKEN_KEY);
  },

  login: async (email, password) => {
    const response = await api.post('/api/auth/login', { email, password });
    return response.data;
  },

  register: async (name, email, password) => {
    const response = await api.post('/api/auth/register', { name, email, password });
    return response.data;
  },

  signup: async (name, email, password) => {
    return authService.register(name, email, password);
  },

  logout: async () => {
    try {
      await api.post('/api/auth/logout');
    } catch {
      // JWT is stateless; always clear local auth even if the request fails.
    } finally {
      authService.removeToken();
    }
  },

  getCurrentUser: async () => {
    const response = await api.get('/api/auth/me');
    return response.data;
  },

  getGoogleOAuthUrl: () => {
    const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
    return `${apiBaseUrl}/oauth2/authorization/google`;
  },
};
