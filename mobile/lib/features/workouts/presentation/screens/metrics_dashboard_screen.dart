import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/workout_provider.dart';

class MetricsDashboardScreen extends ConsumerWidget {
  const MetricsDashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final metricsAsync = ref.watch(workoutMetricsProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('My Progress')),
      body: metricsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, _) => Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(err.toString()),
              const SizedBox(height: 8),
              ElevatedButton(
                onPressed: () => ref.invalidate(workoutMetricsProvider),
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
        data: (metrics) => RefreshIndicator(
          onRefresh: () async => ref.invalidate(workoutMetricsProvider),
          child: ListView(
            padding: const EdgeInsets.all(16),
            children: [
              _SectionTitle('This Week'),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(
                    child: _StatCard(
                      label: 'Sessions',
                      value: '${metrics.weeklySessions}',
                      icon: Icons.fitness_center,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: _StatCard(
                      label: 'Volume',
                      value: '${metrics.weeklyVolumeKg.toStringAsFixed(1)} kg',
                      icon: Icons.monitor_weight_outlined,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              _StatCard(
                label: 'Current Streak',
                value:
                    '${metrics.currentStreak} day${metrics.currentStreak == 1 ? '' : 's'}',
                icon: Icons.local_fire_department,
                fullWidth: true,
              ),
              const SizedBox(height: 24),
              _SectionTitle('Personal Records'),
              const SizedBox(height: 8),
              if (metrics.personalRecords.isEmpty)
                const Center(
                  child: Padding(
                    padding: EdgeInsets.symmetric(vertical: 16),
                    child: Text('No PRs yet — keep lifting!'),
                  ),
                )
              else
                ...metrics.personalRecords.map(
                  (pr) => Card(
                    margin: const EdgeInsets.only(bottom: 8),
                    child: ListTile(
                      leading: const CircleAvatar(
                        child: Icon(Icons.emoji_events, size: 20),
                      ),
                      title: Text(pr.exerciseName),
                      subtitle: Text('${pr.repsAtMaxWeight} reps'),
                      trailing: Text(
                        '${pr.maxWeightKg} kg',
                        style: Theme.of(context).textTheme.titleMedium
                            ?.copyWith(
                              fontWeight: FontWeight.bold,
                              color: Theme.of(context).colorScheme.primary,
                            ),
                      ),
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SectionTitle extends StatelessWidget {
  final String text;
  const _SectionTitle(this.text);

  @override
  Widget build(BuildContext context) {
    return Text(text, style: Theme.of(context).textTheme.titleLarge);
  }
}

class _StatCard extends StatelessWidget {
  final String label;
  final String value;
  final IconData icon;
  final bool fullWidth;

  const _StatCard({
    required this.label,
    required this.value,
    required this.icon,
    this.fullWidth = false,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Icon(icon, size: 32, color: Theme.of(context).colorScheme.primary),
            const SizedBox(width: 12),
            Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(label, style: Theme.of(context).textTheme.bodySmall),
                Text(
                  value,
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
