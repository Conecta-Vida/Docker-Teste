-- Dados de DEMONSTRAÇÃO para o seminário (somente ambiente local/Swarm de laboratório).
-- Senhas em SHA-256 sem salt (formato legado aceito pela API, que migra o hash
-- para SHA-256 + salt no primeiro login).
--   admin@conectavida.com   / admin123    (Administrador -> painel web)
--   cidadao@conectavida.com / cidadao123  (Usuário Comum -> app mobile)

INSERT INTO public.usuarios (nome, email, senha, permissao, data_nascimento, sexo, localizacao) VALUES
  ('Administrador Conecta Vida', 'admin@conectavida.com', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrador', 1990, 'Não informado', 'Bragança Paulista'),
  ('Cidadão Demonstração', 'cidadao@conectavida.com', '024229ecaab0ef7e04b6c764b07d534d8d4769df5a24eb545a00b75c82d20e16', 'Usuário Comum', 2000, 'Masculino', 'Bragança Paulista');

INSERT INTO public.instituicoes_saude (nome, tipo_instituicao, telefone, email, endereco, linksite, horario_seg_sex, horario_sabado, horario_domingo) VALUES
  ('Secretaria Municipal de Saúde', 'SECRETARIA', '(11) 4034-0000', 'saude@braganca.sp.gov.br', 'Centro, Bragança Paulista - SP', 'https://www.braganca.sp.gov.br', '08:00 - 17:00', 'Fechado', 'Fechado'),
  ('UBS Centro', 'UBS', '(11) 4033-1111', 'ubs.centro@braganca.sp.gov.br', 'Rua Coronel Teófilo Leme, Centro', NULL, '07:00 - 19:00', '08:00 - 12:00', 'Fechado'),
  ('Hemocentro Regional', 'HEMOCENTRO', '(11) 4032-2222', 'hemocentro@braganca.sp.gov.br', 'Av. Dr. Tosta, Bragança Paulista', NULL, '07:30 - 16:00', '08:00 - 12:00', 'Fechado');

INSERT INTO public.comunicacoes (tipo, titulo, descricao, categoria, localizacao, status, lido, instituicao_id, data_inicio, data_fim, publico_alvo) VALUES
  ('NOTICIA', 'Calendário de vacinação atualizado', 'A Secretaria de Saúde divulgou o novo calendário de vacinação contra a gripe para 2026.', 'Vacinação', 'Bragança Paulista', 'Ativo', false, 1, now(), now() + interval '30 days', 'População Geral'),
  ('ALERTA', 'Aumento de casos de dengue', 'Foram registrados novos casos de dengue na região central. Elimine água parada.', 'Surtos Epidemiológicos', 'Bragança Paulista', 'Ativo', false, 1, now(), NULL, 'População Geral'),
  ('CAMPANHA', 'Doação de sangue - Hemocentro', 'Campanha de doação de sangue com estoque baixo dos tipos O- e A-.', 'Doação de Sangue', 'Bragança Paulista', 'ATIVA', false, 3, now(), now() + interval '15 days', 'Adultos de 16 a 69 anos');

INSERT INTO public.logs_atividade (usuario_id, acao) VALUES
  (1, 'Banco de demonstração inicializado pelo container postgres.');
