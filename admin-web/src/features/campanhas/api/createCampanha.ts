import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../../../lib/api';
import type { Campanha } from '../types';

export const useCreateCampanha = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (novaCampanha: Omit<Campanha, 'id'>) => {
      const response = await api.post('/api/campanhas', novaCampanha);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['campanhas'] });
    },
  });
};