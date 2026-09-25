import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { recurringPaymentService } from '../api/recurringPaymentService';

export function useRecurringPayments(params = {}) {
  return useQuery({
    queryKey: ['recurring-payments', params],
    queryFn: () => recurringPaymentService.getRecurringPayments(params),
    staleTime: 1000 * 60 * 2,
  });
}

export function useRecurringPayment(id) {
  return useQuery({
    queryKey: ['recurring-payments', id],
    queryFn: () => recurringPaymentService.getRecurringPayment(id),
    enabled: !!id,
  });
}

export function useDetectRecurringPayments() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (params) => recurringPaymentService.detectRecurringPayments(params),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recurring-payments'] });
    },
  });
}

export function useUpdateRecurringPayment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }) => recurringPaymentService.updateRecurringPayment(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recurring-payments'] });
    },
  });
}

export function useDeleteRecurringPayment() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id) => recurringPaymentService.deleteRecurringPayment(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['recurring-payments'] });
    },
  });
}
