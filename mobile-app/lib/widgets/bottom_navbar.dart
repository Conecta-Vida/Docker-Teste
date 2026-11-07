import 'package:flutter/material.dart';
import '../global/appCor.dart';

class CustomBottomNavBar extends StatelessWidget {
  final int currentIndex;
  final Function(int) onTap;

  const CustomBottomNavBar({
    super.key,
    required this.currentIndex,
    required this.onTap,
  });

  static const _items = [
    {
      'active': Icons.article,
      'inactive': Icons.article_outlined,
      'label': 'Notícias',
    },
    {
      'active': Icons.vaccines,
      'inactive': Icons.vaccines_outlined,
      'label': 'Vacinas',
    },
    {
      'active': Icons.water_drop,
      'inactive': Icons.water_drop_outlined,
      'label': 'Doações',
    },
    {
      'active': Icons.warning_rounded,
      'inactive': Icons.warning_amber_outlined,
      'label': 'Alertas',
    },
    {
      'active': Icons.campaign,
      'inactive': Icons.campaign_outlined,
      'label': 'Campanhas',
    },
  ];

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 70,
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.04),
            blurRadius: 15,
            offset: const Offset(0, -5),
          ),
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: List.generate(
          _items.length,
          (index) => _buildNavItem(
            _items[index]['active'] as IconData,
            _items[index]['inactive'] as IconData,
            index,
          ),
        ),
      ),
    );
  }

  Widget _buildNavItem(IconData activeIcon, IconData inactiveIcon, int index) {
    bool isSelected = currentIndex == index;
    return GestureDetector(
      onTap: () => onTap(index),
      behavior: HitTestBehavior.opaque,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 10.0, horizontal: 10.0),
        child: Icon(
          isSelected ? activeIcon : inactiveIcon,
          color: isSelected ? AppCor.primary : Colors.grey.shade400,
          size: 28,
        ),
      ),
    );
  }
}
