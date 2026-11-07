import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../../../lib/api';
import type { Instituicao } from '../types';

export const useCreateInstituicao = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (novaInstituicao: Omit<Instituicao, 'id'>) => {
      const response = await api.post('/api/instituicoes', novaInstituicao);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['instituicoes'] });
    },
  });
};