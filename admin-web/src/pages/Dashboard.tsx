import React from "react";
import { useQuery } from "@tanstack/react-query";
import { api } from "../lib/api";
import { 
  ResponsiveContainer, BarChart, Bar, LineChart, Line, 
  XAxis, YAxis, Tooltip, CartesianGrid 
} from "recharts";

export interface LogAtividadeItem {
  id: number;
  usuarioId: number;
  usuario?: { nome: string; permissao?: string };
  acao: string;
  dataHora: string;
}

export interface DashboardData {
  totalUsuarios: number;
  totalInstituicoes: number;
  totalNoticias: number;
  campanhasAtivas: number;
  crescimentoUsuarios: { mes: string; total: number }[];
  ultimosLogs: LogAtividadeItem[];
  adminMaisAtivo: string;
}

export const useDashboardData = () => {
  return useQuery({
    queryKey: ['dashboardData'],
    queryFn: async () => {
      const response = await api.get<DashboardData>('/api/dashboard/stats');
      return response.data;
    },
  });
};

export const DashboardPage: React.FC = () => {
  const { data, isLoading, error } = useDashboardData();

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-100">
        <div className="text-gray-500 font-medium">A carregar métricas do painel...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8 max-w-7xl mx-auto">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl">
          Erro ao carregar estatísticas do servidor.
        </div>
      </div>
    );
  }

  const chartData = [
    { name: "Usuários", total: data?.totalUsuarios ?? 0 },
    { name: "Instituições", total: data?.totalInstituicoes ?? 0 },
    { name: "Notícias", total: data?.totalNoticias ?? 0 },
    { name: "Campanhas Ativas", total: data?.campanhasAtivas ?? 0 },
  ];

  return (
    <div className="p-6 lg:p-8 max-w-7xl mx-auto space-y-6 w-full overflow-x-hidden">
      {/* Cabeçalho */}
      <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Visão Geral do Sistema</h1>
          <p className="text-sm text-gray-500 mt-1">Acompanhe em tempo real os dados cadastrados na base de dados.</p>
        </div>
        <div className="bg-blue-50 border border-blue-100 px-4 py-2 rounded-xl text-xs shrink-0">
          <span className="text-blue-600 font-semibold">Admin mais ativo:</span>{" "}
          <span className="font-bold text-gray-900">{data?.adminMaisAtivo || 'Nenhum registo'}</span>
        </div>
      </div>

      {/* Cartões de Métricas */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center justify-between min-w-0">
          <div className="truncate">
            <p className="text-sm font-medium text-gray-500">Usuários Cadastrados</p>
            <p className="text-3xl font-bold text-gray-900 mt-2">{data?.totalUsuarios ?? 0}</p>
          </div>
          <div className="p-3 bg-blue-50 text-blue-600 rounded-xl shrink-0">
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
          </div>
        </div>

        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center justify-between min-w-0">
          <div className="truncate">
            <p className="text-sm font-medium text-gray-500">Instituições Parceiras</p>
            <p className="text-3xl font-bold text-gray-900 mt-2">{data?.totalInstituicoes ?? 0}</p>
          </div>
          <div className="p-3 bg-emerald-50 text-emerald-600 rounded-xl shrink-0">
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
            </svg>
          </div>
        </div>

        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center justify-between min-w-0">
          <div className="truncate">
            <p className="text-sm font-medium text-gray-500">Notícias Cadastradas</p>
            <p className="text-3xl font-bold text-gray-900 mt-2">{data?.totalNoticias ?? 0}</p>
          </div>
          <div className="p-3 bg-purple-50 text-purple-600 rounded-xl shrink-0">
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 20H5a2 2 0 01-2-2V6a2 2 0 012-2h10a2 2 0 012 2v1m2 13a2 2 0 01-2-2V7m2 13a2 2 0 002-2V9a2 2 0 00-2-2h-2m-4-3H9M7 16h6M7 8h6v4H7V8z" />
            </svg>
          </div>
        </div>

        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex items-center justify-between min-w-0">
          <div className="truncate">
            <p className="text-sm font-medium text-gray-500">Campanhas Ativas</p>
            <p className="text-3xl font-bold text-gray-900 mt-2">{data?.campanhasAtivas ?? 0}</p>
          </div>
          <div className="p-3 bg-amber-50 text-amber-600 rounded-xl shrink-0">
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M11 5.882V19.24a1.76 1.76 0 01-3.417.592l-2.147-6.15M18 13a3 3 0 100-6M5.436 13.683A4.001 4.001 0 017 6h1.832c4.1 0 7.625-1.234 9.168-3v14c-1.543-1.766-5.067-3-9.168-3H7a3.988 3.988 0 01-1.564-.317z" />
            </svg>
          </div>
        </div>
      </div>

      {/* Reorganização: Gráficos à esquerda e Logs no outro lado */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
        {/* Coluna dos Gráficos (2 Colunas) */}
        <div className="lg:col-span-2 space-y-6 min-w-0">
          {/* Gráfico 1: Linha de Crescimento de Usuários por Mês */}
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 min-w-0">
            <h2 className="text-lg font-bold text-gray-900 mb-4">Entrada de Novos Usuários por Mês</h2>
            <div className="h-72 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={data?.crescimentoUsuarios ?? []} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="mes" stroke="#6b7280" fontSize={12} />
                  <YAxis stroke="#6b7280" fontSize={12} />
                  <Tooltip />
                  <Line type="monotone" dataKey="total" stroke="#10b981" strokeWidth={3} dot={{ r: 4 }} activeDot={{ r: 6 }} />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Gráfico 2: Barras de Distribuição Geral do Sistema */}
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 min-w-0">
            <h2 className="text-lg font-bold text-gray-900 mb-4">Distribuição Geral do Sistema</h2>
            <div className="h-72 w-full">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={chartData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="name" stroke="#6b7280" fontSize={12} />
                  <YAxis stroke="#6b7280" fontSize={12} />
                  <Tooltip />
                  <Bar dataKey="total" fill="#2563eb" radius={[6, 6, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        {/* Painel Lateral de Logs Administrativos */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col min-w-0">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-bold text-gray-900">Logs de Atividade</h2>
            <span className="text-xs bg-emerald-50 text-emerald-700 border border-emerald-100 px-2.5 py-1 rounded-full font-medium shrink-0">Ao Vivo</span>
          </div>
          <p className="text-xs text-gray-500 mb-4">Registo de alterações e exclusões efetuadas por administradores.</p>

          <div className="space-y-3 overflow-y-auto max-h-130 pr-1">
            {!data?.ultimosLogs || data.ultimosLogs.length === 0 ? (
              <p className="text-xs text-gray-400 text-center py-8">Nenhum registo de atividade recente.</p>
            ) : (
              data.ultimosLogs.map((log) => (
                <div key={log.id} className="p-3 bg-gray-50 rounded-lg border border-gray-100 text-xs space-y-2 transition-all hover:bg-gray-100/50">
                  <div className="font-semibold text-indigo-600 break-all">
                    {log.usuario?.nome || `Usuário ID: ${log.usuarioId}`}
                  </div>
                  <p className="text-gray-700 font-medium break-all whitespace-normal leading-relaxed">
                    {log.acao}
                  </p>
                  <div className="text-[10px] text-gray-400 text-right pt-1 border-t border-gray-200/60">
                    {new Date(log.dataHora).toLocaleString()}
                  </div>
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;