import { useQuery } from '@tanstack/react-query';
import { api } from '../../../lib/api';

export interface Campanha {
  id?: number;
  titulo?: string;
  descricao?: string;
  tipo?: string;
  status?: string;
  dataInicio?: string;
  dataFim?: string;
  dataPostada?: string;
  [key: string]: any;
}

export const useCampanhas = () => {
  return useQuery({
    queryKey: ['campanhas'],
    queryFn: async () => {
      const response = await api.get<Campanha[]>('/api/campanhas');
      return response.data;
    },
  });
};