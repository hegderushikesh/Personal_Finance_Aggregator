import { useSelector, useDispatch } from 'react-redux';
import { logout as logoutAction, fetchCurrentUser, loginUser, signupUser, clearError } from '../store/authSlice';
import { authService } from '../services/authService';

export function useAuth() {
  const dispatch = useDispatch();
  const auth = useSelector((state) => state.auth);

  const handleLogout = async () => {
    dispatch(logoutAction());
    try {
      await authService.logout();
    } catch {
      authService.removeToken();
    }
  };

  const handleLogin = (email, password) => dispatch(loginUser({ email, password }));
  const handleSignup = (name, email, password) => dispatch(signupUser({ name, email, password }));
  const loginWithGoogle = () => {
    window.location.href = authService.getGoogleOAuthUrl();
  };

  return {
    user: auth.user,
    token: auth.token,
    isAuthenticated: auth.isAuthenticated,
    loading: auth.loading,
    isInitializing: !auth.isAuthenticated && Boolean(auth.isInitializing ?? !auth.initialized),
    initialized: Boolean(auth.initialized),
    error: auth.error,
    login: handleLogin,
    signup: handleSignup,
    logout: handleLogout,
    loginWithGoogle,
    reauthenticate: () => dispatch(fetchCurrentUser()),
    clearError: () => dispatch(clearError()),
  };
}

export default useAuth;
