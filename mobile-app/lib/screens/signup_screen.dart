import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:geolocator/geolocator.dart';
import 'package:provider/provider.dart';
import '../global/appCor.dart';
import '../models/usuario.dart';
import '../providers/usuario_provider.dart';
import '../services/supabase_service.dart';
import 'news_page.dart';

class SignupScreen extends StatefulWidget {
  const SignupScreen({super.key});

  @override
  State<SignupScreen> createState() => _SignupScreenState();
}

class _SignupScreenState extends State<SignupScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nomeController = TextEditingController();
  final _emailController = TextEditingController();
  final _senhaController = TextEditingController();
  final _idadeController = TextEditingController();
  final _cidadeController = TextEditingController();
  String _sexo = 'Feminino';
  bool _localizacaoAtiva = false;
  bool _carregandoLocalizacao = false;

  DateTime? _parseDataNascimento(String valor) {
    final partes = valor.trim().split('/');
    if (partes.length != 3) return null;

    final dia = int.tryParse(partes[0]);
    final mes = int.tryParse(partes[1]);
    final ano = int.tryParse(partes[2]);
    if (dia == null || mes == null || ano == null) return null;

    final agora = DateTime.now();
    if (ano < 1900 || ano > agora.year) return null;

    try {
      final data = DateTime(ano, mes, dia);
      if (data.year != ano || data.month != mes || data.day != dia) {
        return null;
      }
      return data;
    } catch (_) {
      return null;
    }
  }

  @override
  void dispose() {
    _nomeController.dispose();
    _emailController.dispose();
    _senhaController.dispose();
    _idadeController.dispose();
    _cidadeController.dispose();
    super.dispose();
  }

  Future<void> _solicitarLocalizacao() async {
    setState(() => _carregandoLocalizacao = true);
    bool servicoAtivo = await Geolocator.isLocationServiceEnabled();
    if (!servicoAtivo) {
      if (!mounted) return;
      setState(() => _carregandoLocalizacao = false);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Ative o serviço de localização no dispositivo.'),
        ),
      );
      return;
    }

    LocationPermission permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
    }

    if (permission == LocationPermission.denied ||
        permission == LocationPermission.deniedForever) {
      if (!mounted) return;
      setState(() => _carregandoLocalizacao = false);
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Permissão de localização não concedida.'),
        ),
      );
      return;
    }

    final posicao = await Geolocator.getCurrentPosition();
    final cidade = await SupabaseService.fetchCidadeFromCoords(
      posicao.latitude,
      posicao.longitude,
    );
    if (!mounted) return;
    setState(() {
      _cidadeController.text = cidade ?? '';
      _localizacaoAtiva = true;
      _carregandoLocalizacao = false;
    });
  }

  void _cadastro() async {
    if (_formKey.currentState!.validate()) {
      try {
        final dataNascimento = _parseDataNascimento(_idadeController.text);
        final anoNascimento = dataNascimento?.year;
        String localizacaoLimpa =
            _localizacaoAtiva || _cidadeController.text.isNotEmpty
            ? _cidadeController.text.trim()
            : 'Não informado';

        // Monta o usuário temporário (sem ID)
        final usuarioLocal = UserModel(
          nome: _nomeController.text.trim(),
          email: _emailController.text.trim(),
          dataNascimento: anoNascimento,
          sexo: _sexo,
          localizacao: localizacaoLimpa,
        );

        String senhaDigitada = _senhaController.text.trim();

        // 2. Manda o Java salvar no banco (O Java vai gerar o ID 32, por exemplo)
        await SupabaseService.signUp(usuarioLocal, senhaDigitada);

        // 3. Faz login automático por baixo dos panos para ganhar o Token JWT
        await SupabaseService.signIn(usuarioLocal.email, senhaDigitada);

        // 4. AGORA SIM! Busca o perfil oficial do banco, que vem com o ID preenchido
        final usuarioOficialComId = await SupabaseService.fetchUsuarioByEmail(
          usuarioLocal.email,
        );

        if (!mounted) return;
        // ignore: use_build_context_synchronously
        context.read<UsuarioProvider>().setUsuario(usuarioOficialComId ?? usuarioLocal);
        // ignore: use_build_context_synchronously
        Navigator.pushReplacement(
          context,
          MaterialPageRoute(
            builder: (context) => const HomeScreen(),
          ),
        );
      } catch (error) {
        if (!mounted) return;
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(error.toString().replaceAll('Exception: ', '')),
            backgroundColor: AppCor.error,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Cadastro')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const Text(
                'Crie sua conta para acessar as novidades de saúde.',
                style: TextStyle(color: AppCor.textoCinza, fontSize: 16),
              ),
              const SizedBox(height: 24),
              TextFormField(
                controller: _nomeController,
                decoration: const InputDecoration(
                  labelText: 'Nome completo',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(15.0)),
                  ),
                ),
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return 'Digite seu nome completo';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _emailController,
                keyboardType: TextInputType.emailAddress,
                decoration: const InputDecoration(
                  labelText: 'Email',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(15.0)),
                  ),
                ),
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return 'Digite seu email';
                  }
                  if (!value.contains('@') || !value.contains('.')) {
                    return 'Digite um email válido';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _senhaController,
                obscureText: true,
                decoration: const InputDecoration(
                  labelText: 'Senha',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(15.0)),
                  ),
                ),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return 'Digite sua senha';
                  }
                  if (value.length < 6) {
                    return 'A senha deve ter ao menos 6 caracteres';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _idadeController,
                keyboardType: TextInputType.number,
                inputFormatters: [
                  FilteringTextInputFormatter.digitsOnly,
                  const _DataNascimentoFormatter(),
                ],
                decoration: const InputDecoration(
                  labelText: 'Data de Nascimento (DD/MM/AAAA)',
                  hintText: 'Ex.: 15/08/2001',
                  counterText: "",
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(15.0)),
                  ),
                ),
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return 'Digite sua data de nascimento';
                  }
                  final data = _parseDataNascimento(value);
                  if (data == null) {
                    return 'Digite uma data válida no formato DD/MM/AAAA';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                initialValue: _sexo,
                decoration: const InputDecoration(
                  labelText: 'Sexo',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.all(Radius.circular(15.0)),
                  ),
                ),
                items: const [
                  DropdownMenuItem(value: 'Feminino', child: Text('Feminino')),
                  DropdownMenuItem(
                    value: 'Masculino',
                    child: Text('Masculino'),
                  ),
                  DropdownMenuItem(
                    value: 'Prefiro não informar',
                    child: Text('Prefiro não informar'),
                  ),
                ],
                onChanged: (value) {
                  if (value != null) {
                    setState(() => _sexo = value);
                  }
                },
              ),
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: _carregandoLocalizacao
                    ? null
                    : _solicitarLocalizacao,
                child: Text(
                  _carregandoLocalizacao
                      ? 'Detectando cidade...'
                      : (_localizacaoAtiva
                            ? 'Localização detectada ✓'
                            : 'Detectar minha cidade'),
                ),
              ),
              if (_localizacaoAtiva || _cidadeController.text.isNotEmpty) ...[
                const SizedBox(height: 16),
                TextFormField(
                  controller: _cidadeController,
                  decoration: const InputDecoration(
                    labelText: 'Cidade',
                    prefixIcon: Icon(Icons.location_on_outlined),
                    helperText: 'Detectada pelo GPS — edite se necessário',
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.all(Radius.circular(15.0)),
                    ),
                  ),
                ),
              ],
              const SizedBox(height: 24),
              ElevatedButton(
                onPressed: _cadastro,
                child: const Text(
                  'Finalizar cadastro',
                  style: TextStyle(fontSize: 18),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _DataNascimentoFormatter extends TextInputFormatter {
  const _DataNascimentoFormatter();

  @override
  TextEditingValue formatEditUpdate(
    TextEditingValue oldValue,
    TextEditingValue newValue,
  ) {
    final digits = newValue.text.replaceAll(RegExp(r'[^0-9]'), '');
    final limitado = digits.length > 8 ? digits.substring(0, 8) : digits;

    final buffer = StringBuffer();
    for (int i = 0; i < limitado.length; i++) {
      if (i == 2 || i == 4) buffer.write('/');
      buffer.write(limitado[i]);
    }

    final textoFormatado = buffer.toString();
    return TextEditingValue(
      text: textoFormatado,
      selection: TextSelection.collapsed(offset: textoFormatado.length),
    );
  }
}
