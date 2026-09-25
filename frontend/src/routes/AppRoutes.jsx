import { useEffect } from 'react';
import { Navigate, Route, Routes, useNavigate, useSearchParams } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import LandingPage from '../pages/LandingPage';
import Login from '../pages/Login';
import SignUp from '../pages/SignUp';
import AuthCallback from '../pages/AuthCallback';
import Dashboard from '../pages/Dashboard';
import PlanPage from '../pages/PlanPage';
import Accounts from '../pages/Accounts';
import Transactions from '../pages/Transactions';
import AnalyticsPage from '../features/analytics/pages/AnalyticsPage';
import RecurringPaymentsPage from '../features/recurring/pages/RecurringPaymentsPage';
import AiPage from '../features/ai/pages/AiPage';
import DashboardLayout from '../layouts/DashboardLayout';
import ProtectedRoute from '../components/ProtectedRoute';
import { fetchCurrentUser, login as setAuth } from '../store/authSlice';
import { authService } from '../services/authService';

function DashboardRoute() {
  const [searchParams] = useSearchParams();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const token = searchParams.get('token');

  useEffect(() => {
    if (!token) return;
    authService.setToken(token);
    dispatch(setAuth({ token, user: null }));
    dispatch(fetchCurrentUser())
      .unwrap()
      .then(() => navigate('/dashboard', { replace: true }))
      .catch(() => navigate('/login?error=google', { replace: true }));
  }, [token, dispatch, navigate]);

  if (token) {
    return (
      <div className="flex min-h-[300px] items-center justify-center text-sm text-slate-500">
        Completing Google sign-in...
      </div>
    );
  }

  return <Dashboard />;
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<SignUp />} />
      <Route path="/auth/callback" element={<AuthCallback />} />

      <Route
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/dashboard" element={<DashboardRoute />} />
        <Route path="/plan" element={<PlanPage />} />
        <Route path="/accounts" element={<Accounts />} />
        <Route path="/transactions" element={<Transactions />} />
        <Route path="/recurring-payments" element={<RecurringPaymentsPage />} />
        <Route path="/analytics" element={<AnalyticsPage />} />
        <Route path="/ai" element={<AiPage />} />
        <Route path="/settings" element={<div className="p-4 text-slate-500 font-medium">Settings view coming soon.</div>} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

