import React, { useEffect, useRef } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { fetchCurrentUser, setCredentials } from '../store/authSlice';
import { authService } from '../services/authService';
import { Loader2 } from 'lucide-react';

export default function AuthCallback() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const processedRef = useRef(false);

  useEffect(() => {
    if (processedRef.current) return;
    processedRef.current = true;

    const token = searchParams.get('token');

    if (token) {
      authService.setToken(token);
      dispatch(fetchCurrentUser())
        .unwrap()
        .then(() => {
          navigate('/dashboard', { replace: true });
        })
        .catch((error) => {
          console.error('Failed to fetch authenticated user:', error);
          authService.removeToken();
          navigate('/login?error=AuthenticationFailed', { replace: true });
        });
    } else {
      navigate('/login?error=MissingToken', { replace: true });
    }
  }, [searchParams, dispatch, navigate]);

  return (
    <div className="min-h-screen bg-[#FAF8F5] flex flex-col items-center justify-center p-4">
      <div className="flex flex-col items-center gap-3">
        <div className="w-12 h-12 rounded-2xl bg-[#0F172A] flex items-center justify-center text-white shadow-lg">
          <Loader2 className="w-6 h-6 animate-spin text-[#D4AF37]" />
        </div>
        <p className="text-sm font-medium text-[#475569]">Completing Google authentication...</p>
      </div>
    </div>
  );
}
