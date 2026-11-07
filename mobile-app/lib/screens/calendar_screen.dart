import 'package:flutter/material.dart';
import '../global/appCor.dart';
import '../models/noticia.dart';
import '../models/usuario.dart';
import '../services/supabase_service.dart';
import 'news_details_page.dart';

class CalendarScreen extends StatefulWidget {
  final UserModel usuario;

  const CalendarScreen({super.key, required this.usuario});

  @override
  State<CalendarScreen> createState() => _CalendarScreenState();
}

class _CalendarScreenState extends State<CalendarScreen> {
  bool _isLoading = true;
  List<Map<String, dynamic>> _todosOsEventos = [];

  @override
  void initState() {
    super.initState();
    _carregarDadosUnificados();
  }

  Future<void> _carregarDadosUnificados() async {
    setState(() => _isLoading = true);
    List<Map<String, dynamic>> eventosCarregados = [];

    try {
      // CARREGAR INSCRIÇÕES DA API (ONLINE)
      if (widget.usuario.id != null) {
        final campanhasInscritas =
            await SupabaseService.fetchCampanhasAtivasDoUsuario(
              widget.usuario.id!,
            );
        for (var campanha in campanhasInscritas) {
          eventosCarregados.add({
            'objeto': campanha,
            'titulo': campanha.titulo,
            'data': _formatarDataFim(campanha.dataFim),
            'local': campanha.local,
            'tipo': 'campanha',
          });
        }
      }
    } catch (e) {
      debugPrint('Erro ao carregar calendário: $e');
    }

    if (mounted) {
      setState(() {
        _todosOsEventos = eventosCarregados;
        _isLoading = false;
      });
    }
  }

  String _formatarDataFim(String valor) {
    if (valor.isEmpty) return 'Sem data final definida';
    final d = DateTime.tryParse(valor);
    if (d == null) return valor;
    final dia = d.day.toString().padLeft(2, '0');
    final mes = d.month.toString().padLeft(2, '0');
    final ano = d.year.toString();
    return 'Termina em $dia/$mes/$ano';
  }

  Future<void> _desinscrever(NewsModel campanha) async {
    if (widget.usuario.id == null || campanha.id == null) return;

    setState(() => _isLoading = true);
    try {
      await SupabaseService.desinscreverUsuarioDaCampanha(
        comunicacaoId: campanha.id!,
        usuarioId: widget.usuario.id!,
      );

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            'Lembrete e inscrição removidos: "${campanha.titulo}".',
          ),
          backgroundColor: AppCor.primary,
        ),
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Erro ao desinscrever: $e'),
          backgroundColor: AppCor.error,
        ),
      );
    }

    await _carregarDadosUnificados(); // Recarrega a lista
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppCor.background,
      appBar: AppBar(
        title: const Text(
          'Meu Cronograma',
          style: TextStyle(
            color: AppCor.textoPrimario,
            fontWeight: FontWeight.bold,
          ),
        ),
        backgroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
        iconTheme: const IconThemeData(color: AppCor.primary),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh, color: AppCor.primary),
            onPressed: _carregarDadosUnificados,
          ),
        ],
      ),
      body: _isLoading
          ? const Center(
              child: CircularProgressIndicator(color: AppCor.primary),
            )
          : RefreshIndicator(
              onRefresh: _carregarDadosUnificados,
              color: AppCor.primary,
              child: SingleChildScrollView(
                physics: const AlwaysScrollableScrollPhysics(),
                child: Padding(
                  padding: const EdgeInsets.all(24.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Text(
                        'Compromissos Agendados',
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                          color: AppCor.textoPrimario,
                        ),
                      ),
                      const SizedBox(height: 16),
                      _todosOsEventos.isEmpty
                          ? const Padding(
                              padding: EdgeInsets.symmetric(vertical: 40.0),
                              child: Center(
                                child: Text(
                                  'Nenhum evento ou lembrete agendado.',
                                  style: TextStyle(color: AppCor.textoCinza),
                                ),
                              ),
                            )
                          : ListView.separated(
                              shrinkWrap: true,
                              physics: const NeverScrollableScrollPhysics(),
                              itemCount: _todosOsEventos.length,
                              separatorBuilder: (_, __) =>
                                  const SizedBox(height: 16),
                              itemBuilder: (context, index) {
                                final evento = _todosOsEventos[index];

                                return Container(
                                  decoration: BoxDecoration(
                                    color: Colors.white,
                                    borderRadius: BorderRadius.circular(18),
                                    boxShadow: [
                                      BoxShadow(
                                        color: Colors.black.withOpacity(0.05),
                                        blurRadius: 12,
                                        offset: const Offset(0, 4),
                                      ),
                                    ],
                                  ),
                                  child: ListTile(
                                    contentPadding: const EdgeInsets.symmetric(
                                      horizontal: 20,
                                      vertical: 16,
                                    ),
                                    leading: const Icon(
                                      Icons.alarm_on,
                                      color: AppCor.catVacinacao,
                                    ),
                                    title: Text(
                                      evento['titulo'] ?? 'Sem Título',
                                      style: const TextStyle(
                                        fontWeight: FontWeight.bold,
                                        color: AppCor.textoPrimario,
                                      ),
                                    ),
                                    subtitle: Padding(
                                      padding: const EdgeInsets.only(top: 6.0),
                                      child: Column(
                                        crossAxisAlignment:
                                            CrossAxisAlignment.start,
                                        children: [
                                          Text(
                                            evento['data'] ?? '',
                                            style: const TextStyle(
                                              color: AppCor.textSubtitle,
                                            ),
                                          ),
                                          const SizedBox(height: 2),
                                          Text(
                                            evento['local'] ?? '',
                                            style: const TextStyle(
                                              color: AppCor.textoCinza,
                                            ),
                                          ),
                                        ],
                                      ),
                                    ),
                                    trailing: IconButton(
                                      tooltip: 'Desinscrever',
                                      icon: const Icon(
                                        Icons.remove_circle_outline,
                                        color: AppCor.error,
                                      ),
                                      onPressed: () =>
                                          _desinscrever(evento['objeto']),
                                    ),
                                    onTap: () {
                                      if (evento['objeto'] != null) {
                                        Navigator.push(
                                          context,
                                          MaterialPageRoute(
                                            builder: (context) =>
                                                NewsDetailsPage(
                                                  noticia: evento['objeto'],
                                                  userId: widget.usuario.id,
                                                  sharedBy:
                                                      widget.usuario.email,
                                                ),
                                          ),
                                        );
                                      }
                                    },
                                  ),
                                );
                              },
                            ),
                    ],
                  ),
                ),
              ),
            ),
    );
  }
}
