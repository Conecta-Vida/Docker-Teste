import { useState, useEffect } from 'react';
import { X } from 'lucide-react';
import { useCreateInstituicao } from '../api/createInstituicao';
import { useUpdateInstituicao } from '../api/updateInstituicao';
import type { InstituicaoSaude } from '../types';

interface InstituicaoModalProps {
  isOpen: boolean;
  onClose: () => void;
  instituicaoEditando?: InstituicaoSaude | null; // Nova propriedade!
}

const estadoInicial = { nome: '', tipo: 'Posto de Saúde', endereco: '', cidade: '', telefone: '', status: 'Ativo' };

export const InstituicaoModal = ({ isOpen, onClose, instituicaoEditando }: InstituicaoModalProps) => {
  const { mutateAsync: criar, isPending: isCreating } = useCreateInstituicao();
  const { mutateAsync: atualizar, isPending: isUpdating } = useUpdateInstituicao();
  
  const [formData, setFormData] = useState<any>(estadoInicial);

  // Se recebermos uma instituição para editar, preenchemos o formulário
  useEffect(() => {
    if (instituicaoEditando) {
      setFormData(instituicaoEditando);
    } else {
      setFormData(estadoInicial);
    }
  }, [instituicaoEditando, isOpen]);

  if (!isOpen) return null;

  const isPending = isCreating || isUpdating;
  const isEdicao = !!instituicaoEditando;

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
      console.error('Erro ao guardar:', error);
      alert('Ocorreu um erro ao guardar. Verifique a consola.');
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 animate-fade-in">
      <div className="bg-white rounded-xl shadow-lg w-full max-w-md overflow-hidden">
        <div className="flex justify-between items-center p-6 border-b border-gray-100">
          <h2 className="text-xl font-bold text-gray-900">
            {isEdicao ? 'Editar Instituição' : 'Nova Instituição'}
          </h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition-colors">
            <X size={24} />
          </button>
        </div>
        
        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Nome</label>
            <input required type="text" className="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 outline-none"
              value={formData.nome} onChange={(e) => setFormData({...formData, nome: e.target.value})} />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Tipo</label>
              <select className="w-full border border-gray-300 rounded-lg p-2.5 bg-white focus:ring-2 focus:ring-blue-500 outline-none"
                value={formData.tipo} onChange={(e) => setFormData({...formData, tipo: e.target.value})}>
                <option value="Hospital">Hospital</option>
                <option value="Posto de Saúde">Posto de Saúde</option>
                <option value="Clínica">Clínica</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Cidade</label>
              <input required type="text" className="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 outline-none"
                value={formData.cidade} onChange={(e) => setFormData({...formData, cidade: e.target.value})} />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Endereço</label>
            <input required type="text" className="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 outline-none"
              value={formData.endereco} onChange={(e) => setFormData({...formData, endereco: e.target.value})} />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Telefone</label>
            <input required type="text" className="w-full border border-gray-300 rounded-lg p-2.5 focus:ring-2 focus:ring-blue-500 outline-none"
              value={formData.telefone} onChange={(e) => setFormData({...formData, telefone: e.target.value})} />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <select className="w-full border border-gray-300 rounded-lg p-2.5 bg-white focus:ring-2 focus:ring-blue-500 outline-none"
              value={formData.status} onChange={(e) => setFormData({...formData, status: e.target.value})}>
              <option value="Ativo">Ativo</option>
              <option value="Inativo">Inativo</option>
            </select>
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