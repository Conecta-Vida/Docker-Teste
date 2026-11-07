import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:hive/hive.dart';
import 'package:http/http.dart' as http;
import 'package:provider/provider.dart';

import '../global/appCor.dart';
import '../models/usuario.dart';
import '../providers/usuario_provider.dart';
import '../services/supabase_service.dart';
import 'login_screen.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  // Estado do formulário de Conta
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _nomeController;
  late TextEditingController _idadeController;
  late TextEditingController _cidadeController;
  late String _sexo;
  bool _salvando = false;
  bool _buscandoGPS = false;

  // Estado das configurações do Hive
  bool _notificacoesAtivas = true;
  bool _usarLocalizacao = false;
  List<String> _regioesDisponiveis = [];
  List<String> _regioesSelecionadas = [];
  late Box _preferenciasBox;
  bool _carregandoRegioes = true;

  @override
  void initState() {
    super.initState();
    // Inicializa campos de texto com os dados do usuário atual
    final usuario = context.read<UsuarioProvider>().usuario!;
    _nomeController = TextEditingController(text: usuario.nome);
    _idadeController = TextEditingController(
      text: usuario.dataNascimento?.toString() ?? '',
    );
    _cidadeController = TextEditingController(text: usuario.localizacao);

    String sexoBanco = usuario.sexo;
    if (sexoBanco == 'Feminino' || sexoBanco == 'Masculino') {
      _sexo = sexoBanco;
    } else {
      _sexo = 'Não informado';
    }

    _preferenciasBox = Hive.box('preferencias');
    _carregarConfiguracoesLocais(usuario);
    _buscarRegioesDoBanco();
  }

  @override
  void dispose() {
    _nomeController.dispose();
    _idadeController.dispose();
    _cidadeController.dispose();
    super.dispose();
  }

  // --- MÉTODOS DE GERENCIAMENTO DE CONTA ---

  Future<void> _recalibrarGPS() async {
    setState(() => _buscandoGPS = true);
    try {
      bool servicoAtivo = await Geolocator.isLocationServiceEnabled();
      if (!servicoAtivo) throw Exception('Ative o GPS do celular.');

      LocationPermission permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }
      if (permission == LocationPermission.denied ||
          permission == LocationPermission.deniedForever) {
        throw Exception('Permissão de localização negada.');
      }

      final posicao = await Geolocator.getCurrentPosition();
      final cidadeEncontrada = await SupabaseService.fetchCidadeFromCoords(
        posicao.latitude,
        posicao.longitude,
      );

      if (cidadeEncontrada != null && mounted) {
        setState(() => _cidadeController.text = cidadeEncontrada);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Localização atualizada!'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(e.toString()), backgroundColor: AppCor.error),
        );
      }
    } finally {
      if (mounted) setState(() => _buscandoGPS = false);
    }
  }

  Future<void> _salvarDados() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _salvando = true);
    try {
      final usuarioAtual = context.read<UsuarioProvider>().usuario!;
      final userAtt = UserModel(
        id: usuarioAtual.id,
        nome: _nomeController.text.trim(),
        email: usuarioAtual.email,
        dataNascimento: int.tryParse(_idadeController.text.trim()),
        sexo: _sexo,
        localizacao: _cidadeController.text.trim(),
      );

      await SupabaseService.upsertUsuario(userAtt);
      context.read<UsuarioProvider>().setUsuario(userAtt);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Dados salvos com sucesso!'),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erro: $e'), backgroundColor: AppCor.error),
        );
      }
    } finally {
      if (mounted) setState(() => _salvando = false);
    }
  }

  void _fazerLogout() {
    SupabaseService.signOut();
    context.read<UsuarioProvider>().limpar();
    Navigator.of(context).pushAndRemoveUntil(
      MaterialPageRoute(builder: (context) => const LoginScreen()),
      (route) => false,
    );
  }

  void _confirmarDeletarConta() {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Deletar Conta', style: TextStyle(color: Colors.red)),
        content: const Text(
          'Tem certeza? Essa ação apagará todos os seus dados e não pode ser desfeita.',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(ctx).pop(),
            child: const Text('Cancelar', style: TextStyle(color: Colors.grey)),
          ),
          ElevatedButton(
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            onPressed: () async {
              Navigator.of(ctx).pop();
              try {
                await SupabaseService.deleteUsuario(context.read<UsuarioProvider>().usuario!.email);
                _fazerLogout();
              } catch (e) {
                ScaffoldMessenger.of(context).showSnackBar(
                  SnackBar(
                    content: Text('Erro: $e'),
                    backgroundColor: AppCor.error,
                  ),
                );
              }
            },
            child: const Text('Sim, Excluir'),
          ),
        ],
      ),
    );
  }

  // --- MÉTODOS DO HIVE (MANTIDOS INTACTOS) ---
  void _carregarConfiguracoesLocais(UserModel usuario) {
    setState(() {
      _notificacoesAtivas = _preferenciasBox.get(
        'notificacoes_ligadas',
        defaultValue: true,
      );
      _usarLocalizacao = _preferenciasBox.get(
        'usar_gps',
        defaultValue: usuario.localizacao != 'Não autorizado',
      );
      _regioesSelecionadas = List<String>.from(
        _preferenciasBox.get('regioes_interesse', defaultValue: <String>[]),
      );
    });
  }

  Future<void> _buscarRegioesDoBanco() async {
    final Set<String> localizacoesUnicas = {};
    final String baseUrl = SupabaseService.baseUrl;
    try {
      final resComunicacoes = await http.get(
        Uri.parse('$baseUrl/comunicacoes'),
      );
      if (resComunicacoes.statusCode == 200) {
        final List dynamicList = jsonDecode(resComunicacoes.body);
        for (var item in dynamicList) {
          if (item['localizacao'] != null &&
              item['localizacao'].toString().isNotEmpty) {
            localizacoesUnicas.add(item['localizacao'].toString().trim());
          }
        }
      }
    } catch (e) {
      localizacoesUnicas.addAll([
        'Joanópolis',
        'Piracaia',
        'Bragança Paulista',
      ]);
    }
    setState(() {
      _regioesDisponiveis = localizacoesUnicas.toList()..sort();
      _carregandoRegioes = false;
    });
  }

  Future<void> _salvarRegiao(String cidade, bool selecionado) async {
    setState(() {
      if (selecionado) {
        if (!_regioesSelecionadas.contains(cidade))
          _regioesSelecionadas.add(cidade);
      } else {
        _regioesSelecionadas.remove(cidade);
      }
    });
    await _preferenciasBox.put('regioes_interesse', _regioesSelecionadas);
  }

  // --- CONSTRUÇÃO DA TELA ---
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF2F2F7), // Fundo cinza claro iOS
      appBar: AppBar(
        title: const Text(
          'Ajustes',
          style: TextStyle(fontWeight: FontWeight.w600),
        ),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 20.0),
          children: [
            // 1. SECÇÃO NO TOPO: GERENCIAR CONTA
            _buildSecaoTitulo('GERENCIAR CONTA'),
            _buildContainerGrupo([
              Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 4,
                ),
                child: TextFormField(
                  controller: _nomeController,
                  decoration: const InputDecoration(
                    labelText: 'Nome completo',
                    border: InputBorder.none,
                  ),
                  validator: (val) => val!.isEmpty ? 'Obrigatório' : null,
                ),
              ),
              const Divider(height: 1, indent: 16, endIndent: 16),
              Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 4,
                ),
                child: Row(
                  children: [
                    Expanded(
                      flex: 1,
                      child: TextFormField(
                        controller: _idadeController,
                        keyboardType: TextInputType.number,
                        decoration: const InputDecoration(
                          labelText: 'Ano Nasc.',
                          border: InputBorder.none,
                        ),
                      ),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      flex: 2,
                      child: DropdownButtonFormField<String>(
                        value: _sexo,
                        decoration: const InputDecoration(
                          labelText: 'Sexo',
                          border: InputBorder.none,
                        ),
                        items: const [
                          DropdownMenuItem(
                            value: 'Feminino',
                            child: Text('Feminino'),
                          ),
                          DropdownMenuItem(
                            value: 'Masculino',
                            child: Text('Masculino'),
                          ),
                          DropdownMenuItem(
                            value: 'Não informado',
                            child: Text('Não informar'),
                          ),
                        ],
                        onChanged: (val) {
                          if (val != null) {
                            setState(() => _sexo = val);
                          }
                        },
                      ),
                    ),
                  ],
                ),
              ),
              const Divider(height: 1, indent: 16, endIndent: 16),
              Padding(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 4,
                ),
                child: Row(
                  children: [
                    Expanded(
                      child: TextFormField(
                        controller: _cidadeController,
                        decoration: const InputDecoration(
                          labelText: 'Cidade atual',
                          border: InputBorder.none,
                        ),
                      ),
                    ),
                    IconButton(
                      icon: _buscandoGPS
                          ? const SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            )
                          : const Icon(Icons.gps_fixed, color: AppCor.primary),
                      onPressed: _buscandoGPS ? null : _recalibrarGPS,
                    ),
                  ],
                ),
              ),
              const Divider(height: 1, indent: 16, endIndent: 16),
              ListTile(
                title: const Text(
                  'E-mail (Não editável)',
                  style: TextStyle(color: Colors.grey, fontSize: 13),
                ),
                subtitle: Text(
                  context.watch<UsuarioProvider>().usuario!.email,
                  style: const TextStyle(color: Colors.black, fontSize: 16),
                ),
              ),
            ]),

            const SizedBox(height: 12),

            // Botão de Salvar Alterações
            ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: AppCor.primary,
                padding: const EdgeInsets.symmetric(vertical: 14),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
              onPressed: _salvando ? null : _salvarDados,
              child: _salvando
                  ? const Text(
                      'Salvando...',
                      style: TextStyle(color: Colors.white, fontSize: 16),
                    )
                  : const Text(
                      'Salvar Alterações',
                      style: TextStyle(color: Colors.white, fontSize: 16),
                    ),
            ),

            const SizedBox(height: 12),

            // Botões de Logout e Deletar logo abaixo de salvar
            Row(
              children: [
                Expanded(
                  child: OutlinedButton.icon(
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.orange,
                      side: const BorderSide(color: Colors.orange),
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                    icon: const Icon(Icons.logout),
                    label: const Text('Sair'),
                    onPressed: _fazerLogout,
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: OutlinedButton.icon(
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.red,
                      side: const BorderSide(color: Colors.red),
                      padding: const EdgeInsets.symmetric(vertical: 12),
                    ),
                    icon: const Icon(Icons.delete_forever),
                    label: const Text('Deletar'),
                    onPressed: _confirmarDeletarConta,
                  ),
                ),
              ],
            ),

            const SizedBox(height: 32),

            // 2. SECÇÃO: ALERTAS (MANTIDO INTACTO ABAIXO)
            _buildSecaoTitulo('ALERTAS DE SAÚDE'),
            _buildContainerGrupo([
              SwitchListTile.adaptive(
                title: const Text(
                  'Notificações Push',
                  style: TextStyle(fontSize: 16),
                ),
                subtitle: const Text('Alertas críticos de epidemias e vacinas'),
                value: _notificacoesAtivas,
                activeColor: AppCor.primary,
                contentPadding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 4,
                ),
                onChanged: (value) async {
                  setState(() => _notificacoesAtivas = value);
                  await _preferenciasBox.put('notificacoes_ligadas', value);
                },
              ),
              const Divider(height: 1, indent: 16, endIndent: 16),
              SwitchListTile.adaptive(
                title: const Text(
                  'Usar Localização Atual',
                  style: TextStyle(fontSize: 16),
                ),
                subtitle: const Text('Filtra dados via GPS do aparelho'),
                value: _usarLocalizacao,
                activeColor: AppCor.primary,
                contentPadding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 4,
                ),
                onChanged: (value) async {
                  setState(() => _usarLocalizacao = value);
                  await _preferenciasBox.put('usar_gps', value);
                },
              ),
            ]),

            const SizedBox(height: 28),

            // 3. SECÇÃO: REGIÕES DE INTERESSE (MANTIDO INTACTO ABAIXO)
            _buildSecaoTitulo('REGIÕES DE INTERESSE'),
            _carregandoRegioes
                ? const Center(
                    child: Padding(
                      padding: EdgeInsets.all(16.0),
                      child: CircularProgressIndicator.adaptive(),
                    ),
                  )
                : _regioesDisponiveis.isEmpty
                ? _buildContainerGrupo([
                    const ListTile(
                      title: Text(
                        'Nenhuma região encontrada',
                        style: TextStyle(color: Colors.grey),
                      ),
                    ),
                  ])
                : _buildContainerGrupo(
                    _regioesDisponiveis.map((cidade) {
                      return Column(
                        children: [
                          CheckboxListTile(
                            title: Text(
                              cidade,
                              style: const TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                            value: _regioesSelecionadas.contains(cidade),
                            activeColor: AppCor.primary,
                            controlAffinity: ListTileControlAffinity.trailing,
                            contentPadding: const EdgeInsets.symmetric(
                              horizontal: 16,
                            ),
                            onChanged: (bool? value) =>
                                _salvarRegiao(cidade, value ?? false),
                          ),
                          if (cidade != _regioesDisponiveis.last)
                            const Divider(height: 1, indent: 16, endIndent: 16),
                        ],
                      );
                    }).toList(),
                  ),

            const SizedBox(height: 40),
          ],
        ),
      ),
    );
  }

  Widget _buildSecaoTitulo(String titulo) {
    return Padding(
      padding: const EdgeInsets.only(left: 8.0, bottom: 8.0),
      child: Text(
        titulo,
        style: const TextStyle(
          fontSize: 12,
          fontWeight: FontWeight.w600,
          color: Color(0xFF8E8E93),
          letterSpacing: 0.5,
        ),
      ),
    );
  }

  Widget _buildContainerGrupo(List<Widget> children) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.03),
            blurRadius: 10,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(12),
        child: Column(mainAxisSize: MainAxisSize.min, children: children),
      ),
    );
  }
}
