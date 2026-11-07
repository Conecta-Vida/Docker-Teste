import 'package:flutter/material.dart';
import '../global/appCor.dart';
import '../models/noticia.dart';
import '../controllers/noticia_controller.dart';
import '../screens/news_details_page.dart';

class NoticiasSecaoMais extends StatelessWidget {
  final NewsModel noticiaAtual;
  final int? userId;
  final String? userEmail;

  const NoticiasSecaoMais({
    super.key,
    required this.noticiaAtual,
    this.userId,
    this.userEmail,
  });

  @override
  Widget build(BuildContext context) {
    final controller = NewsController();
    List<NewsModel> todasAsNoticias = controller.getNoticiasPorCategoria(0);
    List<NewsModel> recomendadas = todasAsNoticias
        .where((n) => n.titulo != noticiaAtual.titulo)
        .toList();

    if (recomendadas.isEmpty) return const SizedBox.shrink();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Outras Campanhas',
          style: TextStyle(
            fontSize: 18,
            fontWeight: FontWeight.bold,
            color: AppCor.textoPrimario,
          ),
        ),
        const SizedBox(height: 16),
        GridView.builder(
          padding: EdgeInsets.zero,
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 2,
            crossAxisSpacing: 16,
            mainAxisSpacing: 16,
            childAspectRatio: 0.65,
          ),
          itemCount: recomendadas.length > 4 ? 4 : recomendadas.length,
          itemBuilder: (context, index) {
            return _construirCardDinamico(context, recomendadas[index]);
          },
        ),
      ],
    );
  }

  Widget _construirCardDinamico(
    BuildContext context,
    NewsModel noticiaRelacionada,
  ) {
    bool ehUrgente =
        noticiaRelacionada.categoria.toLowerCase().contains('sangue') ||
        noticiaRelacionada.categoria.toLowerCase().contains('alerta');

    return GestureDetector(
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => NewsDetailsPage(
              noticia: noticiaRelacionada,
              userId: userId,
              sharedBy: userEmail,
            ),
          ),
        );
      },
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: Colors.grey.withValues(alpha:0.1),
              spreadRadius: 1,
              blurRadius: 5,
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              flex: 3,
              child: Container(
                decoration: const BoxDecoration(
                  color: AppCor.background,
                  borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
                ),
                child: Center(
                  child: Icon(
                    ehUrgente ? Icons.warning_rounded : Icons.article,
                    size: 50,
                    color: ehUrgente ? AppCor.error : AppCor.primary,
                  ),
                ),
              ),
            ),
            Expanded(
              flex: 2,
              child: Padding(
                padding: const EdgeInsets.all(12.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    Text(
                      noticiaRelacionada.titulo,
                      style: const TextStyle(
                        fontWeight: FontWeight.bold,
                        fontSize: 13,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    Text(
                      noticiaRelacionada.categoria,
                      style: const TextStyle(
                        color: AppCor.textoCinza,
                        fontSize: 11,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          noticiaRelacionada.tag,
                          style: TextStyle(
                            fontWeight: FontWeight.w900,
                            fontSize: 12,
                            color: ehUrgente
                                ? AppCor.error
                                : AppCor.textoPrimario,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
