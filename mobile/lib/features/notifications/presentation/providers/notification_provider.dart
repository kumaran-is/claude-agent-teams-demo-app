import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../data/notification_repository.dart';
import '../../domain/notification_preference_model.dart';

part 'notification_provider.g.dart';

@riverpod
class NotificationPreferencesNotifier
    extends _$NotificationPreferencesNotifier {
  @override
  Future<NotificationPreferenceModel> build() {
    return ref.watch(notificationRepositoryProvider).getPreferences();
  }

  Future<void> savePreferences({
    required bool remindersEnabled,
    String? reminderTime,
  }) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(
      () => ref
          .read(notificationRepositoryProvider)
          .updatePreferences(
            remindersEnabled: remindersEnabled,
            reminderTime: reminderTime,
          ),
    );
  }
}
