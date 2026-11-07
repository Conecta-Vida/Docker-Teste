import 'package:flutter/material.dart';
import '../global/appCor.dart';

class Categorias extends StatelessWidget {
  final int categoryIndex;
  final Function(int) onTap;

  const Categorias({
    super.key,
    required this.categoryIndex,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Padding(
          padding: EdgeInsets.symmetric(horizontal: 24),
          child: Text(
            'Categorias',
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.bold,
              color: AppCor.textTitle,
            ),
          ),
        ),
        const SizedBox(height: 20),
        SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          padding: const EdgeInsets.symmetric(horizontal: 24),
          child: Row(
            children: [
              _categoryItem('Notícias', AppCor.catNoticias, Icons.article, 0),
              _categoryItem(
                'Vacinação',
                AppCor.catVacinacao,
                Icons.vaccines,
                1,
              ),
              _categoryItem('Doações', AppCor.catDoacoes, Icons.water_drop, 2),
              _categoryItem(
                'Urgentes',
                AppCor.catUrgentes,
                Icons.warning_rounded,
                3,
              ),
              _categoryItem('Eventos', AppCor.catEventos, Icons.event, 4),
            ],
          ),
        ),
      ],
    );
  }

  Widget _categoryItem(String label, Color color, IconData icon, int index) {
    bool isSelected = categoryIndex == index;

    return GestureDetector(
      onTap: () => onTap(index),
      child: Container(
        margin: const EdgeInsets.only(right: 16),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 20),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(40),
          border: Border.all(
            color: isSelected ? color : Colors.transparent,
            width: 2,
          ),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha:0.03),
              blurRadius: 10,
              offset: const Offset(0, 5),
            ),
          ],
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 55,
              height: 55,
              decoration: BoxDecoration(color: color, shape: BoxShape.circle),
              child: Icon(icon, color: Colors.white, size: 28),
            ),
            const SizedBox(height: 16),
            Text(
              label,
              style: const TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w600,
                color: AppCor.textSubtitle,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
