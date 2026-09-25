import React, { useState } from 'react';
import { Navigate, useNavigate, useSearchParams, Link } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { toast } from 'sonner';
import GoogleLoginButton from '../components/auth/GoogleLoginButton';
import useAuth from '../hooks/useAuth';
import { ShieldCheck, Sparkles, Mail, Lock, Loader2, ArrowRight, AlertCircle } from 'lucide-react';

const loginSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Invalid email address'),
  password: z.string().min(1, 'Password is required'),
});

export default function Login() {
  const { isAuthenticated, isInitializing, initialized, loading, error, login, clearError } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const oauthError = searchParams.get('error');
  const [forgotPasswordOpen, setForgotPasswordOpen] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(loginSchema),
  });

  if (isInitializing) {
    return (
      <div className="min-h-screen bg-[#FAF8F5] flex flex-col items-center justify-center p-4">
        <div className="flex flex-col items-center gap-3">
          <div className="w-12 h-12 rounded-2xl bg-[#0F172A] flex items-center justify-center text-white shadow-lg">
            <Loader2 className="w-6 h-6 animate-spin text-[#D4AF37]" />
          </div>
          <p className="text-sm font-medium text-[#475569]">Authenticating FinPilot session...</p>
        </div>
      </div>
    );
  }

  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />;
  }

  const onSubmit = async (data) => {
    clearError();
    const resultAction = await login(data.email, data.password);
    if (resultAction?.type?.endsWith('/fulfilled')) {
      toast.success('Successfully logged in!');
      navigate('/dashboard', { replace: true });
    } else if (resultAction?.type?.endsWith('/rejected')) {
      toast.error(resultAction.payload || 'Invalid email or password.');
    }
  };

  const handleForgotPassword = (e) => {
    e.preventDefault();
    toast.info('Password reset feature will send an email instruction to your registered email address.');
  };

  return (
    <div className="min-h-screen bg-[#FAF8F5] text-[#0F172A] flex flex-col justify-between selection:bg-[#D4AF37]/20 selection:text-[#0F172A]">
      {/* Header */}
      <header className="w-full max-w-[1400px] mx-auto px-6 py-6 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-3 group">
          <div className="w-10 h-10 rounded-xl bg-gold-gradient flex items-center justify-center shadow-sm group-hover:scale-105 transition-transform duration-300">
            <span className="font-display font-black text-xl text-[#0F172A]">F</span>
          </div>
          <div className="flex flex-col">
            <span className="font-display font-extrabold text-2xl tracking-tight text-[#0F172A] leading-none">
              FinPilot
            </span>
            <span className="text-[10px] font-semibold text-[#D4AF37] tracking-[0.18em] uppercase leading-tight">
              Personal Finance Intelligence
            </span>
          </div>
        </Link>

        <Link
          to="/"
          className="text-sm font-medium text-slate-600 hover:text-slate-900 transition-colors"
        >
          &larr; Back to Home
        </Link>
      </header>

      {/* Main Form Container */}
      <main className="flex-1 flex items-center justify-center px-4 py-10">
        <div className="w-full max-w-md">
          <div className="bg-white/90 backdrop-blur-xl border border-slate-200/90 shadow-xl rounded-3xl p-8 sm:p-10 relative overflow-hidden">
            {/* Ambient gold background glow */}
            <div className="absolute -top-24 -right-24 w-48 h-48 bg-[#D4AF37]/10 rounded-full blur-3xl pointer-events-none" />

            <div className="flex flex-col space-y-6">
              {/* Header Icon & Title */}
              <div className="flex flex-col items-center text-center space-y-2">
                <div className="w-13 h-13 rounded-2xl bg-[#0F172A] text-white flex items-center justify-center shadow-lg ring-4 ring-[#D4AF37]/20 mb-1">
                  <Sparkles className="w-6 h-6 text-[#D4AF37]" />
                </div>
                <h1 className="text-2xl sm:text-3xl font-display font-extrabold tracking-tight text-[#0F172A]">
                  Welcome back 👋
                </h1>
                <p className="text-sm text-slate-500 max-w-xs">
                  Manage your finances intelligently with zero friction.
                </p>
              </div>

              {/* OAuth / General Error Alert */}
              {(error || oauthError) && (
                <div className="bg-rose-50 border border-rose-200 text-rose-700 text-xs rounded-xl p-3.5 flex items-start gap-2.5">
                  <AlertCircle className="w-4 h-4 text-rose-500 shrink-0 mt-0.5" />
                  <span>{error || (oauthError && `OAuth Error: ${oauthError}`)}</span>
                </div>
              )}

              {/* Form */}
              <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                {/* Email Input */}
                <div className="space-y-1.5">
                  <label className="text-xs font-bold text-slate-700 uppercase tracking-wider block">
                    Email Address
                  </label>
                  <div className="relative">
                    <Mail className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                    <input
                      {...register('email')}
                      type="email"
                      placeholder="name@example.com"
                      className="w-full h-11 pl-10 pr-4 rounded-xl text-sm bg-slate-50 border border-slate-200 focus:bg-white focus:border-[#0F172A] focus:outline-none focus:ring-2 focus:ring-[#0F172A]/10 transition-all placeholder:text-slate-400"
                    />
                  </div>
                  {errors.email && (
                    <p className="text-xs font-semibold text-rose-500">{errors.email.message}</p>
                  )}
                </div>

                {/* Password Input */}
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between">
                    <label className="text-xs font-bold text-slate-700 uppercase tracking-wider block">
                      Password
                    </label>
                    <a
                      href="#forgot"
                      onClick={handleForgotPassword}
                      className="text-xs font-semibold text-indigo-600 hover:text-indigo-800 transition-colors"
                    >
                      Forgot password?
                    </a>
                  </div>
                  <div className="relative">
                    <Lock className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
                    <input
                      {...register('password')}
                      type="password"
                      placeholder="••••••••"
                      className="w-full h-11 pl-10 pr-4 rounded-xl text-sm bg-slate-50 border border-slate-200 focus:bg-white focus:border-[#0F172A] focus:outline-none focus:ring-2 focus:ring-[#0F172A]/10 transition-all placeholder:text-slate-400"
                    />
                  </div>
                  {errors.password && (
                    <p className="text-xs font-semibold text-rose-500">{errors.password.message}</p>
                  )}
                </div>

                {/* Submit Button */}
                <button
                  type="submit"
                  disabled={loading}
                  className="w-full h-11 mt-2 rounded-xl font-bold text-sm bg-[#0F172A] text-white hover:bg-gold-gradient hover:text-[#0F172A] transition-all duration-300 shadow-md flex items-center justify-center gap-2 group focus:outline-none focus:ring-2 focus:ring-[#D4AF37]"
                >
                  {loading ? (
                    <Loader2 className="w-5 h-5 animate-spin" />
                  ) : (
                    <>
                      <span>Log In</span>
                      <ArrowRight className="w-4 h-4 group-hover:translate-x-0.5 transition-transform" />
                    </>
                  )}
                </button>
              </form>

              {/* Divider */}
              <div className="relative flex items-center justify-center my-2">
                <div className="border-t border-slate-200 w-full" />
                <span className="bg-white px-3 text-xs font-medium text-slate-400 absolute uppercase tracking-widest">
                  or
                </span>
              </div>

              {/* Google OAuth Button */}
              <GoogleLoginButton text="Continue with Google" />

              {/* Sign Up Link */}
              <p className="text-center text-xs font-medium text-slate-500 pt-2">
                Don't have an account?{' '}
                <Link to="/signup" className="font-bold text-indigo-600 hover:text-indigo-800 transition-colors">
                  Sign Up
                </Link>
              </p>
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="py-6 text-center text-xs text-slate-400">
        &copy; {new Date().getFullYear()} FinPilot. All rights reserved.
      </footer>
    </div>
  );
}
