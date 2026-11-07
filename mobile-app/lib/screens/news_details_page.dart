import 'package:flutter/material.dart';
import 'package:share_plus/share_plus.dart';
import 'package:url_launcher/url_launcher.dart';
import '../models/noticia.dart';
import '../global/appCor.dart';
import '../services/supabase_service.dart';
import '../widgets/noticia_info_botao.dart';
import '../widgets/noticia_secao_mais.dart';

class NewsDetailsPage extends StatefulWidget {
  final NewsModel noticia;
  final String? sharedBy;
  final int? userId;

  const NewsDetailsPage({
    super.key,
    required this.noticia,
    this.sharedBy,
    this.userId,
  });

  @override
  State<NewsDetailsPage> createState() => _NewsDetailsPageState();
}

class _NewsDetailsPageState extends State<NewsDetailsPage> {
  bool _processandoCampanha = false;
  bool _inscrito = false;

  bool get _ehCampanha => widget.noticia.tag.toUpperCase() == 'CAMPANHA';

  @override
  void initState() {
    super.initState();
    _carregarStatusInscricao();
  }

  Future<void> _carregarStatusInscricao() async {
    if (!_ehCampanha || widget.userId == null || widget.noticia.id == null) {
      return;
    }

    try {
      final inscrito = await SupabaseService.isUsuarioInscritoEmCampanha(
        comunicacaoId: widget.noticia.id!,
        usuarioId: widget.userId!,
      );
      if (!mounted) return;
      setState(() => _inscrito = inscrito);
    } catch (_) {
      // Mantém a tela funcional mesmo se a API de inscrição falhar.
    }
  }

  Future<void> _alternarInscricaoCampanha() async {
    if (widget.noticia.id == null) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Esta campanha ainda não possui ID válido para inscrição.',
          ),
        ),
      );
      return;
    }

    int? usuarioId = widget.userId;

    // Fallback: se o id não veio da sessão, tenta resolver pelo e-mail autenticado.
    if (usuarioId == null && (widget.sharedBy ?? '').trim().isNotEmpty) {
      final user = await SupabaseService.fetchUsuarioByEmail(
        widget.sharedBy!.trim(),
      );
      usuarioId = user?.id;
    }

    if (usuarioId == null) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text(
            'Não foi possível identificar o usuário para inscrição.',
          ),
        ),
      );
      return;
    }

    setState(() => _processandoCampanha = true);

    try {
      if (_inscrito) {
        await SupabaseService.desinscreverUsuarioDaCampanha(
          comunicacaoId: widget.noticia.id!,
          usuarioId: usuarioId,
        );
        if (!mounted) return;
        setState(() => _inscrito = false);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              'Lembrete removido. Inscrição cancelada para: "${widget.noticia.titulo}".',
            ),
            backgroundColor: AppCor.primary,
          ),
        );
      } else {
        await SupabaseService.inscreverUsuarioEmCampanha(
          comunicacaoId: widget.noticia.id!,
          usuarioId: usuarioId,
        );
        if (!mounted) return;
        setState(() => _inscrito = true);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              'Lembrete Ativo! Você está inscrito em: ${widget.noticia.titulo}',
            ),
            backgroundColor: AppCor.primary,
          ),
        );
      }
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Erro ao processar sua inscrição: $e'),
          backgroundColor: AppCor.error,
        ),
      );
    } finally {
      if (mounted) setState(() => _processandoCampanha = false);
    }
  }

  Color _corDaCategoria(String categoria) {
    if (categoria.toLowerCase().contains('alerta')) {
      return AppCor.error;
    }
    if (categoria.toLowerCase().contains('vacina')) {
      return AppCor.catVacinacao;
    }
    if (categoria.toLowerCase().contains('sangue') ||
        categoria.toLowerCase().contains('doação')) {
      return AppCor.catNoticias;
    }
    return AppCor.primary;
  }

  @override
  Widget build(BuildContext context) {
    final String imagePath = widget.noticia.imagem.trim();
    final bool hasImage = imagePath.isNotEmpty;
    final bool isNetworkImage = hasImage && imagePath.startsWith('http');

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        leading: IconButton(
          icon: Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: Colors.white.withValues(alpha: 0.8),
              shape: BoxShape.circle,
            ),
            child: const Icon(
              Icons.arrow_back,
              color: AppCor.textoPrimario,
              size: 20,
            ),
          ),
          onPressed: () => Navigator.pop(context),
        ),
        actions: [
          IconButton(
            icon: Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.8),
                shape: BoxShape.circle,
              ),
              child: const Icon(
                Icons.share_outlined,
                color: AppCor.primary,
                size: 20,
              ),
            ),
            onPressed: () async {
              final shareText =
                  '${widget.noticia.titulo}\n\n${widget.noticia.subtitulo}\n\n${widget.noticia.descricao}\n\nÓrgão: ${widget.noticia.orgao}\nContato: ${widget.noticia.orgaoTelefone}';
              await Share.share(shareText, subject: widget.noticia.titulo);
              await SupabaseService.trackNewsShare(
                widget.noticia,
                sharedBy: widget.sharedBy ?? 'app_user',
              );
            },
          ),
          const SizedBox(width: 8),
        ],
      ),
      extendBodyBehindAppBar: true,

      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: double.infinity,
              height: 320,
              child: Hero(
                tag: widget.noticia.titulo + widget.noticia.data,
                child: !hasImage
                    ? Container(
                        color: const Color(0xFFEAF3FB),
                        alignment: Alignment.center,
                        child: const Icon(
                          Icons.image_not_supported_outlined,
                          color: Color(0xFF90A4AE),
                          size: 48,
                        ),
                      )
                    : isNetworkImage
                    ? Image.network(
                        imagePath,
                        fit: BoxFit.cover,
                        loadingBuilder: (context, child, loadingProgress) {
                          if (loadingProgress == null) return child;
                          return const Center(
                            child: CircularProgressIndicator(
                              color: AppCor.primary,
                            ),
                          );
                        },
                      )
                    : Image.asset(imagePath, fit: BoxFit.cover),
              ),
            ),

            Padding(
              padding: const EdgeInsets.all(24.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 6,
                    ),
                    decoration: BoxDecoration(
                      color: _corDaCategoria(
                        widget.noticia.categoria,
                      ).withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      widget.noticia.categoria.toUpperCase(),
                      style: TextStyle(
                        color: _corDaCategoria(widget.noticia.categoria),
                        fontWeight: FontWeight.bold,
                        fontSize: 12,
                        letterSpacing: 1,
                      ),
                    ),
                  ),
                  const SizedBox(height: 16),

                  Text(
                    widget.noticia.titulo,
                    style: const TextStyle(
                      fontSize: 26,
                      fontWeight: FontWeight.bold,
                      color: AppCor.textoPrimario,
                      height: 1.2,
                    ),
                  ),
                  const SizedBox(height: 24),

                  Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: AppCor.background,
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(
                        color: AppCor.textoCinza.withValues(alpha: 0.2),
                      ),
                    ),
                    child: Column(
                      children: [
                        NoticiasInfoBotao(
                          icone: Icons.calendar_month,
                          titulo: 'Data e Hora (Toque no ícone para inscrever)',
                          valor: widget.noticia.data,
                          iconeAcao: _processandoCampanha
                              ? Icons.hourglass_top
                              : (_inscrito ? Icons.alarm_on : Icons.add_alarm),
                          corAcao: _inscrito
                              ? AppCor.catVacinacao
                              : AppCor.catDoacoes,
                          aoClicar: (_ehCampanha && !_processandoCampanha)
                              ? _alternarInscricaoCampanha
                              : null,
                        ),
                        const Divider(height: 1),
                        NoticiasInfoBotao(
                          icone: Icons.location_on,
                          titulo: 'Local (Toque para abrir)',
                          valor: widget.noticia.local,
                          iconeAcao: Icons.map_outlined,
                          corAcao: AppCor.primary,
                          aoClicar: () async {
                            final url = Uri.parse(
                              'https://www.google.com/maps/search/?api=1&query=${Uri.encodeComponent(widget.noticia.local)}',
                            );
                            if (await canLaunchUrl(url)) {
                              await launchUrl(url);
                            }
                          },
                        ),
                        const Divider(height: 1),
                        NoticiasInfoBotao(
                          icone: Icons.business,
                          titulo: 'Órgão Publicador',
                          valor: widget.noticia.orgao,
                          iconeAcao: Icons.open_in_new,
                          corAcao: AppCor.primary,
                          aoClicar: () async {
                            final url = Uri.parse(widget.noticia.orgaoSite);
                            if (await canLaunchUrl(url)) {
                              await launchUrl(url);
                            }
                          },
                        ),
                        const Divider(height: 1),
                        NoticiasInfoBotao(
                          icone: Icons.phone,
                          titulo: 'Contato',
                          valor: widget.noticia.orgaoTelefone,
                          iconeAcao: Icons.call_outlined,
                          corAcao: AppCor.primary,
                          aoClicar: () async {
                            final url = Uri.parse(
                              'tel:${widget.noticia.orgaoTelefone}',
                            );
                            if (await canLaunchUrl(url)) {
                              await launchUrl(url);
                            }
                          },
                        ),
                        const Divider(height: 1),
                        NoticiasInfoBotao(
                          icone: Icons.people,
                          titulo: 'Público-Alvo',
                          valor: widget.noticia.publicoAlvo,
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 24),

                  const Text(
                    'Detalhes',
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.w600,
                      color: AppCor.textoPrimario,
                    ),
                  ),
                  const SizedBox(height: 12),

                  Text(
                    widget.noticia.descricao,
                    style: const TextStyle(
                      fontSize: 16,
                      height: 1.6,
                      color: Colors.black54,
                    ),
                    textAlign: TextAlign.justify,
                  ),

                  const SizedBox(height: 24),

                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton(
                      onPressed: () async {
                        final shareText =
                            '${widget.noticia.titulo}\n\n${widget.noticia.subtitulo}\n\n${widget.noticia.descricao}\n\nÓrgão: ${widget.noticia.orgao}\nContato: ${widget.noticia.orgaoTelefone}';
                        await Share.share(
                          shareText,
                          subject: widget.noticia.titulo,
                        );
                        await SupabaseService.trackNewsShare(
                          widget.noticia,
                          sharedBy: widget.sharedBy ?? 'app_user',
                        );
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: _corDaCategoria(
                          widget.noticia.categoria,
                        ),
                        padding: const EdgeInsets.symmetric(vertical: 14),
                      ),
                      child: const Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(Icons.share, color: Colors.white),
                          SizedBox(width: 8),
                          Text(
                            'Compartilhar',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                              color: Colors.white,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  const SizedBox(height: 40),

                  NoticiasSecaoMais(
                    noticiaAtual: widget.noticia,
                    userId: widget.userId,
                    userEmail: widget.sharedBy,
                  ),
                  const SizedBox(height: 20),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
