import { useQuery } from '@tanstack/react-query';
import { api } from '../../../lib/api';

export interface DashboardStats {
  totalUsuarios: number;
  totalInstituicoes: number;
  totalNoticias: number;
  campanhasAtivas: number;
}

export const useDashboardStats = () => {
  return useQuery({
    queryKey: ['dashboardStats'],
    queryFn: async () => {
      const response = await api.get<DashboardStats>('/api/dashboard/stats');
      return response.data;
    },
  });
};