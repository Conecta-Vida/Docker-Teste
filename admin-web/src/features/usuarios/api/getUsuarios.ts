import { useQuery } from '@tanstack/react-query';
import { api } from '../../../lib/api';
import type { Usuario } from '../types';

export const useUsuarios = () => {
  return useQuery({
    queryKey: ['usuarios'],
    queryFn: async () => {
      const response = await api.get<Usuario[]>('/api/usuarios');
      return response.data;
    },
  });
};