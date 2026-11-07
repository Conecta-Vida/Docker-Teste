import 'package:flutter/material.dart';

class LoginController {
  final formKey = GlobalKey<FormState>();

  final emailController = TextEditingController();
  final senhaController = TextEditingController();

  bool tentarLogin() {
    if (formKey.currentState!.validate()) {
      return true;
    }
    return false;
  }

  // fazer dispose depois de usar para não gerar problema na memória
  void limparMemoria() {
    emailController.dispose();
    senhaController.dispose();
  }
}
