import 'package:flutter/material.dart';
import 'package:hive/hive.dart';
import 'package:provider/provider.dart';
import '../global/appCor.dart';
import '../controllers/noticia_controller.dart';
import '../models/noticia.dart';
import '../providers/usuario_provider.dart';
import '../services/supabase_service.dart';
import '../widgets/noticia_card.dart';
import '../widgets/bottom_navbar.dart';
import '../widgets/noticias_header.dart';
import 'notifications_screen.dart';
import 'settings_screen.dart';
import 'calendar_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final NewsController _newsController = NewsController();
  int _categoryIndex = 0;
  late Future<List<NewsModel>> _noticiasFuture;
  String _searchQuery = '';
  Set<String> _cidadesFiltro = {};
  List<NewsModel> _noticiasCache = [];
  bool _popupAlertaExibido = false;
  bool _usarGps = true;

  @override
  void initState() {
    super.initState();
    _carregarFiltrosSalvos();
    _refreshNoticias();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _mostrarPopupAlertaCritico();
    });
  }

  Future<void> _carregarFiltrosSalvos() async {
    try {
      final box = Hive.isBoxOpen('preferencias')
          ? Hive.box('preferencias')
          : await Hive.openBox('preferencias');

      final regioesSalvas = List<String>.from(
        box.get('regioes_interesse', defaultValue: <String>[]),
      );
      final gpsLigado = box.get('usar_gps', defaultValue: true);

      if (mounted) {
        setState(() {
          _usarGps = gpsLigado;
          _cidadesFiltro = regioesSalvas.toSet();

          // Acessa o usuário através do Provider em vez do widget.usuario
          final usuario = context.read<UsuarioProvider>().usuario!;
          final cidadeUsuario = usuario.localizacao;
          final cidadeValida =
              cidadeUsuario.isNotEmpty &&
              cidadeUsuario != 'Não informado' &&
              cidadeUsuario != 'Não disponível';

          // Se a chave GPS estiver ativa nos ajustes, injeta a cidade do usuário nos filtros
          if (_usarGps && cidadeValida) {
            _cidadesFiltro.add(cidadeUsuario);
          } else if (!_usarGps && cidadeValida) {
            _cidadesFiltro.remove(cidadeUsuario);
          }
        });
      }
    } catch (e) {
      debugPrint('Erro ao carregar filtros do Hive: $e');
    }
  }

  Future<void> _salvarFiltros() async {
    try {
      final box = Hive.isBoxOpen('preferencias')
          ? Hive.box('preferencias')
          : await Hive.openBox('preferencias');

      // Acessa o usuário através do Provider em vez do widget.usuario
      final usuario = context.read<UsuarioProvider>().usuario!;
      final cidadeUsuario = usuario.localizacao;
      final cidadeValida =
          cidadeUsuario.isNotEmpty &&
          cidadeUsuario != 'Não informado' &&
          cidadeUsuario != 'Não disponível';

      if (cidadeValida) {
        bool isGpsAtivo = _cidadesFiltro.contains(cidadeUsuario);
        await box.put('usar_gps', isGpsAtivo);
        _usarGps = isGpsAtivo;
      }

      List<String> regioes = _cidadesFiltro.toList();
      if (cidadeValida) {
        regioes.remove(cidadeUsuario);
      }

      await box.put('regioes_interesse', regioes);
    } catch (e) {
      debugPrint('Erro ao salvar filtros no Hive: $e');
    }
  }

  Future<void> _mostrarPopupAlertaCritico() async {
    if (!mounted || _popupAlertaExibido) return;

    try {
      final notificacoes = await SupabaseService.fetchNotificacoes();
      final alertaCritico = notificacoes.firstWhere(
        (item) => (item['tipo'] ?? '').toString().toUpperCase() == 'ALERTA',
        orElse: () => <String, dynamic>{},
      );

      if (alertaCritico.isEmpty || !mounted || _popupAlertaExibido) return;

      _popupAlertaExibido = true;

      showDialog<void>(
        context: context,
        barrierDismissible: false,
        builder: (ctx) {
          return AlertDialog(
            title: Text(
              (alertaCritico['titulo'] ?? 'ALERTA CRÍTICO').toString(),
            ),
            content: Text(
              (alertaCritico['descricao'] ??
                      'Mensagem crítica de saúde pública.')
                  .toString(),
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.of(ctx).pop(),
                child: const Text('Fechar'),
              ),
            ],
          );
        },
      );
    } catch (_) {
      // Se a API falhar, apenas não mostra o popup.
    }
  }

  void _refreshNoticias() {
    _noticiasFuture = SupabaseService.fetchNoticiasPorCategoria(_categoryIndex);
  }

  List<NewsModel> _filtrarNoticias(List<NewsModel> noticias) {
    var lista = noticias;
    if (_searchQuery.isNotEmpty) {
      final q = _searchQuery.toLowerCase();
      lista = lista
          .where(
            (n) =>
                n.titulo.toLowerCase().contains(q) ||
                n.descricao.toLowerCase().contains(q),
          )
          .toList();
    }
    if (_cidadesFiltro.isNotEmpty) {
      lista = lista
          .where(
            (n) => _cidadesFiltro.any(
              (c) => n.local.toLowerCase().contains(c.toLowerCase()),
            ),
          )
          .toList();
    }
    return lista;
  }

  List<String> _cidadesDisponiveis() {
    final cidades =
        _noticiasCache
            .map((n) => n.local)
            .where((l) => l.isNotEmpty && l != 'Região Geral')
            .toSet()
            .toList()
          ..sort();
    return cidades;
  }

  void _abrirFiltroCidades() {
    final disponiveis = _cidadesDisponiveis();
    if (disponiveis.isEmpty) return;

    Set<String> selecao = Set.from(_cidadesFiltro);

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      useSafeArea: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (ctx) {
        return StatefulBuilder(
          builder: (ctx, setModalState) {
            return ConstrainedBox(
              constraints: BoxConstraints(
                maxHeight: MediaQuery.of(ctx).size.height * 0.6,
              ),
              child: Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: 24,
                  vertical: 16,
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Text(
                      'Filtrar por cidades',
                      style: TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 12),
                    Expanded(
                      child: ListView(
                        children: disponiveis.map((cidade) {
                          return CheckboxListTile(
                            title: Text(cidade),
                            value: selecao.contains(cidade),
                            activeColor: AppCor.primary,
                            contentPadding: EdgeInsets.zero,
                            onChanged: (checked) {
                              setModalState(() {
                                checked == true
                                    ? selecao.add(cidade)
                                    : selecao.remove(cidade);
                              });
                            },
                          );
                        }).toList(),
                      ),
                    ),
                    const SizedBox(height: 12),
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton(
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppCor.primary,
                          foregroundColor: Colors.white,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(12),
                          ),
                        ),
                        onPressed: () {
                          setState(() => _cidadesFiltro = selecao);
                          _salvarFiltros();
                          Navigator.pop(ctx);
                        },
                        child: const Text('Aplicar'),
                      ),
                    ),
                    const SizedBox(height: 8),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildCidadeChips() {
    final cidadeUsuario = context.read<UsuarioProvider>().usuario!.localizacao;
    final temFiltroAtivo = _cidadesFiltro.isNotEmpty;

    final cidadeUsuarioValida =
        cidadeUsuario.isNotEmpty &&
        cidadeUsuario != 'Não informado' &&
        cidadeUsuario != 'Não disponível';

    final cidadesExtras = _cidadesFiltro
        .where((c) => c != cidadeUsuario)
        .toList();

    return SizedBox(
      height: 36,
      child: ListView(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 24),
        children: [
          _buildChip(
            'Todas',
            selecionado: !temFiltroAtivo,
            onTap: () {
              setState(() => _cidadesFiltro.clear());
              _salvarFiltros();
            },
          ),

          if (cidadeUsuarioValida)
            _buildChip(
              cidadeUsuario,
              selecionado: _cidadesFiltro.contains(cidadeUsuario),
              onTap: () {
                setState(() {
                  _cidadesFiltro.contains(cidadeUsuario)
                      ? _cidadesFiltro.remove(cidadeUsuario)
                      : _cidadesFiltro.add(cidadeUsuario);
                });
                _salvarFiltros();
              },
            ),

          ...cidadesExtras.map((cidade) => _buildChipRemovivel(cidade)),
          _buildChipFiltro(),
        ],
      ),
    );
  }

  Widget _buildChip(
    String label, {
    required bool selecionado,
    required VoidCallback onTap,
  }) {
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: GestureDetector(
        onTap: onTap,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
          decoration: BoxDecoration(
            color: selecionado ? AppCor.primary : Colors.white,
            borderRadius: BorderRadius.circular(20),
            border: Border.all(
              color: selecionado ? AppCor.primary : Colors.grey.shade300,
            ),
          ),
          child: Text(
            label,
            style: TextStyle(
              color: selecionado ? Colors.white : AppCor.textoCinza,
              fontWeight: selecionado ? FontWeight.bold : FontWeight.normal,
              fontSize: 13,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildChipRemovivel(String cidade) {
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        decoration: BoxDecoration(
          color: AppCor.primary,
          borderRadius: BorderRadius.circular(20),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              cidade,
              style: const TextStyle(
                fontSize: 13,
                color: Colors.white,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(width: 4),
            GestureDetector(
              onTap: () {
                setState(() => _cidadesFiltro.remove(cidade));
                _salvarFiltros();
              },
              child: const Icon(Icons.close, size: 14, color: Colors.white),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildChipFiltro() {
    final temExtra = _cidadesFiltro.any((c) {
      final cidadeUsuario = context
          .read<UsuarioProvider>()
          .usuario!
          .localizacao;
      return c != cidadeUsuario;
    });
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: GestureDetector(
        onTap: _abrirFiltroCidades,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
          decoration: BoxDecoration(
            color: temExtra ? AppCor.primary : Colors.white,
            borderRadius: BorderRadius.circular(20),
            border: Border.all(
              color: temExtra ? AppCor.primary : Colors.grey.shade300,
            ),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                Icons.tune,
                size: 14,
                color: temExtra ? Colors.white : AppCor.textoCinza,
              ),
              const SizedBox(width: 4),
              Text(
                'Filtrar',
                style: TextStyle(
                  fontSize: 13,
                  color: temExtra ? Colors.white : AppCor.textoCinza,
                  fontWeight: temExtra ? FontWeight.bold : FontWeight.normal,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Future<void> _reloadNoticias() async {
    setState(() {
      _refreshNoticias();
    });
    await _noticiasFuture;
  }

  @override
  Widget build(BuildContext context) {
    // Aqui pegamos o usuário atual de forma correta e síncrona
    final usuario = context.watch<UsuarioProvider>().usuario!;
    final String tituloSessao = _newsController.getTituloSessao(_categoryIndex);

    return Scaffold(
      backgroundColor: AppCor.background,
      body: SafeArea(
        child: Column(
          children: [
            NoticiasHeader(
              nome: usuario.nome,
              onSearch: (query) => setState(() => _searchQuery = query),
              onNotifications: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    // Passamos 'usuario' lido do provider para as telas que ainda o pedem no construtor
                    builder: (context) => NotificationsScreen(usuario: usuario),
                  ),
                );
              },
              onCalendar: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    // Passamos 'usuario' lido do provider
                    builder: (context) => CalendarScreen(usuario: usuario),
                  ),
                );
              },
              onSettings: () async {
                await Navigator.push(
                  context,
                  MaterialPageRoute(
                    // Corrigido para const SettingsScreen() sem passar 'usuario'
                    builder: (context) => const SettingsScreen(),
                  ),
                );
                // Quando voltar das configurações, aplica o que foi alterado lá
                _carregarFiltrosSalvos();
              },
            ),
            const SizedBox(height: 36),
            _buildCidadeChips(),
            const SizedBox(height: 16),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    tituloSessao,
                    style: const TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                      color: AppCor.textTitle,
                    ),
                  ),
                  const Text(
                    'Categoria',
                    style: TextStyle(color: AppCor.textoCinza),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            Expanded(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 24),
                child: RefreshIndicator(
                  onRefresh: _reloadNoticias,
                  color: AppCor.primary,
                  child: FutureBuilder<List<NewsModel>>(
                    future: _noticiasFuture,
                    builder: (context, snapshot) {
                      if (snapshot.connectionState == ConnectionState.waiting) {
                        return const Center(
                          child: CircularProgressIndicator(
                            color: AppCor.primary,
                          ),
                        );
                      }

                      final todas =
                          snapshot.data ??
                          _newsController.getNoticiasPorCategoria(
                            _categoryIndex,
                          );
                      if (_noticiasCache != todas) {
                        WidgetsBinding.instance.addPostFrameCallback((_) {
                          if (mounted) setState(() => _noticiasCache = todas);
                        });
                      }
                      final noticias = _filtrarNoticias(todas);

                      if (snapshot.hasError && todas.isEmpty) {
                        return const Center(
                          child: Text(
                            'Não foi possível carregar as notícias. Puxe para atualizar.',
                            style: TextStyle(color: AppCor.textoCinza),
                            textAlign: TextAlign.center,
                          ),
                        );
                      }

                      if (noticias.isEmpty) {
                        return const Center(
                          child: Text(
                            'Nenhuma notícia encontrada.',
                            style: TextStyle(color: AppCor.textoCinza),
                          ),
                        );
                      }

                      return ListView.builder(
                        itemCount: noticias.length,
                        itemBuilder: (context, index) {
                          return Padding(
                            padding: const EdgeInsets.only(bottom: 20.0),
                            child: NoticiaCard(
                              noticia: noticias[index],
                              // Pegando ID e Email diretamente do Provider
                              userEmail: usuario.email,
                              userId: usuario.id,
                            ),
                          );
                        },
                      );
                    },
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
      bottomNavigationBar: CustomBottomNavBar(
        currentIndex: _categoryIndex,
        onTap: (index) {
          setState(() {
            _categoryIndex = index;
            _refreshNoticias();
          });
        },
      ),
    );
  }
}
