export interface Instituicao {
  id?: string;
  nome: string;
  tipo: string;
  endereco: string;
  cidade: string;
  telefone: string;
  status: string;
}
// Nome usado pelos componentes de listagem/modal
export type InstituicaoSaude = Instituicao;
