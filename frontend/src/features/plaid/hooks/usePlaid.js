import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { plaidService } from '../api/plaidService';

export function usePlaidConnections() {
  return useQuery({
    queryKey: ['plaid', 'connections'],
    queryFn: () => plaidService.getConnections(),
    staleTime: 1000 * 30, // 30 seconds
  });
}

export function useCreateLinkToken() {
  return useMutation({
    mutationFn: () => plaidService.createLinkToken(),
  });
}

export function useExchangePublicToken() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data) => plaidService.exchangePublicToken(data),
    onSuccess: () => {
      // Invalidate all affected financial datasets
      queryClient.invalidateQueries({ queryKey: ['plaid', 'connections'] });
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['budget'] });
      queryClient.invalidateQueries({ queryKey: ['analytics'] });
    },
  });
}

export function useSyncPlaidConnection() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (connectionId) => plaidService.syncConnection(connectionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['plaid', 'connections'] });
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['budget'] });
      queryClient.invalidateQueries({ queryKey: ['analytics'] });
    },
  });
}

export function useDisconnectPlaidConnection() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (connectionId) => plaidService.disconnect(connectionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['plaid', 'connections'] });
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
      queryClient.invalidateQueries({ queryKey: ['transactions'] });
      queryClient.invalidateQueries({ queryKey: ['budget'] });
      queryClient.invalidateQueries({ queryKey: ['analytics'] });
    },
  });
}
