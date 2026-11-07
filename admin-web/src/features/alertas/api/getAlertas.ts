import { useQuery } from '@tanstack/react-query';
import { api } from '../../../lib/api';

export interface Alerta {
  id?: number;
  titulo?: string;
  descricao?: string;
  tipo?: string;
  status?: string;
  dataPostada?: string;
  [key: string]: any;
}

export const useAlertas = () => {
  return useQuery({
    queryKey: ['alertas'],
    queryFn: async () => {
      const response = await api.get<Alerta[]>('/api/alertas');
      return response.data;
    },
  });
};