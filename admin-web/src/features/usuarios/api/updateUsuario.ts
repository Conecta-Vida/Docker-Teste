import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api } from '../../../lib/api';
import type { Usuario } from '../types';

export const useUpdateUsuario = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (dadosAtualizados: Usuario) => {
      const response = await api.put(`/api/usuarios/${dadosAtualizados.id}`, dadosAtualizados);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
    },
  });
};