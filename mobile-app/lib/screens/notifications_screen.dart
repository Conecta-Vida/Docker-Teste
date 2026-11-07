import 'package:flutter/material.dart';
import 'package:hive/hive.dart';
import '../global/appCor.dart';
import '../models/usuario.dart';
import '../services/supabase_service.dart';
import 'news_details_page.dart';

class NotificationsScreen extends StatefulWidget {
  final UserModel? usuario;

  const NotificationsScreen({super.key, this.usuario});

  @override
  State<NotificationsScreen> createState() => _NotificationsScreenState();
}

class _NotificationsScreenState extends State<NotificationsScreen> {
  static const String _boxPreferencias = 'preferencias';
  static const String _keyLidas = 'notificacoes_lidas';
  static const String _keyNotificacoesAtivas = 'notificacoes_ligadas';

  final List<String> _filtros = [
    'Todas',
    'Vacinação',
    'Doações',
    'Urgentes',
    'Eventos',
  ];
  String _filtroSelecionado = 'Todas';

  Set<String> _idsLidos = {};
  bool _notificacoesAtivas = true;
  bool _inicializando = true;

  late Future<List<_NotificacaoItem>> _notificacoesFuture;

  @override
  void initState() {
    super.initState();
    _notificacoesFuture = Future.value([]);
    _inicializarTela();
  }

  Future<void> _inicializarTela() async {
    try {
      final box = await _obterBoxPreferencias();
      final lidas = List<String>.from(
        box.get(_keyLidas, defaultValue: <String>[]),
      );
      final notificacoesAtivas =
          box.get(_keyNotificacoesAtivas, defaultValue: true) == true;

      setState(() {
        _idsLidos = lidas.toSet();
        _notificacoesAtivas = notificacoesAtivas;
        _notificacoesFuture = _buscarNotificacoes();
        _inicializando = false;
      });
    } catch (_) {
      setState(() {
        _notificacoesFuture = _buscarNotificacoes();
        _inicializando = false;
      });
    }
  }

  Future<Box> _obterBoxPreferencias() async {
    if (Hive.isBoxOpen(_boxPreferencias)) {
      return Hive.box(_boxPreferencias);
    }
    return Hive.openBox(_boxPreferencias);
  }

  Future<List<_NotificacaoItem>> _buscarNotificacoes() async {
    // 1. Busca as notificações padrão da API
    final data = await SupabaseService.fetchNotificacoes();

    // 2. Busca lembretes automáticos de campanhas inscritas (que acabam amanhã)
    if (widget.usuario?.id != null) {
      final lembretes = await SupabaseService.fetchLembretesCampanhaUmDiaAntes(
        widget.usuario!.id!,
      );
      data.insertAll(0, lembretes);
    }

    // 3. Mapeia e ordena as notificações da mais recente para a mais antiga
    final itens = data.map(_NotificacaoItem.fromMap).toList();

    itens.sort((a, b) {
      final da = DateTime.tryParse(a.data);
      final db = DateTime.tryParse(b.data);
      if (da == null && db == null) return 0;
      if (da == null) return 1;
      if (db == null) return -1;
      return db.compareTo(da);
    });

    return itens;
  }

  List<_NotificacaoItem> _filtrarNotificacoes(List<_NotificacaoItem> lista) {
    if (_filtroSelecionado == 'Todas') return lista;
    return lista.where((item) => item.categoria == _filtroSelecionado).toList();
  }

  Future<void> _persistirLidas() async {
    final box = await _obterBoxPreferencias();
    await box.put(_keyLidas, _idsLidos.toList());
  }

  Future<void> _alternarLida(_NotificacaoItem item) async {
    setState(() {
      if (_idsLidos.contains(item.id)) {
        _idsLidos.remove(item.id);
      } else {
        _idsLidos.add(item.id);
      }
    });
    await _persistirLidas();
  }

  Future<void> _abrirNoticia(_NotificacaoItem item) async {
    // 1. Mostra um indicador de carregamento rápido
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (context) =>
          const Center(child: CircularProgressIndicator(color: AppCor.primary)),
    );

    try {
      // 2. Baixa todas as notícias do feed (Categoria 0 pega todas)
      final todasAsNoticias = await SupabaseService.fetchNoticiasPorCategoria(
        0,
      );

      // 3. Procura a notícia que tem o mesmo título da notificação
      final noticiaEncontrada = todasAsNoticias.firstWhere(
        (n) => n.titulo.trim() == item.tituloOriginal.trim(),
      );

      if (!mounted) return;
      Navigator.pop(context); // Remove o popup de carregamento

      // 4. Manda o usuário para a tela de detalhes
      Navigator.push(
        context,
        MaterialPageRoute(
          builder: (context) => NewsDetailsPage(
            noticia: noticiaEncontrada,
            userId: widget.usuario?.id,
            sharedBy: widget.usuario?.email,
          ),
        ),
      );
    } catch (e) {
      // Se não encontrou a notícia (pode ser um alerta apagado)
      if (!mounted) return;
      Navigator.pop(context); // Remove o popup de carregamento
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Os detalhes completos deste alerta não estão disponíveis.',
          ),
          backgroundColor: AppCor.textoCinza,
        ),
      );
    }
  }

  Future<void> _marcarTodasComoLidas(
    List<_NotificacaoItem> notificacoes,
  ) async {
    setState(() {
      _idsLidos.addAll(notificacoes.map((n) => n.id));
    });
    await _persistirLidas();
  }

  Future<void> _atualizar() async {
    setState(() {
      _notificacoesFuture = _buscarNotificacoes();
    });
    await _notificacoesFuture;
  }

  String _formatarData(String valor) {
    if (valor.isEmpty) return '--';
    final data = DateTime.tryParse(valor);
    if (data == null) {
      return valor.length >= 10 ? valor.substring(0, 10) : valor;
    }
    final dia = data.day.toString().padLeft(2, '0');
    final mes = data.month.toString().padLeft(2, '0');
    final ano = data.year.toString();
    final hora = data.hour.toString().padLeft(2, '0');
    final minuto = data.minute.toString().padLeft(2, '0');
    return '$dia/$mes/$ano $hora:$minuto';
  }

  IconData _iconePorCategoria(String categoria) {
    switch (categoria) {
      case 'Vacinação':
        return Icons.vaccines_outlined;
      case 'Doações':
        return Icons.volunteer_activism_outlined;
      case 'Urgentes':
        return Icons.warning_amber_rounded;
      case 'Eventos':
        return Icons.event_available_outlined;
      default:
        return Icons.notifications_outlined;
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_inicializando) {
      return const Scaffold(
        body: Center(child: CircularProgressIndicator(color: AppCor.primary)),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text('Notificações'),
        actions: [
          FutureBuilder<List<_NotificacaoItem>>(
            future: _notificacoesFuture,
            builder: (context, snapshot) {
              if (!snapshot.hasData || snapshot.data!.isEmpty) {
                return const SizedBox.shrink();
              }
              return IconButton(
                tooltip: 'Marcar todas como lidas',
                onPressed: () => _marcarTodasComoLidas(snapshot.data!),
                icon: const Icon(Icons.done_all),
              );
            },
          ),
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (!_notificacoesAtivas)
              Container(
                width: double.infinity,
                margin: const EdgeInsets.only(bottom: 16),
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: const Color(0xFFFFF3CD),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: const Color(0xFFFFE69C)),
                ),
                child: const Row(
                  children: [
                    Icon(Icons.info_outline, color: Color(0xFF856404)),
                    SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        'As notificações push estão desativadas nas configurações.\nVocê ainda pode consultar os alertas por esta tela.',
                        style: TextStyle(color: Color(0xFF856404)),
                      ),
                    ),
                  ],
                ),
              ),
            const Text(
              'Filtrar por categoria',
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 16),
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: _filtros.map((filtro) {
                return ChoiceChip(
                  label: Text(filtro),
                  selected: _filtroSelecionado == filtro,
                  selectedColor: AppCor.primary,
                  backgroundColor: Colors.grey.shade200,
                  labelStyle: TextStyle(
                    color: _filtroSelecionado == filtro
                        ? Colors.white
                        : AppCor.textTitle,
                  ),
                  onSelected: (_) =>
                      setState(() => _filtroSelecionado = filtro),
                );
              }).toList(),
            ),
            const SizedBox(height: 24),
            Expanded(
              child: FutureBuilder<List<_NotificacaoItem>>(
                future: _notificacoesFuture,
                builder: (context, snapshot) {
                  if (snapshot.connectionState == ConnectionState.waiting) {
                    return const Center(
                      child: CircularProgressIndicator(color: AppCor.primary),
                    );
                  }

                  if (snapshot.hasError) {
                    return Center(
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          const Icon(
                            Icons.wifi_off,
                            size: 48,
                            color: AppCor.textSubtitle,
                          ),
                          const SizedBox(height: 12),
                          const Text(
                            'Não foi possível carregar as notificações.',
                            style: TextStyle(fontWeight: FontWeight.w600),
                            textAlign: TextAlign.center,
                          ),
                          const SizedBox(height: 8),
                          ElevatedButton(
                            onPressed: _atualizar,
                            child: const Text('Tentar novamente'),
                          ),
                        ],
                      ),
                    );
                  }

                  final notificacoes = _filtrarNotificacoes(
                    snapshot.data ?? [],
                  );

                  if (notificacoes.isEmpty) {
                    return RefreshIndicator(
                      onRefresh: _atualizar,
                      child: ListView(
                        children: const [
                          SizedBox(height: 120),
                          Icon(
                            Icons.notifications_off_outlined,
                            size: 52,
                            color: AppCor.textSubtitle,
                          ),
                          SizedBox(height: 12),
                          Center(
                            child: Text(
                              'Nenhuma notificação nesta categoria.',
                              style: TextStyle(fontWeight: FontWeight.w600),
                            ),
                          ),
                        ],
                      ),
                    );
                  }

                  return RefreshIndicator(
                    onRefresh: _atualizar,
                    child: ListView.separated(
                      itemCount: notificacoes.length,
                      separatorBuilder: (_, __) => const SizedBox(height: 12),
                      itemBuilder: (context, index) {
                        final item = notificacoes[index];
                        final lida = _idsLidos.contains(item.id);

                        return InkWell(
                          borderRadius: BorderRadius.circular(16),
                          onTap: () {
                            _alternarLida(item);
                            _abrirNoticia(item); // Redireciona para a notícia
                          },
                          child: Container(
                            padding: const EdgeInsets.all(14),
                            decoration: BoxDecoration(
                              color: Colors.white,
                              borderRadius: BorderRadius.circular(16),
                              border: Border.all(
                                color: lida
                                    ? Colors.grey.shade200
                                    : AppCor.primary.withOpacity(0.35),
                              ),
                              boxShadow: [
                                BoxShadow(
                                  color: Colors.black.withOpacity(0.04),
                                  blurRadius: 10,
                                  offset: const Offset(0, 3),
                                ),
                              ],
                            ),
                            child: Row(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Container(
                                  margin: const EdgeInsets.only(top: 2),
                                  padding: const EdgeInsets.all(8),
                                  decoration: BoxDecoration(
                                    color: lida
                                        ? Colors.grey.shade100
                                        : AppCor.primary.withOpacity(0.1),
                                    shape: BoxShape.circle,
                                  ),
                                  child: Icon(
                                    _iconePorCategoria(item.categoria),
                                    color: lida
                                        ? AppCor.textSubtitle
                                        : AppCor.primary,
                                  ),
                                ),
                                const SizedBox(width: 12),
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment:
                                        CrossAxisAlignment.start,
                                    children: [
                                      Row(
                                        children: [
                                          Expanded(
                                            child: Text(
                                              item.titulo,
                                              style: TextStyle(
                                                fontWeight: lida
                                                    ? FontWeight.w500
                                                    : FontWeight.bold,
                                                color: AppCor.textTitle,
                                              ),
                                            ),
                                          ),
                                          if (!lida)
                                            Container(
                                              width: 9,
                                              height: 9,
                                              decoration: const BoxDecoration(
                                                color: AppCor.primary,
                                                shape: BoxShape.circle,
                                              ),
                                            ),
                                        ],
                                      ),
                                      const SizedBox(height: 4),
                                      Text(
                                        item.descricao,
                                        style: const TextStyle(
                                          color: AppCor.textSubtitle,
                                        ),
                                      ),
                                      const SizedBox(height: 8),
                                      Wrap(
                                        spacing: 8,
                                        runSpacing: 6,
                                        children: [
                                          _TagInfo(text: item.categoria),
                                          _TagInfo(
                                            text: _formatarData(item.data),
                                          ),
                                          if (item.localizacao.isNotEmpty)
                                            _TagInfo(text: item.localizacao),
                                        ],
                                      ),
                                    ],
                                  ),
                                ),
                              ],
                            ),
                          ),
                        );
                      },
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _TagInfo extends StatelessWidget {
  final String text;

  const _TagInfo({required this.text});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.grey.shade100,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        text,
        style: const TextStyle(
          fontSize: 11,
          color: AppCor.textSubtitle,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }
}

class _NotificacaoItem {
  final String id;
  final String titulo;
  final String descricao;
  final String categoria;
  final String data;
  final String localizacao;
  final String
  tituloOriginal; // Guardará o título real para podermos buscar a notícia

  const _NotificacaoItem({
    required this.id,
    required this.titulo,
    required this.descricao,
    required this.categoria,
    required this.data,
    required this.localizacao,
    required this.tituloOriginal,
  });

  factory _NotificacaoItem.fromMap(Map<String, dynamic> map) {
    String title = (map['titulo'] ?? 'Notificação').toString();
    String desc = (map['descricao'] ?? 'Sem detalhes').toString();
    String original = title;

    // Se for o alerta de "termina amanhã", extraímos o título original de dentro da descrição
    if (title == 'Campanha termina amanhã') {
      final regex = RegExp(r'A campanha "(.*?)" encerra');
      final match = regex.firstMatch(desc);
      if (match != null) {
        original = match.group(1) ?? original;
      }
    }

    return _NotificacaoItem(
      id: (map['id'] ?? '').toString(),
      titulo: title,
      descricao: desc,
      categoria: (map['categoria'] ?? 'Todas').toString(),
      data: (map['data'] ?? '').toString(),
      localizacao: (map['localizacao'] ?? '').toString(),
      tituloOriginal: original,
    );
  }
}
