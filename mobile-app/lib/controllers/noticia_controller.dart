// lib/controllers/news_controller.dart
import '../models/noticia.dart';

class NewsController {
  // Função que a tela vai chamar para pegar a lista certa
  List<NewsModel> getNoticiasPorCategoria(int categoryIndex) {
    switch (categoryIndex) {
      case 0:
        return _noticiasHome;
      case 1:
        return _noticiasVacinacao;
      case 2:
        return _noticiasSangue;
      case 3:
        return _noticiasUrgentes;
      case 4:
        return _noticiasEventos;
      default:
        return _noticiasHome;
    }
  }

  // Função para pegar o título da seção
  String getTituloSessao(int categoryIndex) {
    switch (categoryIndex) {
      case 0:
        return 'Todas as Notícias';
      case 1:
        return 'Campanhas de Vacinação';
      case 2:
        return 'Doações de Sangue';
      case 3:
        return 'Alertas Urgentes';
      case 4:
        return 'Todas as Campanhas';
      default:
        return 'Últimas Notícias';
    }
  }

  List<NewsModel> getAllNoticias() {
    return [
      ..._noticiasHome,
      ..._noticiasVacinacao,
      ..._noticiasSangue,
      ..._noticiasUrgentes,
      ..._noticiasEventos,
    ];
  }

  // --- AS LISTAS DE DADOS ---
  final List<NewsModel> _noticiasHome = [
    NewsModel(
      tag: 'SUS - Região Bragantina',
      data: '03/04/2025',
      titulo: 'Vacinação contra Influenza aberta para todas as idades',
      subtitulo: 'Válido até Junho de 2025. Vacina Sempre Brasil!',
      descricao:
          'A campanha de vacinação está disponível para toda a população...',
      imagem: 'assets/images/vacina_banner.png',
      categoria: 'Vacinação',
      local: 'Posto de Saúde central',
      publicoAlvo: 'Todas as idades',
      orgao: 'Secretaria Municipal de Saúde',
      orgaoTelefone: '(11) 4002-8922',
      orgaoSite: 'https://www.saude.gov.br',
      textoBotaoAcao: 'Ver Detalhes',
    ),
    NewsModel(
      tag: 'Informativo',
      data: '21/03/2025',
      titulo: 'Ouvidoria aberta à população reforça transparência',
      subtitulo: 'A escuta ativa da população é fundamental.',
      descricao: 'A Ouvidoria do SUS reforça seus canais de atendimento...',
      imagem:
          'https://images.unsplash.com/photo-1551076805-e1869033e561?q=80&w=800&auto=format&fit=crop',
      categoria: 'Notícias',
      local: 'Sede da Ouvidoria',
      publicoAlvo: 'População em geral',
      orgao: 'Ouvidoria SUS',
      orgaoTelefone: '(11) 3030-3030',
      orgaoSite: 'https://www.saude.gov.br/ouvidoria',
      textoBotaoAcao: 'Abrir Mais',
    ),
    NewsModel(
      tag: 'Alerta',
      data: '12/05/2026',
      titulo: 'Combate à Dengue: limpe seu quintal',
      subtitulo: 'A campanha percorre bairros da cidade.',
      descricao:
          'A ação educativa orienta moradores a eliminar águas paradas e distruibui kits de prevenção.',
        imagem: 'assets/images/vacina_banner.png',
      categoria: 'Urgente',
      local: 'Vários bairros',
      publicoAlvo: 'População em geral',
      orgao: 'Secretaria Municipal de Saúde',
      orgaoTelefone: '(11) 4002-8922',
      orgaoSite: 'https://www.saude.gov.br',
      textoBotaoAcao: 'Veja como ajudar',
    ),
  ];

  final List<NewsModel> _noticiasVacinacao = [
    NewsModel(
      tag: 'Campanha',
      data: '03/04/2025',
      titulo: 'Vacinação contra Influenza',
      subtitulo: 'Válido até Junho de 2025.',
      descricao: 'A vacina da Influenza já está liberada nos postos.',
      imagem:
          'assets/images/vacina_banner.png', // Lembre-se de ter essa imagem ou trocar por uma URL
      categoria: 'Vacinação',
      local: 'Posto de Saúde central',
      publicoAlvo: 'População em geral',
      orgao: 'Secretaria Municipal de Saúde',
      orgaoTelefone: '(11) 4002-8922',
      orgaoSite: 'https://www.saude.gov.br',
      textoBotaoAcao: 'Saber Mais',
    ),
  ];

  final List<NewsModel> _noticiasSangue = [
    NewsModel(
      tag: 'Ato de Amor',
      data: '19/03/2025',
      titulo: 'Doe sangue, salve vidas!',
      subtitulo: 'Um simples gesto.',
      descricao: 'Participe da nossa campanha de doação no hemocentro central.',
      imagem:
          'https://images.unsplash.com/photo-1615461066841-6116e61058f4?q=80&w=800&auto=format&fit=crop',
      categoria: 'Doações',
      local: 'Hemocentro central',
      publicoAlvo: 'Doadores voluntários',
      orgao: 'Hemocentro Municipal',
      orgaoTelefone: '(11) 5555-1212',
      orgaoSite: 'https://www.saude.gov.br/hemocentro',
      textoBotaoAcao: 'Quero ajudar',
    ),
  ];

  final List<NewsModel> _noticiasUrgentes = [
    NewsModel(
      tag: 'ALERTA',
      data: '19/03/2025',
      titulo: 'URGENTE! Ana Clara precisa de doações',
      subtitulo: 'Ajude a salvar uma vida.',
      descricao: 'Precisamos de sangue O- com urgência.',
      imagem:
          'https://images.unsplash.com/photo-1584036561566-baf8f5f1b144?q=80&w=800&auto=format&fit=crop',
      categoria: 'Urgente',
      local: 'Hospital Municipal',
      publicoAlvo: 'Doadores O-',
      orgao: 'Clínica de Emergência',
      orgaoTelefone: '(11) 9999-0000',
      orgaoSite: 'https://www.saude.gov.br/emergencia',
      textoBotaoAcao: 'Apoiar agora',
    ),
  ];

  final List<NewsModel> _noticiasEventos = [
    NewsModel(
      tag: 'Solidariedade',
      data: '19/03/2025',
      titulo: 'Evento de doação no domingo',
      subtitulo: 'Compareça e ajude!',
      descricao: 'Participe do nosso evento solidário na praça principal.',
      imagem:
          'https://images.unsplash.com/photo-1615461066841-6116e61058f4?q=80&w=800&auto=format&fit=crop',
      categoria: 'Eventos',
      local: 'Praça Central',
      publicoAlvo: 'Interessados em ajuda comunitária',
      orgao: 'Associação Vida',
      orgaoTelefone: '(11) 8888-1515',
      orgaoSite: 'https://www.saude.gov.br/eventos',
      textoBotaoAcao: 'Participar',
    ),
  ];
}
