import { useEffect } from 'react';
import { useDispatch } from 'react-redux';
import { Toaster } from 'sonner';
import AppRoutes from './routes/AppRoutes';
import { fetchCurrentUser, logout } from './store/authSlice';
import { authService } from './services/authService';

export default function App() {
  const dispatch = useDispatch();

  useEffect(() => {
    const searchParams = new URLSearchParams(window.location.search);
    const urlToken = searchParams.get('token');
    const token = urlToken || authService.getToken();

    if (token) {
      if (urlToken) {
        authService.setToken(urlToken);
        window.history.replaceState({}, document.title, window.location.pathname);
      }
      dispatch(fetchCurrentUser());
    } else {
      dispatch(logout());
    }
  }, [dispatch]);

  return (
    <>
      <Toaster position="bottom-right" theme="dark" richColors />
      <AppRoutes />
    </>
  );
}
