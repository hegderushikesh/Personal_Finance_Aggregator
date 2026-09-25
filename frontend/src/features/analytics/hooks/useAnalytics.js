import { useQuery } from '@tanstack/react-query';
import { analyticsService } from '../api/analyticsService';

export function useSpending(params) {
  return useQuery({
    queryKey: ['analytics', 'spending', params],
    queryFn: () => analyticsService.getSpending(params),
    staleTime: 1000 * 60, // 1 minute
  });
}

export function useSpendingTrends(params) {
  return useQuery({
    queryKey: ['analytics', 'spending-trends', params],
    queryFn: () => analyticsService.getSpendingTrends(params),
    staleTime: 1000 * 60,
  });
}

export function useIncomeExpense(params) {
  return useQuery({
    queryKey: ['analytics', 'income-expense', params],
    queryFn: () => analyticsService.getIncomeExpense(params),
    staleTime: 1000 * 60,
  });
}

export function useCashFlow(params) {
  return useQuery({
    queryKey: ['analytics', 'cash-flow', params],
    queryFn: () => analyticsService.getCashFlow(params),
    staleTime: 1000 * 60,
  });
}

export function useCategorySpending(params) {
  return useQuery({
    queryKey: ['analytics', 'categories', params],
    queryFn: () => analyticsService.getCategorySpending(params),
    staleTime: 1000 * 60,
  });
}

export function useNetWorth() {
  return useQuery({
    queryKey: ['analytics', 'net-worth'],
    queryFn: () => analyticsService.getNetWorth(),
    staleTime: 1000 * 60,
  });
}

export function useNetWorthTrend(params) {
  return useQuery({
    queryKey: ['analytics', 'net-worth-trend', params],
    queryFn: () => analyticsService.getNetWorthTrend(params),
    staleTime: 1000 * 60,
  });
}
