import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../../../lib/api';

export const useDeleteInstituicao = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: string) => {
      await api.delete(`/api/instituicoes/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['instituicoes'] });
    },
  });
};