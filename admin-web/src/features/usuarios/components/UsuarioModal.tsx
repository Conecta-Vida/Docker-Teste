import { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { useCreateUsuario } from '../api/createUsuario';
import { useUpdateUsuario } from '../api/updateUsuario';
import type { Usuario } from '../types';

interface UsuarioModalProps {
  isOpen: boolean;
  onClose: () => void;
  usuarioEditando?: Usuario | null;
}

const estadoInicial = { nome: '', email: '', perfil: 'Profissional', status: 'Ativo' };

export const UsuarioModal = ({ isOpen, onClose, usuarioEditando }: UsuarioModalProps) => {
  const { mutateAsync: criar, isPending: isCreating } = useCreateUsuario();
  const { mutateAsync: atualizar, isPending: isUpdating } = useUpdateUsuario();
  
  const [formData, setFormData] = useState<any>(estadoInicial);

  useEffect(() => {
    if (usuarioEditando) {
      setFormData(usuarioEditando);
    } else {
      setFormData(estadoInicial);
    }
  }, [usuarioEditando, isOpen]);

  if (!isOpen) return null;

  const isPending = isCreating || isUpdating;
  const isEdicao = !!usuarioEditando;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (isEdicao) {
        await atualizar(formData);
      } else {
        await criar(formData);
      }
      onClose();
    } catch (error) {
      console.error('Erro ao guardar utilizador:', error);
      alert('Ocorreu um erro ao guardar. Verifique a consola.');
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 animate-fade-in">
      <div className="bg-white rounded-xl shadow-lg w-full max-w-md overflow-hidden">
        <div className="flex justify-between items-center p-6 border-b border-gray-100">
          <h2 className="text-xl font-bold text-gray-900">
            {isEdicao ? 'Editar Utilizador' : 'Novo Utilizador'}
          </h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition-colors">
            <X size={24} />
          </button>
        </div>
        
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Nome Completo</label>
            <input required type="text" placeholder="Ex: Maria Silva" className="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 outline-none"
              value={formData.nome} onChange={(e) => setFormData({...formData, nome: e.target.value})} />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">E-mail corporativo</label>
            <input required type="email" placeholder="maria@conectavida.com" className="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 outline-none"
              value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Perfil</label>
              <select className="w-full border border-gray-300 rounded-lg p-2.5 bg-white focus:ring-2 focus:ring-blue-500 outline-none"
                value={formData.perfil} onChange={(e) => setFormData({...formData, perfil: e.target.value})}>
                <option value="Administrador">Administrador</option>
                <option value="Gestor de Saúde">Gestor de Saúde</option>
                <option value="Profissional">Profissional</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
              <select className="w-full border border-gray-300 rounded-lg p-2.5 bg-white focus:ring-2 focus:ring-blue-500 outline-none"
                value={formData.status} onChange={(e) => setFormData({...formData, status: e.target.value})}>
                <option value="Ativo">Ativo</option>
                <option value="Inativo">Inativo</option>
              </select>
            </div>
          </div>

          <div className="pt-4 flex justify-end gap-3">
            <button type="button" onClick={onClose} className="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg font-medium transition-colors">
              Cancelar
            </button>
            <button type="submit" disabled={isPending} className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors disabled:opacity-50">
              {isPending ? 'A Guardar...' : 'Guardar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};