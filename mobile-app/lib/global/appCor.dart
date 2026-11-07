import 'package:flutter/material.dart';

class AppCor {
  // ==========================================
  // CORES PRINCIPAIS (Atualizadas)
  // ==========================================
  static const Color primary = Color.fromARGB(255, 65, 87, 255);
  static const Color secondary = Color.fromARGB(255, 128, 181, 255);

  // ==========================================
  // CORES DE SUPORTE
  // ==========================================
  static const Color background = Color(0xFFF5F5F5);
  static const Color error = Color(
    0xFFD32F2F,
  ); // Perfeita para os erros do Formulário!

  // ==========================================
  // CORES DE TEXTO
  // ==========================================
  static const Color textoPrimario = Color(
    0xFF212121,
  ); // Corrigido para Hex válido
  static const Color textoSecundario = Color.fromARGB(255, 250, 248, 248);
  static const Color textoCinza = Color(0xFF9E9E9E);

  // Cores de Título originais (mantidas para a tela de notícias)
  static const Color textTitle = Color(0xFF2C3E50);
  static const Color textSubtitle = Color(0xFF546E7A);

  // ==========================================
  // CORES DAS CATEGORIAS (Tela de Notícias)
  // ==========================================
  static const Color catNoticias = Color(0xFFFF6B8B);
  static const Color catVacinacao = Color(0xFF00C996);
  static const Color catDoacoes = Color(0xFFFF9548);
  static const Color catUrgentes = Color(0xFF3B82F6);
  static const Color catEventos = Color(0xFF8B5CF6);
}
