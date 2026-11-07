import 'package:flutter/material.dart';
import '../global/appCor.dart';

class NoticiasHeader extends StatelessWidget {
  final String nome;
  final VoidCallback? onNotifications;
  final VoidCallback? onCalendar;
  final VoidCallback? onSettings;
  final ValueChanged<String>? onSearch;

  const NoticiasHeader({
    super.key,
    required this.nome,
    this.onNotifications,
    this.onCalendar,
    this.onSettings,
    this.onSearch,
  });

  @override
  Widget build(BuildContext context) {
    return Stack(
      clipBehavior: Clip.none,
      alignment: Alignment.bottomCenter,
      children: [
        Container(
          width: double.infinity,
          padding: EdgeInsets.only(
            top: MediaQuery.of(context).padding.top + 10,
            left: 24,
            right: 24,
            bottom: 70,
          ),
          decoration: const BoxDecoration(
            color: AppCor.primary,
            borderRadius: BorderRadius.vertical(bottom: Radius.circular(30)),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: [
                  _buildIconButton(Icons.notifications_outlined, onNotifications),
                  const SizedBox(width: 10),
                  _buildIconButton(Icons.calendar_month, onCalendar),
                  const SizedBox(width: 10),
                  _buildIconButton(Icons.settings, onSettings),
                ],
              ),
              const SizedBox(height: 20),
              Text(
                'Olá, $nome',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 8),
              const Text(
                'Confira as novidades da saúde hoje.',
                style: TextStyle(color: Colors.white70, fontSize: 14),
              ),
            ],
          ),
        ),
        Positioned(bottom: -25, left: 24, right: 24, child: _buildSearchBar()),
      ],
    );
  }

  Widget _buildIconButton(IconData icon, VoidCallback? action) {
    return GestureDetector(
      onTap: action,
      child: Container(
        width: 42,
        height: 42,
        decoration: BoxDecoration(
          color: Colors.white.withValues(alpha:0.2),
          shape: BoxShape.circle,
        ),
        child: Icon(icon, color: Colors.white, size: 22),
      ),
    );
  }

  Widget _buildSearchBar() {
    return Container(
      height: 50,
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(25),
        boxShadow: [
          BoxShadow(color: Colors.black.withValues(alpha:0.1), blurRadius: 10),
        ],
      ),
      child: TextField(
        onChanged: onSearch,
        decoration: const InputDecoration(
          hintText: 'Pesquisar notícias...',
          prefixIcon: Icon(Icons.search),
          border: InputBorder.none,
          contentPadding: EdgeInsets.symmetric(vertical: 15),
        ),
      ),
    );
  }
}
