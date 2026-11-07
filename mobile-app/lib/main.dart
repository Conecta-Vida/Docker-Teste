import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/material.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:provider/provider.dart';
import 'global/appCor.dart';
import 'providers/usuario_provider.dart';
import 'screens/splash_screen.dart';
import 'services/push_notification_service.dart';
import 'services/supabase_service.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();

  try {
    // Inicializa o Hive para o Flutter
    await Hive.initFlutter();

    // Abre a "caixa" chamada 'preferencias' pra usar na pag. settings
    await Hive.openBox('preferencias');

    // Carrega o .env com os dados do supabase pra entrar
    await dotenv.load(fileName: ".env");

    // extrai o url e chave publica pra variavel dotenv.
    await SupabaseService.initializeCredentials(
      dotenv.env['SUPABASE_URL']!,
      dotenv.env['SUPABASE_ANON_KEY']!,
    );

    // Inicia ligação com banco de dados
    await SupabaseService.init();
    await SupabaseService.syncDefaultNoticias();

    // Inicializa Firebase e serviço de push notification
    await Firebase.initializeApp();
    await PushNotificationService.inicializar(
      apiBaseUrl: dotenv.env['API_BASE_URL'] ?? 'http://localhost:8080/api',
    );
  } catch (e) {
    // Se esquecer do ficheiro .env ou a net falhar, dá erro
    print('🚨 ERRO FATAL NO ARRANQUE: $e');
  }

  runApp(
    ChangeNotifierProvider(
      create: (_) => UsuarioProvider(),
      child: const ConectaVidaApp(),
    ),
  );
}

class ConectaVidaApp extends StatelessWidget {
  const ConectaVidaApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Conecta Vida',

      // ==================
      // THEMEDATA GLOBAL
      // ==================
      theme: ThemeData(
        // Fundo padrão
        scaffoldBackgroundColor: AppCor.background,

        // Cores Base
        colorScheme: const ColorScheme.light(
          primary: AppCor.primary,
          secondary: AppCor.secondary,
          error: AppCor.error,
        ),

        // AppBar
        appBarTheme: const AppBarTheme(
          backgroundColor: Colors.transparent,
          elevation: 0,
          centerTitle: true,
          iconTheme: IconThemeData(color: AppCor.textoPrimario),
          titleTextStyle: TextStyle(
            color: AppCor.textoPrimario,
            fontSize: 20,
            fontWeight: FontWeight.bold,
          ),
        ),

        //botões elevados
        elevatedButtonTheme: ElevatedButtonThemeData(
          style: ElevatedButton.styleFrom(
            backgroundColor: AppCor.primary, // Cor de fundo
            foregroundColor: Colors.white, // Cor do texto
            padding: const EdgeInsets.symmetric(vertical: 16),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
          ),
        ),

        //Cor primária
        iconTheme: const IconThemeData(color: AppCor.textoCinza),
      ),

      // ==========================================
      home: const SplashScreen(),
    );
  }
}
