import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../../../lib/api';
import type { Campanha } from '../types';

export const useUpdateCampanha = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (dadosAtualizados: Campanha) => {
      const response = await api.put(`/api/campanhas/${dadosAtualizados.id}`, dadosAtualizados);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['campanhas'] });
    },
  });
};