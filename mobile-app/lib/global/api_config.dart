import 'package:flutter/foundation.dart';
import 'package:flutter_dotenv/flutter_dotenv.dart';

/// Resolve a URL da mobile-api (porta 8081 no Docker Swarm).
///
/// Ordem de prioridade:
///  1. `flutter run --dart-define=API_BASE_URL=http://192.168.0.10:8081/api`
///  2. `API_BASE_URL` no arquivo `.env`
///  3. Padrão por plataforma:
///     - Emulador Android -> http://10.0.2.2:8081/api (10.0.2.2 = PC do emulador)
///     - Web / desktop / iOS simulator -> http://localhost:8081/api
class ApiConfig {
  static const String _definidaNoBuild = String.fromEnvironment('API_BASE_URL');

  static String resolverBaseUrl() {
    if (_definidaNoBuild.isNotEmpty) return _semBarraFinal(_definidaNoBuild);

    final doEnv = dotenv.isInitialized ? dotenv.env['API_BASE_URL']?.trim() : null;
    if (doEnv != null && doEnv.isNotEmpty) return _semBarraFinal(doEnv);

    if (!kIsWeb && defaultTargetPlatform == TargetPlatform.android) {
      return 'http://10.0.2.2:8081/api';
    }
    return 'http://localhost:8081/api';
  }

  static String _semBarraFinal(String url) =>
      url.endsWith('/') ? url.substring(0, url.length - 1) : url;
}
