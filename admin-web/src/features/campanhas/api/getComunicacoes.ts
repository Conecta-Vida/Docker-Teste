import { useQuery } from '@tanstack/react-query';
import { api } from '../../../lib/api';

export interface Comunicacao {
  id: number;
  tipo: string;
  instituicao_id?: number;
  titulo: string;
  descricao: string;
  categoria?: string;
  linkimagem?: string;
  localizacao?: string;
  publico_alvo?: string;
  status?: string;
  lido?: boolean;
  data_postada?: string;
  data_inicio?: string;
  data_fim?: string;
}

export const useComunicacoes = () => {
  return useQuery({
    queryKey: ['comunicacoes'],
    queryFn: async () => {
      const response = await api.get<Comunicacao[]>('/api/comunicacoes');
      return response.data;
    },
  });
};