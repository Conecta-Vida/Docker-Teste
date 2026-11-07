import { Edit, Trash2, Mail, Shield, Loader2, AlertCircle } from 'lucide-react';
import { useUsuarios } from '../api/getUsuarios';
import { useDeleteUsuario } from '../api/deleteUsuario';
import type { Usuario } from '../types';

interface UsuariosListProps {
  onEdit: (usuario: Usuario) => void;
}

export const UsuariosList = ({ onEdit }: UsuariosListProps) => {
  const { data: usuarios, isLoading, isError } = useUsuarios();
  const { mutateAsync: apagarUsuario, isPending: isDeleting } = useDeleteUsuario();

  const handleDelete = async (id: string, nome: string) => {
    if (window.confirm(`Tem a certeza que deseja remover o acesso do utilizador "${nome}"? Esta ação não pode ser desfeita.`)) {
      try {
        await apagarUsuario(id);
      } catch (error) {
        console.error('Erro ao remover utilizador:', error);
        alert('Não foi possível remover o utilizador. Verifique a consola.');
      }
    }
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-12 flex flex-col items-center justify-center text-blue-600 animate-fade-in">
        <Loader2 className="w-8 h-8 animate-spin mb-4" />
        <p className="text-gray-500 font-medium">A carregar utilizadores...</p>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="bg-white rounded-xl shadow-sm border border-red-100 p-12 flex flex-col items-center justify-center text-red-500 animate-fade-in">
        <AlertCircle className="w-8 h-8 mb-4" />
        <p className="font-medium text-center">Erro ao carregar utilizadores do servidor.</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden animate-fade-in">
      <table className="w-full text-left border-collapse">
        <thead>
          <tr className="bg-gray-50 border-b border-gray-100 text-sm text-gray-500">
            <th className="p-4 font-medium">Nome e Email</th>
            <th className="p-4 font-medium">Perfil</th>
            <th className="p-4 font-medium">Status</th>
            <th className="p-4 font-medium text-center">Ações</th>
          </tr>
        </thead>
        <tbody>
          {usuarios && usuarios.length > 0 ? (
            usuarios.map((user) => (
              <tr key={user.id} className="border-b border-gray-50 hover:bg-gray-50/50 transition-colors">
                <td className="p-4">
                  <p className="text-gray-900 font-medium">{user.nome}</p>
                  <div className="flex items-center gap-1 text-xs text-gray-500 mt-1">
                    <Mail size={12} />
                    {user.email}
                  </div>
                </td>
                <td className="p-4">
                  <div className="flex items-center gap-2 text-gray-600">
                    <Shield size={16} className="text-gray-400" />
                    {user.perfil}
                  </div>
                </td>
                <td className="p-4">
                  <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                    user.status === 'Ativo' ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-700'
                  }`}>
                    {user.status}
                  </span>
                </td>
                <td className="p-4 flex justify-center gap-3">
                  <button 
                    onClick={() => onEdit(user)}
                    className="text-blue-600 hover:text-blue-800 transition-colors" 
                    title="Editar"
                  >
                    <Edit size={18} />
                  </button>
                  <button 
                    onClick={() => handleDelete(user.id, user.nome)}
                    disabled={isDeleting}
                    className="text-red-600 hover:text-red-800 transition-colors disabled:opacity-50" 
                    title="Remover"
                  >
                    <Trash2 size={18} />
                  </button>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan={4} className="p-8 text-center text-gray-500">
                Nenhum utilizador registado no banco de dados.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};