import 'package:flutter/material.dart';
import '../models/usuario.dart';

class UsuarioProvider extends ChangeNotifier {
  UserModel? _usuario;

  UserModel? get usuario => _usuario;

  // Define o usuário logado e notifica os widgets dependentes
  void setUsuario(UserModel usuario) {
    _usuario = usuario;
    notifyListeners();
  }

  // Limpa os dados ao fazer logout
  void limpar() {
    _usuario = null;
    notifyListeners();
  }
}
