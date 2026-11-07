import 'dart:convert';
import 'dart:developer' as developer;

import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:hive/hive.dart'; //Adicionado para ler as configurações locais
import 'package:http/http.dart' as http;

// Handler executado quando o app está FECHADO ou em BACKGROUND
// DEVE ficar fora de qualquer classe (nível global)
@pragma('vm:entry-point')
Future<void> firebaseBackgroundHandler(RemoteMessage message) async {
  developer.log(
    'Push recebido em background: ${message.notification?.title}',
    name: 'FCM',
  );
}

class PushNotificationService {
  static final FlutterLocalNotificationsPlugin _localNotifications =
      FlutterLocalNotificationsPlugin();

  static const AndroidNotificationChannel _canal = AndroidNotificationChannel(
    'ifsp_channel',
    'IFSP Notificações',
    description: 'Canal de notificações locais do Conecta Vida',
    importance: Importance.max,
    playSound: true,
    enableVibration: true,
  );

  static const NotificationDetails _notificationDetails = NotificationDetails(
    android: AndroidNotificationDetails(
      'ifsp_channel',
      'IFSP Notificações',
      channelDescription: 'Canal de notificações locais do Conecta Vida',
      importance: Importance.max,
      priority: Priority.high,
      icon: '@drawable/ifsp_logo',
      actions: <AndroidNotificationAction>[
        AndroidNotificationAction('0', 'Ok'),
        AndroidNotificationAction('1', 'Cancel'),
      ],
    ),
  );

  /// Inicializar o serviço — chamar no main() antes do runApp
  static Future<void> inicializar({required String apiBaseUrl}) async {
    // Registra o handler de background
    FirebaseMessaging.onBackgroundMessage(firebaseBackgroundHandler);

    // Configuração do plugin de notificações locais
    const AndroidInitializationSettings androidSettings =
        AndroidInitializationSettings('@drawable/ifsp_logo');

    const InitializationSettings initSettings = InitializationSettings(
      android: androidSettings,
    );

    await _localNotifications.initialize(
      initSettings,
      onDidReceiveNotificationResponse: _onDidReceiveNotificationResponse,
    );

    // Solicita permissão de notificação no Android (13+)
    await _localNotifications
        .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin
        >()
        ?.requestNotificationsPermission();

    // Cria canal de notificação no Android
    await _localNotifications
        .resolvePlatformSpecificImplementation<
          AndroidFlutterLocalNotificationsPlugin
        >()
        ?.createNotificationChannel(_canal);

    // Solicita permissão do FCM
    final messaging = FirebaseMessaging.instance;
    await messaging.requestPermission(
      alert: true,
      badge: true,
      sound: true,
      criticalAlert: true,
    );

    // Obtém e envia o token FCM para o backend
    final token = await messaging.getToken();
    if (token != null) {
      developer.log('Token FCM obtido: $token', name: 'FCM');
      await _enviarTokenParaBackend(token, apiBaseUrl);
    }

    // Atualiza token quando ele for renovado
    messaging.onTokenRefresh.listen((novoToken) {
      developer.log('Token FCM renovado: $novoToken', name: 'FCM');
      _enviarTokenParaBackend(novoToken, apiBaseUrl);
    });

    // Push recebido com o app ABERTO → exibe notificação local
    FirebaseMessaging.onMessage.listen((RemoteMessage message) async {
      // Atualizado para async
      await _exibirNotificacaoLocal(message);
    });

    // Push clicado com o app em BACKGROUND (não fechado)
    FirebaseMessaging.onMessageOpenedApp.listen((RemoteMessage message) {
      developer.log(
        'App aberto por push: ${message.notification?.title}',
        name: 'FCM',
      );
    });
  }

  /// Verifica se o usuário permitiu notificações no SettingsScreen
  static Future<bool> _notificacoesAtivas() async {
    try {
      if (!Hive.isBoxOpen('preferencias')) {
        await Hive.openBox('preferencias');
      }
      final box = Hive.box('preferencias');
      return box.get('notificacoes_ligadas', defaultValue: true);
    } catch (e) {
      developer.log('Erro ao ler Hive no FCM: $e', name: 'FCM');
      return true; // Fallback de segurança
    }
  }

  /// Exibe notificação local na bandeja do sistema
  static Future<void> _exibirNotificacaoLocal(RemoteMessage message) async {
    if (!await _notificacoesAtivas()) {
      developer.log(
        'Push silenciado pelas configurações do usuário.',
        name: 'FCM',
      );
      return;
    }

    final notification = message.notification;
    if (notification == null) return;

    showNotification(
      notification.title ?? 'Notificação',
      notification.body ?? '',
      payload: jsonEncode(message.data),
    );
  }

  /// Exibe notificação local manualmente (como no exemplo de aula)
  static Future<void> showNotification(
    String notificationTitle,
    String notificationBody, {
    String? payload,
  }) async {
    if (!await _notificacoesAtivas()) {
      developer.log(
        'Push local silenciado pelas configurações do usuário.',
        name: 'FCM',
      );
      return;
    }

    final int idNotification = DateTime.now().millisecondsSinceEpoch.remainder(
      100000,
    );

    developer.log('Chamando notificação local...', name: 'FCM');
    return _localNotifications.show(
      idNotification,
      notificationTitle,
      notificationBody,
      _notificationDetails,
      payload: payload,
    );
  }

  static void _onDidReceiveNotificationResponse(NotificationResponse details) {
    developer.log(
      'Selecionado: ${details.actionId} | ${details.payload}',
      name: 'FCM',
    );

    if (details.actionId == '0') {
      developer.log('Usuário clicou em Ok', name: 'FCM');
    } else if (details.actionId == '1') {
      developer.log('Usuário clicou em Cancelar', name: 'FCM');
    } else {
      developer.log('Usuário tocou na notificação', name: 'FCM');
    }
  }

  /// Envia o token FCM para o backend salvar
  static Future<void> _enviarTokenParaBackend(
    String token,
    String apiBaseUrl,
  ) async {
    try {
      await http.post(
        Uri.parse('$apiBaseUrl/push/registrar-token'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'token': token, 'plataforma': 'android'}),
      );
    } catch (e) {
      developer.log('Erro ao registrar token FCM: $e', name: 'FCM');
    }
  }
}
