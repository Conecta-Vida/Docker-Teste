import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../../../lib/api';
import type { Instituicao } from '../types';

export const useUpdateInstituicao = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (dadosAtualizados: Instituicao) => {
      const response = await api.put(`/api/instituicoes/${dadosAtualizados.id}`, dadosAtualizados);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['instituicoes'] });
    },
  });
};