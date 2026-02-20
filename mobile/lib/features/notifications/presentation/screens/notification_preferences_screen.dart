import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/notification_provider.dart';

class NotificationPreferencesScreen extends ConsumerWidget {
  const NotificationPreferencesScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final prefsAsync = ref.watch(notificationPreferencesNotifierProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Notification Settings')),
      body: prefsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, _) => Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(err.toString()),
              const SizedBox(height: 8),
              ElevatedButton(
                onPressed: () =>
                    ref.invalidate(notificationPreferencesNotifierProvider),
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
        data: (prefs) => ListView(
          padding: const EdgeInsets.all(16),
          children: [
            SwitchListTile(
              title: const Text('Reminders'),
              subtitle: const Text('Receive workout and nutrition reminders'),
              value: prefs.remindersEnabled,
              onChanged: (v) => ref
                  .read(notificationPreferencesNotifierProvider.notifier)
                  .savePreferences(
                    remindersEnabled: v,
                    reminderTime: prefs.reminderTime,
                  ),
            ),
            if (prefs.remindersEnabled) ...[
              const Divider(),
              ListTile(
                title: const Text('Reminder Time'),
                subtitle: Text(prefs.reminderTime ?? 'Not set'),
                trailing: const Icon(Icons.chevron_right),
                onTap: () => _pickTime(context, ref, prefs.reminderTime),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Future<void> _pickTime(
    BuildContext context,
    WidgetRef ref,
    String? current,
  ) async {
    final parts = current?.split(':');
    final initial = TimeOfDay(
      hour: int.tryParse(parts?.firstOrNull ?? '') ?? 8,
      minute: int.tryParse(parts?.lastOrNull ?? '') ?? 0,
    );

    final picked = await showTimePicker(context: context, initialTime: initial);
    if (picked == null) return;

    final timeStr =
        '${picked.hour.toString().padLeft(2, '0')}:${picked.minute.toString().padLeft(2, '0')}';

    final prefs = ref.read(notificationPreferencesNotifierProvider).valueOrNull;
    if (prefs == null) return;

    ref
        .read(notificationPreferencesNotifierProvider.notifier)
        .savePreferences(
          remindersEnabled: prefs.remindersEnabled,
          reminderTime: timeStr,
        );
  }
}
