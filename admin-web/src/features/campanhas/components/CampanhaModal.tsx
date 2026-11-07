import React, { useState, useEffect } from 'react';
import { useCreateCampanha } from '../api/createCampanha';
import { useUpdateCampanha } from '../api/updateCampanha';
import type { Campanha } from '../types';

interface CampanhaModalProps {
  isOpen: boolean;
  onClose: () => void;
  campanhaParaEditar?: Campanha | null;
}

export const CampanhaModal: React.FC<CampanhaModalProps> = ({
  isOpen,
  onClose,
  campanhaParaEditar,
}) => {
  const [titulo, setTitulo] = useState('');
  const [descricao, setDescricao] = useState('');
  const [categoria, setCategoria] = useState('');
  const [linkimagem, setLinkimagem] = useState('');
  const [localizacao, setLocalizacao] = useState('');
  const [publicoAlvo, setPublicoAlvo] = useState('');
  const [dataInicio, setDataInicio] = useState('');
  const [dataFim, setDataFim] = useState('');
  const [status, setStatus] = useState<'Ativa' | 'Encerrada' | 'Rascunho'>('Ativa');

  const createMutation = useCreateCampanha();
  const updateMutation = useUpdateCampanha();

  useEffect(() => {
    if (campanhaParaEditar) {
      setTitulo(campanhaParaEditar.titulo || '');
      setDescricao(campanhaParaEditar.descricao || '');
      setCategoria(campanhaParaEditar.categoria || '');
      setLinkimagem(campanhaParaEditar.linkimagem || '');
      setLocalizacao(campanhaParaEditar.localizacao || '');
      setPublicoAlvo(campanhaParaEditar.publico_alvo || '');
      setDataInicio(campanhaParaEditar.dataInicio ? campanhaParaEditar.dataInicio.split('T')[0] : '');
      setDataFim(campanhaParaEditar.dataFim ? campanhaParaEditar.dataFim.split('T')[0] : '');
      setStatus((campanhaParaEditar.status as any) || 'Ativa');
    } else {
      setTitulo('');
      setDescricao('');
      setCategoria('');
      setLinkimagem('');
      setLocalizacao('');
      setPublicoAlvo('');
      setDataInicio('');
      setDataFim('');
      setStatus('Ativa');
    }
  }, [campanhaParaEditar, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const payload = {
      titulo,
      descricao,
      categoria,
      linkimagem,
      localizacao,
      publico_alvo: publicoAlvo,
      tipo: 'Campanha',
      status,
      dataInicio: dataInicio ? `${dataInicio}T00:00:00` : undefined,
      dataFim: dataFim ? `${dataFim}T23:59:59` : undefined,
    };

    try {
      if (campanhaParaEditar && campanhaParaEditar.id) {
        await updateMutation.mutateAsync({
          ...campanhaParaEditar,
          ...payload,
        } as Campanha);
      } else {
        await createMutation.mutateAsync(payload as any);
      }
      onClose();
    } catch (error) {
      console.error('Erro ao guardar campanha:', error);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4 overflow-y-auto">
      <div className="relative w-full max-w-2xl rounded-xl bg-white p-6 shadow-2xl my-8 z-10">
        <div className="flex justify-between items-center pb-3 mb-4 border-b border-gray-100">
          <h2 className="text-xl font-bold text-gray-800">
            {campanhaParaEditar ? 'Editar Campanha' : 'Nova Campanha'}
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 font-bold text-lg"
          >
            &times;
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Título</label>
              <input
                type="text"
                required
                value={titulo}
                onChange={(e) => setTitulo(e.target.value)}
                className="w-full rounded-lg border border-gray-300 p-2.5 text-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-100 focus:outline-none"
                placeholder="Título da campanha"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Categoria</label>
              <input
                type="text"
                value={categoria}
                onChange={(e) => setCategoria(e.target.value)}
                className="w-full rounded-lg border border-gray-300 p-2.5 text-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-100 focus:outline-none"
                placeholder="Ex: Saúde, Vacinação"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">Descrição</label>
            <textarea
              required
              rows={3}
              value={descricao}
              onChange={(e) => setDescricao(e.target.value)}
              className="w-full rounded-lg border border-gray-300 p-2.5 text-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-100 focus:outline-none"
              placeholder="Detalhes completos..."
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Localização</label>
              <input
                type="text"
                value={localizacao}
                onChange={(e) => setLocalizacao(e.target.value)}
                className="w-full rounded-lg border border-gray-300 p-2.5 text-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-100 focus:outline-none"
                placeholder="Local ou endereço"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Público-Alvo</label>
              <input
                type="text"
                value={publicoAlvo}
                onChange={(e) => setPublicoAlvo(e.target.value)}
                className="w-full rounded-lg border border-gray-300 p-2.5 text-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-100 focus:outline-none"
                placeholder="Ex: Idosos, Gestantes"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-1">Link da Imagem</label>
            <input
              type="text"
              value={linkimagem}
              onChange={(e) => setLinkimagem(e.target.value)}
              className="w-full rounded-lg border border-gray-300 p-2.5 text-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-100 focus:outline-none"
              placeholder="https://exemplo.com/imagem.jpg"
            />
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Data de Início</label>
              <input
                type="date"
                value={dataInicio}
                onChange={(e) => setDataInicio(e.target.value)}
                className="w-full rounded-lg border border-gray-300 p-2.5 text-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-100 focus:outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Data de Fim</label>
              <input
                type="date"
                value={dataFim}
                onChange={(e) => setDataFim(e.target.value)}
                className="w-full rounded-lg border border-gray-300 p-2.5 text-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-100 focus:outline-none"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-1">Status</label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value as any)}
                className="w-full rounded-lg border border-gray-300 p-2.5 text-sm focus:border-blue-500 focus:ring-2 focus:ring-blue-100 focus:outline-none bg-white"
              >
                <option value="Ativa">Ativa</option>
                <option value="Encerrada">Encerrada</option>
                <option value="Rascunho">Rascunho</option>
              </select>
            </div>
          </div>

          <div className="flex justify-end space-x-3 pt-4 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg bg-gray-100 px-4 py-2 text-sm font-medium text-gray-700 hover:bg-gray-200 transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={createMutation.isPending || updateMutation.isPending}
              className="rounded-lg bg-blue-600 px-5 py-2 text-sm font-semibold text-white hover:bg-blue-700 disabled:opacity-50 transition-colors shadow-sm"
            >
              {createMutation.isPending || updateMutation.isPending ? 'A guardar...' : 'Guardar'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};