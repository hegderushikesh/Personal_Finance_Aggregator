import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { authService } from '../services/authService';

const readError = (error, fallback) => {
  return (
    error.userMessage ||
    error.response?.data?.message ||
    (typeof error.response?.data === 'string' ? error.response.data : null) ||
    fallback
  );
};

export const fetchCurrentUser = createAsyncThunk(
  'auth/fetchCurrentUser',
  async (_, { rejectWithValue }) => {
    try {
      const token = authService.getToken();
      if (!token) {
        return rejectWithValue('No token found');
      }
      const user = await authService.getCurrentUser();
      return { user, token };
    } catch (error) {
      authService.removeToken();
      return rejectWithValue(readError(error, 'Failed to authenticate user'));
    }
  }
);

export const loginUser = createAsyncThunk(
  'auth/loginUser',
  async ({ email, password }, { rejectWithValue }) => {
    try {
      const data = await authService.login(email, password);
      authService.setToken(data.token);
      return data;
    } catch (error) {
      return rejectWithValue(readError(error, 'Invalid email or password.'));
    }
  }
);

export const signupUser = createAsyncThunk(
  'auth/signupUser',
  async ({ name, email, password }, { rejectWithValue }) => {
    try {
      const data = await authService.register(name, email, password);
      authService.setToken(data.token);
      return data;
    } catch (error) {
      return rejectWithValue(readError(error, 'An account with this email already exists.'));
    }
  }
);

const initialState = {
  user: null,
  token: authService.getToken(),
  isAuthenticated: false,
  loading: false,
  isInitializing: true,
  initialized: false,
  error: null,
};

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    login: (state, action) => {
      const { user, token } = action.payload;
      state.user = user;
      state.token = token;
      state.isAuthenticated = Boolean(token);
      state.isInitializing = false;
      state.initialized = true;
      state.error = null;
      if (token) {
        authService.setToken(token);
      }
    },
    setUser: (state, action) => {
      state.user = action.payload;
      state.isAuthenticated = Boolean(state.token && action.payload);
      state.isInitializing = false;
      state.initialized = true;
    },
    logout: (state) => {
      state.user = null;
      state.token = null;
      state.isAuthenticated = false;
      state.isInitializing = false;
      state.initialized = true;
      state.loading = false;
      state.error = null;
      authService.removeToken();
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchCurrentUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCurrentUser.fulfilled, (state, action) => {
        state.user = action.payload.user;
        state.token = action.payload.token;
        state.isAuthenticated = true;
        state.loading = false;
        state.isInitializing = false;
        state.initialized = true;
        state.error = null;
      })
      .addCase(fetchCurrentUser.rejected, (state) => {
        state.user = null;
        state.token = null;
        state.isAuthenticated = false;
        state.loading = false;
        state.isInitializing = false;
        state.initialized = true;
      })
      .addCase(loginUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loginUser.fulfilled, (state, action) => {
        state.user = action.payload.user;
        state.token = action.payload.token;
        state.isAuthenticated = true;
        state.loading = false;
        state.isInitializing = false;
        state.initialized = true;
        state.error = null;
      })
      .addCase(loginUser.rejected, (state, action) => {
        state.loading = false;
        state.isInitializing = false;
        state.initialized = true;
        state.error = action.payload || 'Invalid email or password.';
      })
      .addCase(signupUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(signupUser.fulfilled, (state, action) => {
        state.user = action.payload.user;
        state.token = action.payload.token;
        state.isAuthenticated = true;
        state.loading = false;
        state.isInitializing = false;
        state.initialized = true;
        state.error = null;
      })
      .addCase(signupUser.rejected, (state, action) => {
        state.loading = false;
        state.isInitializing = false;
        state.initialized = true;
        state.error = action.payload || 'An account with this email already exists.';
      });
  },
});

export const { login, setUser, logout, clearError } = authSlice.actions;
export const setCredentials = login;
export default authSlice.reducer;
