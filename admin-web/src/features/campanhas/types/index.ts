export interface Campanha {
  id?: number;
  tipo?: string;
  instituicao_id?: number;
  titulo?: string;
  descricao?: string;
  categoria?: string;
  linkimagem?: string;
  localizacao?: string;
  publico_alvo?: string;
  status?: string;
  lido?: boolean;
  dataPostada?: string;
  dataInicio?: string;
  dataFim?: string;
  [key: string]: any;
}