export interface Usuario {
  id: string;
  nome: string;
  email: string;
  perfil: 'Administrador' | 'Gestor de Saúde' | 'Profissional';
  status: 'Ativo' | 'Inativo';
}