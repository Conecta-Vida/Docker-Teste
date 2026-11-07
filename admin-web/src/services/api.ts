import axios from 'axios';

// ===================================================================
// CONTRATOS DE DADOS / INTERFACES (ALINHADOS COM AS ENTIDADES JAVA)
// ===================================================================

export interface Usuario {
  id?: number;
  nome: string;
  email: string;
  senha?: string;
  dataNascimento?: number; // 🟢 Alterado de idade para dataNascimento
  sexo?: string;
  localizacao?: string; 
  permissao?: string;
}

export interface InstituicaoSaude {
  id?: number;
  tipoInstituicao: string; 
  nome: string;
  email?: string;
  telefone?: string;
  linksite?: string;
  endereco?: string;
  horarioSegSex?: string;
  horarioSabado?: string;
  horarioDomingo?: string;
}

export interface LogAtividade {
  id?: number;
  usuario?: Usuario;
  acao: string;
  dataHora: string;
}

export interface Alerta {
  id?: number;
  tipo?: 'ALERTA';
  categoria: string; 
  titulo: string;
  descricao: string;
  localizacao?: string;
  lido: boolean;
  dataPostada?: string;
}

export interface Campanha {
  id?: number;
  tipo?: 'CAMPANHA';
  titulo: string;
  descricao: string;
  categoria?: string;
  linkimagem?: string;
  localizacao?: string;
  dataInicio: string;
  dataFim: string;
  publicoAlvo?: string;
  status: string;
}

export interface DashboardStats {
  totalUsuarios: number;
  alertasAtivos: number;
  campanhasAtivas: number;
  noticiasPublicadas: number;
}

export interface ChartData {
  mes: string;
  quantidade: number;
}

// ===================================================================
// CONFIGURAÇÃO CENTRAL DA INSTÂNCIA DO AXIOS
// ===================================================================

const api = axios.create({
  baseURL: 'https://conecta-vida-api.onrender.com/api', // 🚀 Apontando corretamente para o seu link do Render!
  timeout: 12000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * 🔒 INTERCEPTOR DE REQUISIÇÃO (REQUEST)
 * Injeta o token Bearer automaticamente se o usuário estiver autenticado.
 */
api.interceptors.request.use(
  (config) => {
    const dadosSessao = localStorage.getItem('@conecta:admin');
    if (dadosSessao) {
      const { token } = JSON.parse(dadosSessao);
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * 🛡️ INTERCEPTOR DE RESPOSTA (RESPONSE)
 * Desloga o usuário se o token expirar (HTTP 401).
 */
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('@conecta:admin');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ===================================================================
// MAPEAMENTO DOS MICROSSERVIÇOS DA API (SERVICOS ASSÍNCRONOS)
// ===================================================================

export const authService = {
  login: async (email: string, senha: string): Promise<any> => {
    const response = await api.post('/auth/login', { email, senha });
    return response.data;
  },
  logout: async (): Promise<void> => {
    await api.post('/auth/logout');
  }
};

export const usuarioService = {
  listarTodos: async (): Promise<Usuario[]> => {
    const response = await api.get('/usuarios');
    return response.data;
  },
  cadastrar: async (dados: Usuario): Promise<Usuario> => {
    const response = await api.post('/usuarios', dados);
    return response.data;
  },
  atualizar: async (id: number, dados: Usuario): Promise<Usuario> => {
    const response = await api.put(`/usuarios/${id}`, dados);
    return response.data;
  },
  deletar: async (id: number): Promise<void> => {
    await api.delete(`/usuarios/${id}`);
  },
  importarCsv: async (file: File): Promise<string> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/usuarios/importar-csv', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    return response.data;
  }
};

export const instituicaoService = {
  listarTodas: async (): Promise<InstituicaoSaude[]> => {
    const response = await api.get('/instituicoes');
    return response.data;
  },
  cadastrar: async (dados: InstituicaoSaude): Promise<InstituicaoSaude> => {
    const response = await api.post('/instituicoes', dados);
    return response.data;
  },
  atualizar: async (id: number, dados: InstituicaoSaude): Promise<InstituicaoSaude> => {
    const response = await api.put(`/instituicoes/${id}`, dados);
    return response.data;
  },
  deletar: async (id: number): Promise<void> => {
    // 🟢 CORRIGIDO: Removido prefixo duplicado /api/ e apontado para a rota certa do back-end
    await api.delete(`/instituicoes/${id}`);
  }
};

export const campanhaService = {
  listarTodas: async (): Promise<Campanha[]> => {
    const response = await api.get('/campanhas');
    return response.data;
  },
  buscarPorId: async (id: number): Promise<Campanha> => {
    const response = await api.get(`/campanhas/${id}`);
    return response.data;
  },
  cadastrar: async (dados: Campanha): Promise<Campanha> => {
    const response = await api.post('/comunicacoes', { ...dados, tipo: 'CAMPANHA' });
    return response.data;
  },
  atualizar: async (id: number, dados: Campanha): Promise<Campanha> => {
    const response = await api.put(`/campanhas/${id}`, dados);
    return response.data;
  },
  deletar: async (id: number): Promise<void> => {
    // 🟢 CORRIGIDO: Campanhas são deletadas via fluxo unificado de comunicações no Java
    await api.delete(`/comunicacoes/${id}`);
  }
};

export const alertaService = {
  listarTodos: async (): Promise<Alerta[]> => {
    const response = await api.get('/alertas/ativos');
    return response.data;
  },
  cadastrar: async (dados: Alerta): Promise<Alerta> => {
    const response = await api.post('/comunicacoes', { ...dados, tipo: 'ALERTA', lido: false });
    return response.data;
  },
  atualizar: async (id: number, dados: Alerta): Promise<Alerta> => {
    const response = await api.put(`/alertas/${id}`, dados);
    return response.data;
  },
  marcarComoLido: async (id: number): Promise<void> => {
    await api.put(`/alertas/${id}`, { lido: true });
  },
  deletar: async (id: number): Promise<void> => {
    // 🟢 CORRIGIDO: Alertas também são removidos de forma polimórfica pela rota unificada
    await api.delete(`/comunicacoes/${id}`);
  }
};

export const dashboardService = {
  getStats: async (): Promise<DashboardStats> => {
    const response = await api.get('/dashboard/stats');
    return response.data;
  },
  getChartData: async (): Promise<ChartData[]> => {
    const response = await api.get('/dashboard/chart');
    return response.data;
  }
};

export const logService = {
  listarRecentes: async (): Promise<LogAtividade[]> => {
    const response = await api.get('/dashboard/logs/recentes');
    return response.data;
  }
};

export const relatorioService = {
  getUrlDownloadPdf: (): string => {
    return 'https://conecta-vida-api.onrender.com/api/relatorios/usuarios'; // 🚀 Atualizado para o Render
  },
  getUrlExportarCsv: (): string => {
    return 'https://conecta-vida-api.onrender.com/api/relatorios/usuarios/csv'; // 🚀 Atualizado para o Render
  }
};

export default api;