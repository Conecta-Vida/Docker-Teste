import { Edit, Trash2, Calendar, Loader2, AlertCircle } from 'lucide-react';
import { useCampanhas } from '../api/getCampanhas';
import { useDeleteCampanha } from '../api/deleteCampanha';
import type { Campanha } from '../types';

interface CampanhasListProps {
  onEdit: (campanha: Campanha) => void;
}

export const CampanhasList = ({ onEdit }: CampanhasListProps) => {
  const { data: campanhas, isLoading, isError } = useCampanhas();
  const { mutateAsync: apagarCampanha, isPending: isDeleting } = useDeleteCampanha();

  const handleDelete = async (id: string, titulo: string) => {
    if (window.confirm(`Tem a certeza que deseja apagar a campanha "${titulo}"? Esta ação é irreversível.`)) {
      try {
        await apagarCampanha(id);
      } catch (error) {
        console.error('Erro ao apagar campanha:', error);
        alert('Não foi possível apagar. Verifique a consola.');
      }
    }
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-12 flex flex-col items-center justify-center text-blue-600 animate-fade-in">
        <Loader2 className="w-8 h-8 animate-spin mb-4" />
        <p className="text-gray-500 font-medium">A carregar campanhas...</p>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="bg-white rounded-xl shadow-sm border border-red-100 p-12 flex flex-col items-center justify-center text-red-500 animate-fade-in">
        <AlertCircle className="w-8 h-8 mb-4" />
        <p className="font-medium text-center">Erro ao carregar campanhas.</p>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden animate-fade-in">
      <table className="w-full text-left border-collapse">
        <thead>
          <tr className="bg-gray-50 border-b border-gray-100 text-sm text-gray-500">
            <th className="p-4 font-medium">Título da Campanha</th>
            <th className="p-4 font-medium">Período</th>
            <th className="p-4 font-medium">Status</th>
            <th className="p-4 font-medium text-center">Ações</th>
          </tr>
        </thead>
        <tbody>
          {campanhas && campanhas.length > 0 ? (
            campanhas.map((campanha) => (
              <tr key={campanha.id} className="border-b border-gray-50 hover:bg-gray-50/50 transition-colors">
                <td className="p-4 text-gray-900 font-medium">
                  {campanha.titulo}
                  <p className="text-xs text-gray-500 font-normal mt-1 truncate max-w-xs">{campanha.descricao}</p>
                </td>
                <td className="p-4 text-gray-600 flex items-center gap-2">
                  <Calendar size={16} className="text-gray-400" />
                  <span className="text-sm">
                    {new Date(campanha.dataInicio).toLocaleDateString()} - {new Date(campanha.dataFim).toLocaleDateString()}
                  </span>
                </td>
                <td className="p-4">
                  <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                    campanha.status === 'Ativa' ? 'bg-emerald-100 text-emerald-700' : 
                    campanha.status === 'Rascunho' ? 'bg-yellow-100 text-yellow-700' : 
                    'bg-gray-100 text-gray-700'
                  }`}>
                    {campanha.status}
                  </span>
                </td>
                <td className="p-4 flex justify-center gap-3">
                  <button 
                    onClick={() => onEdit(campanha)}
                    className="text-blue-600 hover:text-blue-800 transition-colors" 
                    title="Editar"
                  >
                    <Edit size={18} />
                  </button>
                  <button 
                    onClick={() => handleDelete(campanha.id, campanha.titulo)}
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
                Nenhuma campanha registada no banco de dados.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
};