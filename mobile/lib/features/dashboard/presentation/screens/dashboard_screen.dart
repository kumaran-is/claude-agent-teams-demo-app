import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../auth/presentation/providers/auth_provider.dart';
import '../../../nutrition/presentation/providers/nutrition_provider.dart';
import '../../../workouts/presentation/providers/workout_provider.dart';

class DashboardScreen extends ConsumerWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final metricsAsync = ref.watch(workoutMetricsProvider);
    final today = DateTime(
      DateTime.now().year,
      DateTime.now().month,
      DateTime.now().day,
    );
    final nutritionAsync = ref.watch(nutritionSummaryProvider(today));

    return Scaffold(
      appBar: AppBar(
        title: const Text('Dashboard'),
        actions: [
          IconButton(
            icon: const Icon(Icons.notifications_outlined),
            tooltip: 'Notification settings',
            onPressed: () => context.push('/settings/notifications'),
          ),
          IconButton(
            icon: const Icon(Icons.logout),
            tooltip: 'Sign out',
            onPressed: () async {
              await ref.read(authNotifierProvider.notifier).signOut();
              if (context.mounted) context.go('/login');
            },
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          ref.invalidate(workoutMetricsProvider);
          ref.invalidate(nutritionSummaryProvider(today));
        },
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // ── Streak card ─────────────────────────────────────────────────
            metricsAsync.when(
              loading: () => const _SkeletonCard(height: 80),
              error: (_, __) => const SizedBox.shrink(),
              data: (metrics) => Card(
                color: Theme.of(context).colorScheme.primaryContainer,
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Row(
                    children: [
                      const Icon(
                        Icons.local_fire_department,
                        size: 40,
                        color: Colors.deepOrange,
                      ),
                      const SizedBox(width: 12),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            '${metrics.currentStreak} day streak',
                            style: Theme.of(context).textTheme.headlineSmall
                                ?.copyWith(fontWeight: FontWeight.bold),
                          ),
                          Text(
                            '${metrics.weeklySessions} sessions this week · '
                            '${metrics.weeklyVolumeKg.toStringAsFixed(0)} kg',
                            style: Theme.of(context).textTheme.bodySmall,
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),
            const SizedBox(height: 12),

            // ── Daily macro summary ──────────────────────────────────────────
            Text(
              'Today\'s Nutrition',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            nutritionAsync.when(
              loading: () => const _SkeletonCard(height: 80),
              error: (_, __) => const SizedBox.shrink(),
              data: (summary) => Card(
                child: Padding(
                  padding: const EdgeInsets.symmetric(
                    horizontal: 16,
                    vertical: 12,
                  ),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      _MacroChip(
                        label: 'Cal',
                        value: summary.totalCalories.toStringAsFixed(0),
                        color: Theme.of(context).colorScheme.primary,
                      ),
                      _MacroChip(
                        label: 'Protein',
                        value: '${summary.totalProteinG.toStringAsFixed(0)}g',
                        color: Colors.blue,
                      ),
                      _MacroChip(
                        label: 'Carbs',
                        value: '${summary.totalCarbsG.toStringAsFixed(0)}g',
                        color: Colors.orange,
                      ),
                      _MacroChip(
                        label: 'Fat',
                        value: '${summary.totalFatG.toStringAsFixed(0)}g',
                        color: Colors.red,
                      ),
                    ],
                  ),
                ),
              ),
            ),
            const SizedBox(height: 24),

            // ── Quick-start button ────────────────────────────────────────────
            FilledButton.icon(
              onPressed: () => context.push('/workouts/active'),
              icon: const Icon(Icons.play_arrow),
              label: const Text('Start Workout'),
              style: FilledButton.styleFrom(
                minimumSize: const Size.fromHeight(52),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _MacroChip extends StatelessWidget {
  final String label;
  final String value;
  final Color color;

  const _MacroChip({
    required this.label,
    required this.value,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Text(
          value,
          style: Theme.of(context).textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.bold,
            color: color,
          ),
        ),
        Text(label, style: Theme.of(context).textTheme.bodySmall),
      ],
    );
  }
}

class _SkeletonCard extends StatelessWidget {
  final double height;
  const _SkeletonCard({required this.height});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      height: height,
      child: Card(
        child: Center(
          child: SizedBox(
            width: 24,
            height: 24,
            child: CircularProgressIndicator(strokeWidth: 2),
          ),
        ),
      ),
    );
  }
}
