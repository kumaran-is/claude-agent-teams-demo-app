import 'package:dio/dio.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../../core/network/api_client.dart';
import '../domain/notification_preference_model.dart';

part 'notification_repository.g.dart';

class NotificationException implements Exception {
  final String message;
  const NotificationException(this.message);

  @override
  String toString() => message;
}

class NotificationRepository {
  NotificationRepository(this._dio);

  final Dio _dio;

  Future<NotificationPreferenceModel> getPreferences() async {
    try {
      final response = await _dio.get<Map<String, dynamic>>(
        '/api/v1/notifications/preferences',
      );
      return NotificationPreferenceModel.fromJson(response.data!);
    } on DioException catch (e) {
      throw NotificationException(_mapDioError(e));
    }
  }

  Future<NotificationPreferenceModel> updatePreferences({
    required bool remindersEnabled,
    String? reminderTime,
  }) async {
    try {
      final response = await _dio.put<Map<String, dynamic>>(
        '/api/v1/notifications/preferences',
        data: {
          'remindersEnabled': remindersEnabled,
          if (reminderTime != null) 'reminderTime': reminderTime,
        },
      );
      return NotificationPreferenceModel.fromJson(response.data!);
    } on DioException catch (e) {
      throw NotificationException(_mapDioError(e));
    }
  }

  Future<void> registerToken(String token) async {
    try {
      await _dio.post<void>(
        '/api/v1/notifications/token',
        data: {'token': token},
      );
    } on DioException catch (e) {
      throw NotificationException(_mapDioError(e));
    }
  }

  String _mapDioError(DioException e) {
    return switch (e.response?.statusCode) {
      401 => 'Please sign in again',
      404 => 'Preferences not found',
      _ => 'Notification error: ${e.message}',
    };
  }
}

@riverpod
NotificationRepository notificationRepository(NotificationRepositoryRef ref) {
  return NotificationRepository(ref.watch(apiClientProvider));
}
