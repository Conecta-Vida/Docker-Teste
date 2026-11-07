import React, { useState } from "react";
import { useComunicacoes } from "../features/campanhas/api/getComunicacoes";
import type { Comunicacao } from "../features/campanhas/api/getComunicacoes";
import { CampanhaModal } from "../features/campanhas/components/CampanhaModal";
import { api } from "../lib/api";
import { useQueryClient } from "@tanstack/react-query";

export const CampanhasPage: React.FC = () => {
  const { data: comunicacoes, isLoading, error } = useComunicacoes();
  const queryClient = useQueryClient();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [comunicacaoParaEditar, setComunicacaoParaEditar] = useState<Comunicacao | null>(null);

  const handleOpenCreate = () => {
    setComunicacaoParaEditar(null);
    setIsModalOpen(true);
  };

  const handleOpenEdit = (item: Comunicacao) => {
    setComunicacaoParaEditar(item);
    setIsModalOpen(true);
  };

  const handleDelete = async (id: number) => {
    if (window.confirm("Tem certeza de que deseja eliminar este registo?")) {
      try {
        await api.delete(`/api/comunicacoes/${id}`);
        queryClient.invalidateQueries({ queryKey: ["comunicacoes"] });
      } catch (err) {
        console.error("Erro ao eliminar:", err);
        alert("Erro ao eliminar o registo.");
      }
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-100">
        <div className="text-gray-500 font-medium">A carregar campanhas e alertas...</div>
      </div>
    );
  }
  
  if (error) {
    return (
      <div className="p-8 max-w-7xl mx-auto">
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl">
          Erro ao carregar dados do servidor.
        </div>
      </div>
    );
  }

  return (
    <div className="p-8 max-w-7xl mx-auto space-y-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 tracking-tight">Gestão de Campanhas e Alertas</h1>
          <p className="text-sm text-gray-500 mt-1">Gerencie todos os comunicados, campanhas de saúde e alertas do sistema.</p>
        </div>
        <button
          onClick={handleOpenCreate}
          className="inline-flex items-center justify-center rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-700 shadow-sm transition-all focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
        >
          + Nova Campanha / Alerta
        </button>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        {!comunicacoes || comunicacoes.length === 0 ? (
          <div className="p-12 text-center">
            <p className="text-gray-500 font-medium">Nenhum registo encontrado na base de dados.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200 text-sm">
              <thead className="bg-gray-50/75">
                <tr>
                  <th className="px-6 py-3.5 text-left font-semibold text-gray-700 tracking-wider">Tipo</th>
                  <th className="px-6 py-3.5 text-left font-semibold text-gray-700 tracking-wider">Título & Descrição</th>
                  <th className="px-6 py-3.5 text-left font-semibold text-gray-700 tracking-wider">Categoria & Público</th>
                  <th className="px-6 py-3.5 text-left font-semibold text-gray-700 tracking-wider">Localização</th>
                  <th className="px-6 py-3.5 text-left font-semibold text-gray-700 tracking-wider">Período</th>
                  <th className="px-6 py-3.5 text-left font-semibold text-gray-700 tracking-wider">Status</th>
                  <th className="px-6 py-3.5 text-right font-semibold text-gray-700 tracking-wider">Ações</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200 bg-white">
                {comunicacoes.map((item: Comunicacao) => (
                  <tr key={item.id} className="hover:bg-gray-50/50 transition-colors">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-semibold ${
                        item.tipo === 'Alerta' ? 'bg-red-50 text-red-700 border border-red-100' : 'bg-blue-50 text-blue-700 border border-blue-100'
                      }`}>
                        {item.tipo}
                      </span>
                    </td>
                    <td className="px-6 py-4 max-w-xs">
                      <div className="font-semibold text-gray-900 truncate">{item.titulo}</div>
                      <div className="text-gray-500 text-xs truncate mt-0.5">{item.descricao}</div>
                      <div className="text-gray-400 text-[10px] mt-1">
                        Postado em: {item.data_postada ? new Date(item.data_postada).toLocaleDateString() : 'N/A'}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-gray-900 font-medium">{item.categoria || 'Geral'}</div>
                      <div className="text-gray-500 text-xs">{item.publico_alvo || 'Todos'}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-gray-600">
                      {item.localizacao || 'Não especificada'}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-xs text-gray-600">
                      <div><strong>Início:</strong> {item.data_inicio ? new Date(item.data_inicio).toLocaleDateString() : 'Não def.'}</div>
                      <div><strong>Fim:</strong> {item.data_fim ? new Date(item.data_fim).toLocaleDateString() : 'Não def.'}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-50 text-emerald-700 border border-emerald-100">
                        {item.status || 'Ativo'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-xs font-medium space-x-3">
                      <button
                        onClick={() => handleOpenEdit(item)}
                        className="text-indigo-600 hover:text-indigo-900 transition-colors font-semibold"
                      >
                        Editar
                      </button>
                      <button
                        onClick={() => handleDelete(item.id)}
                        className="text-red-600 hover:text-red-900 transition-colors font-semibold"
                      >
                        Apagar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <CampanhaModal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          queryClient.invalidateQueries({ queryKey: ["comunicacoes"] });
        }}
        campanhaParaEditar={comunicacaoParaEditar}
      />
    </div>
  );
};

export default CampanhasPage;