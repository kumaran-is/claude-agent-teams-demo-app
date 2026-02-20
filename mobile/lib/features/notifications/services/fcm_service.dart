import 'dart:developer';

import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/foundation.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../data/notification_repository.dart';

part 'fcm_service.g.dart';

/// Background message handler — must be a top-level function.
@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  log('FCM background message: ${message.messageId}', name: 'FCM');
}

class FcmService {
  FcmService(this._repo);

  final NotificationRepository _repo;

  /// Initialises FCM: requests permission, registers token, and sets up handlers.
  Future<void> initialize() async {
    FirebaseMessaging.onBackgroundMessage(_firebaseMessagingBackgroundHandler);

    final settings = await FirebaseMessaging.instance.requestPermission(
      alert: true,
      badge: true,
      sound: true,
    );

    if (settings.authorizationStatus == AuthorizationStatus.authorized ||
        settings.authorizationStatus == AuthorizationStatus.provisional) {
      await _registerToken();
    }

    FirebaseMessaging.onMessage.listen(_handleForegroundMessage);
    FirebaseMessaging.onMessageOpenedApp.listen(_handleMessageOpenedApp);
  }

  Future<void> _registerToken() async {
    try {
      final token = await FirebaseMessaging.instance.getToken();
      if (token != null) {
        await _repo.registerToken(token);
        log('FCM token registered', name: 'FCM');
      }

      FirebaseMessaging.instance.onTokenRefresh.listen((newToken) async {
        try {
          await _repo.registerToken(newToken);
          log('FCM token refreshed', name: 'FCM');
        } catch (e) {
          log('FCM token refresh failed: $e', name: 'FCM');
        }
      });
    } catch (e) {
      // Non-fatal: token registration failing does not block the app.
      log('FCM token registration failed: $e', name: 'FCM');
    }
  }

  void _handleForegroundMessage(RemoteMessage message) {
    log('FCM foreground: ${message.notification?.title}', name: 'FCM');
    // Foreground notifications are handled via the OS on iOS.
    // On Android, a local notification library (e.g. flutter_local_notifications)
    // would be used here. Kept as a hook for future integration.
  }

  void _handleMessageOpenedApp(RemoteMessage message) {
    log('FCM app opened from notification: ${message.data}', name: 'FCM');
    // Deep-link routing based on message.data['route'] would go here.
  }
}

@riverpod
FcmService fcmService(FcmServiceRef ref) {
  return FcmService(ref.watch(notificationRepositoryProvider));
}

/// Call once at app startup (after Firebase.initializeApp).
@riverpod
Future<void> initFcm(InitFcmRef ref) async {
  // Only run on real devices — skip on web or when running tests.
  if (kIsWeb) return;
  await ref.watch(fcmServiceProvider).initialize();
}
