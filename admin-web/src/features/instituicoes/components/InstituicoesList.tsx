import { Edit, Trash2, MapPin, Loader2, AlertCircle } from 'lucide-react';
import { useInstituicoes } from '../api/getInstituicoes';
import { useDeleteInstituicao } from '../api/deleteInstituicao';
import type { InstituicaoSaude } from '../types';

interface InstituicoesListProps {
  onEdit: (instituicao: InstituicaoSaude) => void;
}

export const InstituicoesList = ({ onEdit }: InstituicoesListProps) => {
  const { data: instituicoes, isLoading, isError } = useInstituicoes();
  const { mutateAsync: apagarInstituicao, isPending: isDeleting } = useDeleteInstituicao();

  const handleDelete = async (id: string, nome: string) => {
    if (window.confirm(`Tem a certeza que deseja apagar a instituição "${nome}"? Esta ação não pode ser desfeita.`)) {
      try {
        await apagarInstituicao(id);
      } catch (error) {
        console.error('Erro ao apagar:', error);
        alert('Não foi possível apagar a instituição. Verifique a consola.');
      }
    }
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-12 flex flex-col items-center justify-center text-blue-600 animate-fade-in">
        <Loader2 className="w-8 h-8 animate-spin mb-4" />
        <p className="text-gray-500 font-medium">A carregar instituições...</p>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="bg-white rounded-xl shadow-sm border border-red-100 p-12 flex flex-col items-center justify-center text-red-500 animate-fade-in">
        <AlertCircle className="w-8 h-8 mb-4" />
        <p className="font-medium text-center">Erro ao comunicar com o servidor.</p>
        <p className="text-sm text-gray-500 mt-1 text-center">Verifique se a API Spring Boot está a correr na porta 8080.</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden animate-fade-in">
      <table className="w-full text-left border-collapse">
        <thead>
          <tr className="bg-gray-50 border-b border-gray-100 text-sm text-gray-500">
            <th className="p-4 font-medium">Nome da Instituição</th>
            <th className="p-4 font-medium">Tipo</th>
            <th className="p-4 font-medium">Localização</th>
            <th className="p-4 font-medium">Status</th>
            <th className="p-4 font-medium text-center">Ações</th>
          </tr>
        </thead>
        <tbody>
          {instituicoes && instituicoes.length > 0 ? (
            instituicoes.map((inst) => (
              <tr key={inst.id} className="border-b border-gray-50 hover:bg-gray-50/50 transition-colors">
                <td className="p-4 text-gray-900 font-medium">{inst.nome}</td>
                <td className="p-4 text-gray-600">{inst.tipo}</td>
                <td className="p-4 text-gray-600 flex items-center gap-2">
                  <MapPin size={16} className="text-gray-400" />
                  {inst.cidade}
                </td>
                <td className="p-4">
                  <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                    inst.status === 'Ativo' ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'
                  }`}>
                    {inst.status}
                  </span>
                </td>
                <td className="p-4 flex justify-center gap-3">
                  <button 
                    onClick={() => onEdit(inst)}
                    className="text-blue-600 hover:text-blue-800 transition-colors" 
                    title="Editar"
                  >
                    <Edit size={18} />
                  </button>
                  <button 
                    onClick={() => handleDelete(inst.id, inst.nome)}
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
              <td colSpan={5} className="p-8 text-center text-gray-500">
                Nenhuma instituição registada no banco de dados.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};