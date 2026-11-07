import React, { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/api";
import { Building2, Plus, Search, X } from "lucide-react";

export interface Instituicao {
  id: number;
  nome: string;
  email: string;
  telefone: string;
  tipoInstituicao: string;
  endereco?: string;
}

export const InstituicoesPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [searchTerm, setSearchTerm] = useState("");

  const [formData, setFormData] = useState({
    nome: "",
    email: "",
    telefone: "",
    tipoInstituicao: "Hospital", // Valor padrão inicial
    endereco: ""
  });

  const { data: instituicoes, isLoading } = useQuery({
    queryKey: ['instituicoes'],
    queryFn: async () => {
      const response = await api.get<Instituicao[]>('/api/instituicoes');
      return response.data;
    },
  });

  // Mutação para Criar ou Atualizar
  const saveMutation = useMutation({
    mutationFn: async (data: typeof formData) => {
      if (editingId) {
        return await api.put(`/api/instituicoes/${editingId}`, data);
      } else {
        return await api.post('/api/instituicoes', data);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['instituicoes'] });
      handleCloseModal();
    }
  });

  // Mutação para Excluir
  const deleteMutation = useMutation({
    mutationFn: async (id: number) => {
      return await api.delete(`/api/instituicoes/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['instituicoes'] });
    }
  });

  const handleOpenModal = (inst?: Instituicao) => {
    if (inst) {
      setEditingId(inst.id);
      setFormData({
        nome: inst.nome || "",
        email: inst.email || "",
        telefone: inst.telefone || "",
        tipoInstituicao: inst.tipoInstituicao || "Hospital",
        endereco: inst.endereco || ""
      });
    } else {
      setEditingId(null);
      setFormData({ nome: "", email: "", telefone: "", tipoInstituicao: "Hospital", endereco: "" });
    }
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingId(null);
    setFormData({ nome: "", email: "", telefone: "", tipoInstituicao: "Hospital", endereco: "" });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    saveMutation.mutate(formData);
  };

  const filteredInstituicoes = instituicoes?.filter(inst => 
    inst.nome.toLowerCase().includes(searchTerm.toLowerCase()) ||
    inst.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="p-6 lg:p-8 max-w-7xl mx-auto space-y-6 w-full overflow-x-hidden">
      {/* Cabeçalho */}
      <div className="bg-white p-5 rounded-xl shadow-sm border border-gray-100 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-xl font-bold text-gray-900 tracking-tight">Instituições Parceiras</h1>
          <p className="text-xs text-gray-500 mt-0.5">Gerencie as instituições de saúde registadas no sistema.</p>
        </div>
        <button 
          onClick={() => handleOpenModal()}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-xl text-xs font-medium transition-colors flex items-center gap-2 cursor-pointer shadow-sm"
        >
          <Plus className="w-4 h-4" />
          <span>Nova Instituição</span>
        </button>
      </div>

      {/* Contentor da Tabela Compacta */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="p-4 border-b border-gray-100 flex items-center gap-2">
          <Search className="w-4 h-4 text-gray-400 shrink-0" />
          <input 
            type="text" 
            placeholder="Pesquisar instituições..." 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="text-xs outline-none w-full text-gray-700 placeholder-gray-400 bg-transparent"
          />
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-50/70 border-b border-gray-100 text-[11px] font-semibold text-gray-500 uppercase tracking-wider">
                <th className="py-2.5 px-4">Nome</th>
                <th className="py-2.5 px-4">E-mail</th>
                <th className="py-2.5 px-4">Telefone</th>
                <th className="py-2.5 px-4">Tipo</th>
                <th className="py-2.5 px-4 text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 text-xs">
              {isLoading ? (
                <tr>
                  <td colSpan={5} className="py-8 text-center text-gray-400">A carregar instituições...</td>
                </tr>
              ) : !filteredInstituicoes || filteredInstituicoes.length === 0 ? (
                <tr>
                  <td colSpan={5} className="py-8 text-center text-gray-400">Nenhuma instituição encontrada.</td>
                </tr>
              ) : (
                filteredInstituicoes.map((inst) => (
                  <tr key={inst.id} className="hover:bg-gray-50/50 transition-colors">
                    <td className="py-3 px-4 font-medium text-gray-900 flex items-center gap-2">
                      <Building2 className="w-4 h-4 text-blue-600 shrink-0" />
                      <span className="truncate">{inst.nome}</span>
                    </td>
                    <td className="py-3 px-4 text-gray-600 truncate">{inst.email}</td>
                    <td className="py-3 px-4 text-gray-600">{inst.telefone || 'N/A'}</td>
                    <td className="py-3 px-4">
                      <span className="bg-emerald-50 text-emerald-700 border border-emerald-100 px-2.5 py-0.5 rounded-md text-[10px] font-semibold">
                        {inst.tipoInstituicao || 'Hospital'}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right space-x-3">
                      <button 
                        onClick={() => handleOpenModal(inst)}
                        className="text-blue-600 hover:text-blue-800 font-medium cursor-pointer"
                      >
                        Editar
                      </button>
                      <button 
                        onClick={() => {
                          if (window.confirm("Tem certeza que deseja excluir esta instituição?")) {
                            deleteMutation.mutate(inst.id);
                          }
                        }}
                        className="text-red-600 hover:text-red-800 font-medium cursor-pointer"
                      >
                        Excluir
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal de Cadastro / Edição */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl max-w-md w-full p-6 space-y-4">
            <div className="flex justify-between items-center border-b border-gray-100 pb-3">
              <h2 className="text-base font-bold text-gray-900">
                {editingId ? "Editar Instituição" : "Nova Instituição"}
              </h2>
              <button onClick={handleCloseModal} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div>
                <label className="block font-medium text-gray-700 mb-1">Nome da Instituição</label>
                <input 
                  type="text" 
                  required
                  value={formData.nome}
                  onChange={(e) => setFormData({...formData, nome: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-200 rounded-xl outline-none focus:border-blue-500"
                  placeholder="Ex: Hospital Municipal"
                />
              </div>

              <div>
                <label className="block font-medium text-gray-700 mb-1">E-mail</label>
                <input 
                  type="email" 
                  required
                  value={formData.email}
                  onChange={(e) => setFormData({...formData, email: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-200 rounded-xl outline-none focus:border-blue-500"
                  placeholder="instituicao@email.com"
                />
              </div>

              <div>
                <label className="block font-medium text-gray-700 mb-1">Telefone</label>
                <input 
                  type="text" 
                  value={formData.telefone}
                  onChange={(e) => setFormData({...formData, telefone: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-200 rounded-xl outline-none focus:border-blue-500"
                  placeholder="(11) 99999-9999"
                />
              </div>

              {/* Seletor de Tipo de Instituição (Hospital, UBS ou Clínica) */}
              <div>
                <label className="block font-medium text-gray-700 mb-1">Tipo de Instituição</label>
                <select 
                  value={formData.tipoInstituicao}
                  onChange={(e) => setFormData({...formData, tipoInstituicao: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-200 rounded-xl outline-none focus:border-blue-500 bg-white"
                >
                  <option value="Hospital">Hospital</option>
                  <option value="UBS">UBS</option>
                  <option value="Clínica">Clínica</option>
                </select>
              </div>

              <div>
                <label className="block font-medium text-gray-700 mb-1">Endereço</label>
                <input 
                  type="text" 
                  value={formData.endereco}
                  onChange={(e) => setFormData({...formData, endereco: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-200 rounded-xl outline-none focus:border-blue-500"
                  placeholder="Rua, Número, Bairro"
                />
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t border-gray-100">
                <button 
                  type="button"
                  onClick={handleCloseModal}
                  className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl font-medium cursor-pointer"
                >
                  Cancelar
                </button>
                <button 
                  type="submit"
                  disabled={saveMutation.isPending}
                  className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl font-medium cursor-pointer shadow-sm"
                >
                  {saveMutation.isPending ? "A salvar..." : "Salvar"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default InstituicoesPage;