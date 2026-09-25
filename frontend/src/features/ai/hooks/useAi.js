import { useQuery, useMutation } from '@tanstack/react-query';
import { aiService } from '../api/aiService';

export const AI_QUERY_KEYS = {
  health: ['ai', 'health'],
  spendingInsights: (params) => ['ai', 'insights', 'spending', params],
  budgetInsights: (params) => ['ai', 'insights', 'budget', params],
  recurringInsights: ['ai', 'insights', 'recurring'],
  monthlySummary: (params) => ['ai', 'summary', 'monthly', params],
};

export function useAiHealth() {
  return useQuery({
    queryKey: AI_QUERY_KEYS.health,
    queryFn: aiService.getHealth,
    staleTime: 60 * 1000,
    retry: 1,
  });
}

export function useMonthlySummary(params = {}) {
  return useQuery({
    queryKey: AI_QUERY_KEYS.monthlySummary(params),
    queryFn: () => aiService.getMonthlySummary(params),
    staleTime: 5 * 60 * 1000, // 5 min cache
    retry: 1,
  });
}

export function useSpendingInsights(params = {}) {
  return useQuery({
    queryKey: AI_QUERY_KEYS.spendingInsights(params),
    queryFn: () => aiService.getSpendingInsights(params),
    staleTime: 5 * 60 * 1000,
    retry: 1,
  });
}

export function useBudgetInsights(params = {}) {
  return useQuery({
    queryKey: AI_QUERY_KEYS.budgetInsights(params),
    queryFn: () => aiService.getBudgetInsights(params),
    staleTime: 5 * 60 * 1000,
    retry: 1,
  });
}

export function useRecurringInsights() {
  return useQuery({
    queryKey: AI_QUERY_KEYS.recurringInsights,
    queryFn: aiService.getRecurringInsights,
    staleTime: 5 * 60 * 1000,
    retry: 1,
  });
}

export function useAiChat() {
  return useMutation({
    mutationFn: (message) => aiService.chat(message),
  });
}

export function useSuggestCategory() {
  return useMutation({
    mutationFn: (data) => aiService.suggestCategory(data),
  });
}
