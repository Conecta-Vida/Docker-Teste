// lib/components/news_card.dart
import 'package:flutter/material.dart';
import '../models/noticia.dart';
import '../screens/news_details_page.dart';

class NoticiaCard extends StatelessWidget {
  final NewsModel noticia;
  final String? userEmail;
  final int? userId;

  const NoticiaCard({
    super.key,
    required this.noticia,
    this.userEmail,
    this.userId,
  });

  @override
  Widget build(BuildContext context) {
    final String imagePath = noticia.imagem.trim();
    final bool hasImage = imagePath.isNotEmpty;
    final bool isNetworkImage = hasImage && imagePath.startsWith('http');

    return GestureDetector(
      onTap: () {
        // Passa dados para os Detalhes
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) =>
                NewsDetailsPage(
                  noticia: noticia,
                  sharedBy: userEmail,
                  userId: userId,
                ),
          ),
        );
      },
      child: Container(
        width: double.infinity,
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(24),
          boxShadow: [
            BoxShadow(
              color: Colors.blue.withValues(alpha: 0.08),
              blurRadius: 15,
              offset: const Offset(0, 5),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ClipRRect(
              borderRadius: const BorderRadius.vertical(
                top: Radius.circular(24),
              ),
              child: Hero(
                tag: noticia.titulo + noticia.data,
                child: !hasImage
                    ? Container(
                        height: 140,
                        width: double.infinity,
                        color: const Color(0xFFEAF3FB),
                        alignment: Alignment.center,
                        child: const Icon(
                          Icons.image_not_supported_outlined,
                          color: Color(0xFF90A4AE),
                          size: 34,
                        ),
                      )
                    : isNetworkImage
                    ? Image.network(
                        imagePath,
                        height: 140,
                        width: double.infinity,
                        fit: BoxFit.cover,
                      )
                    : Image.asset(
                        imagePath,
                        height: 140,
                        width: double.infinity,
                        fit: BoxFit.cover,
                      ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 10,
                          vertical: 6,
                        ),
                        decoration: BoxDecoration(
                          color: const Color(0xFF1E88E5).withValues(alpha: 0.1),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Text(
                          noticia.tag,
                          style: const TextStyle(
                            color: Color(0xFF1E88E5),
                            fontSize: 12,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                      Text(
                        noticia.data,
                        style: TextStyle(
                          color: Colors.grey.shade500,
                          fontSize: 12,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 12),
                  Text(
                    noticia.titulo,
                    style: const TextStyle(
                      fontSize: 17,
                      fontWeight: FontWeight.w800,
                      color: Color(0xFF2C3E50),
                      height: 1.3,
                    ),
                  ),

                  const SizedBox(height: 8),

                  Text(
                    noticia.descricao,
                    maxLines: 2,
                    overflow: TextOverflow
                        .ellipsis, // Adiciona "..." se o texto for maior que as 2 linhas
                    style: TextStyle(fontSize: 14, color: Colors.grey.shade600),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
