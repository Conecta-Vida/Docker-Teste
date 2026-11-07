import { useQuery } from '@tanstack/react-query';
import { api } from '../../../lib/api';
import type { Instituicao } from '../types';

export const useInstituicoes = () => {
  return useQuery({
    queryKey: ['instituicoes'],
    queryFn: async () => {
      const response = await api.get<Instituicao[]>('/api/instituicoes');
      return response.data;
    },
  });
};