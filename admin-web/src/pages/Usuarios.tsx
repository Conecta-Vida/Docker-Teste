import React, { useState, useRef } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/api";
import { Users, Search, Plus, Download, Printer, Upload, X } from "lucide-react";

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  permissao: string;
  telefone?: string;
}

export const UsuariosPage: React.FC = () => {
  const queryClient = useQueryClient();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [searchTerm, setSearchTerm] = useState("");

  const [formData, setFormData] = useState({
    nome: "",
    email: "",
    senha: "",
    permissao: "COMUM"
  });

  const { data: usuarios, isLoading } = useQuery({
    queryKey: ['usuarios'],
    queryFn: async () => {
      const response = await api.get<Usuario[]>('/api/usuarios');
      return response.data;
    },
  });

  const saveMutation = useMutation({
    mutationFn: async (data: typeof formData) => {
      if (editingId) {
        return await api.put(`/api/usuarios/${editingId}`, data);
      } else {
        return await api.post('/api/usuarios', data);
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      handleCloseModal();
    }
  });

  const deleteMutation = useMutation({
    mutationFn: async (id: number) => {
      return await api.delete(`/api/usuarios/${id}`);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
    }
  });

  const handleOpenModal = (user?: Usuario) => {
    if (user) {
      setEditingId(user.id);
      setFormData({
        nome: user.nome || "",
        email: user.email || "",
        senha: "",
        permissao: user.permissao || "COMUM"
      });
    } else {
      setEditingId(null);
      setFormData({ nome: "", email: "", senha: "", permissao: "COMUM" });
    }
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingId(null);
    setFormData({ nome: "", email: "", senha: "", permissao: "COMUM" });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    saveMutation.mutate(formData);
  };

  // Exportar para CSV
  const handleExportCSV = () => {
    if (!usuarios || usuarios.length === 0) return;
    const headers = ["ID,Nome,E-mail,Permissão\n"];
    const rows = usuarios.map(u => `${u.id},"${u.nome}","${u.email}",${u.permissao}`).join("\n");
    const blob = new Blob([headers + rows], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", `usuarios_conectavida_${new Date().toISOString().slice(0,10)}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // Importar de arquivo CSV manualmente
  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = async (event) => {
      const text = event.target?.result as string;
      const lines = text.split("\n");
      // Ignora o cabeçalho (linha 0) e processa as linhas
      for (let i = 1; i < lines.length; i++) {
        const line = lines[i].trim();
        if (!line) continue;
        const parts = line.split(",");
        if (parts.length >= 3) {
          const nome = parts[1]?.replace(/"/g, "") || "Usuário Importado";
          const email = parts[2]?.replace(/"/g, "") || "";
          const permissao = parts[3]?.replace(/"/g, "").trim() || "COMUM";

          try {
            await api.post('/api/usuarios', {
              nome,
              email,
              senha: "TempPassword123!", // Senha temporária padrão para importações manuais
              permissao
            });
          } catch (err) {
            console.error("Erro ao importar linha:", err);
          }
        }
      }
      queryClient.invalidateQueries({ queryKey: ['usuarios'] });
      alert("Importação concluída com sucesso!");
    };
    reader.readAsText(file);
  };

  const handlePrintPDF = () => {
    window.print();
  };

  const filteredUsuarios = usuarios?.filter(user => 
    user.nome.toLowerCase().includes(searchTerm.toLowerCase()) ||
    user.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="p-6 lg:p-8 max-w-7xl mx-auto space-y-6 w-full overflow-x-hidden">
      {/* Cabeçalho */}
      <div className="bg-white p-5 rounded-xl shadow-sm border border-gray-100 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
        <div>
          <h1 className="text-xl font-bold text-gray-900 tracking-tight">Gerenciamento de Utilizadores</h1>
          <p className="text-xs text-gray-500 mt-0.5">Visualize, controle, importe e exporte os utilizadores.</p>
        </div>
        
        {/* Input file invisível para importação */}
        <input 
          type="file" 
          ref={fileInputRef} 
          onChange={handleFileUpload} 
          accept=".csv" 
          className="hidden" 
        />

        <div className="flex items-center gap-2 flex-wrap">
          <button 
            onClick={() => fileInputRef.current?.click()}
            className="bg-amber-50 text-amber-700 border border-amber-200 hover:bg-amber-100 px-3 py-2 rounded-xl text-xs font-medium transition-colors flex items-center gap-1.5 cursor-pointer shadow-sm"
            title="Importar lista de usuários em CSV"
          >
            <Upload className="w-4 h-4" />
            <span>Importar CSV</span>
          </button>

          <button 
            onClick={handleExportCSV}
            className="bg-emerald-50 text-emerald-700 border border-emerald-200 hover:bg-emerald-100 px-3 py-2 rounded-xl text-xs font-medium transition-colors flex items-center gap-1.5 cursor-pointer shadow-sm"
            title="Exportar para CSV/Excel"
          >
            <Download className="w-4 h-4" />
            <span>Exportar CSV</span>
          </button>

          <button 
            onClick={handlePrintPDF}
            className="bg-gray-50 text-gray-700 border border-gray-200 hover:bg-gray-100 px-3 py-2 rounded-xl text-xs font-medium transition-colors flex items-center gap-1.5 cursor-pointer shadow-sm"
            title="Imprimir / Salvar PDF"
          >
            <Printer className="w-4 h-4" />
            <span>Imprimir / PDF</span>
          </button>

          <button 
            onClick={() => handleOpenModal()}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-xl text-xs font-medium transition-colors flex items-center gap-2 cursor-pointer shadow-sm"
          >
            <Plus className="w-4 h-4" />
            <span>Novo Utilizador</span>
          </button>
        </div>
      </div>

      {/* Contentor da Tabela Compacta */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="p-4 border-b border-gray-100 flex items-center gap-2">
          <Search className="w-4 h-4 text-gray-400 shrink-0" />
          <input 
            type="text" 
            placeholder="Pesquisar utilizadores..." 
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
                <th className="py-2.5 px-4">Permissão</th>
                <th className="py-2.5 px-4 text-right">Ações</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 text-xs">
              {isLoading ? (
                <tr>
                  <td colSpan={4} className="py-8 text-center text-gray-400">A carregar utilizadores...</td>
                </tr>
              ) : !filteredUsuarios || filteredUsuarios.length === 0 ? (
                <tr>
                  <td colSpan={4} className="py-8 text-center text-gray-400">Nenhum utilizador encontrado.</td>
                </tr>
              ) : (
                filteredUsuarios.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-50/50 transition-colors">
                    <td className="py-3 px-4 font-medium text-gray-900 flex items-center gap-2">
                      <Users className="w-4 h-4 text-indigo-600 shrink-0" />
                      <span className="truncate">{user.nome}</span>
                    </td>
                    <td className="py-3 px-4 text-gray-600 truncate">{user.email}</td>
                    <td className="py-3 px-4">
                      <span className={`px-2.5 py-0.5 rounded-md text-[10px] font-semibold ${
                        user.permissao === 'ADMIN' || user.permissao === 'ADMINISTRADOR'
                          ? 'bg-purple-50 text-purple-700 border border-purple-100'
                          : 'bg-blue-50 text-blue-700 border border-blue-100'
                      }`}>
                        {user.permissao || 'COMUM'}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-right space-x-3">
                      <button 
                        onClick={() => handleOpenModal(user)}
                        className="text-blue-600 hover:text-blue-800 font-medium cursor-pointer"
                      >
                        Editar
                      </button>
                      <button 
                        onClick={() => {
                          if (window.confirm("Tem certeza que deseja excluir este utilizador?")) {
                            deleteMutation.mutate(user.id);
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

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl shadow-xl max-w-md w-full p-6 space-y-4">
            <div className="flex justify-between items-center border-b border-gray-100 pb-3">
              <h2 className="text-base font-bold text-gray-900">
                {editingId ? "Editar Utilizador" : "Novo Utilizador"}
              </h2>
              <button onClick={handleCloseModal} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-3 text-xs">
              <div>
                <label className="block font-medium text-gray-700 mb-1">Nome Completo</label>
                <input 
                  type="text" 
                  required
                  value={formData.nome}
                  onChange={(e) => setFormData({...formData, nome: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-200 rounded-xl outline-none focus:border-blue-500"
                  placeholder="Ex: João da Silva"
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
                  placeholder="joao@email.com"
                />
              </div>

              <div>
                <label className="block font-medium text-gray-700 mb-1">
                  {editingId ? "Nova Senha (deixe em branco para manter)" : "Senha"}
                </label>
                <input 
                  type="password" 
                  {...(!editingId ? { required: true } : {})}
                  value={formData.senha}
                  onChange={(e) => setFormData({...formData, senha: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-200 rounded-xl outline-none focus:border-blue-500"
                  placeholder="••••••••"
                />
              </div>

              <div>
                <label className="block font-medium text-gray-700 mb-1">Permissão</label>
                <select 
                  value={formData.permissao}
                  onChange={(e) => setFormData({...formData, permissao: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-200 rounded-xl outline-none focus:border-blue-500 bg-white"
                >
                  <option value="COMUM">COMUM</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
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

export default UsuariosPage;