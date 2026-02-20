import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../providers/nutrition_provider.dart';

class NutritionSummaryScreen extends ConsumerWidget {
  const NutritionSummaryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final today = DateTime(
      DateTime.now().year,
      DateTime.now().month,
      DateTime.now().day,
    );
    final summaryAsync = ref.watch(nutritionSummaryProvider(today));
    final logsAsync = ref.watch(nutritionLogsProvider(date: today));

    return Scaffold(
      appBar: AppBar(title: const Text('Nutrition')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/nutrition/log'),
        icon: const Icon(Icons.add),
        label: const Text('Log Food'),
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          ref.invalidate(nutritionSummaryProvider(today));
          ref.invalidate(nutritionLogsProvider);
        },
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            Text(
              'Today\'s Summary',
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 12),
            summaryAsync.when(
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (err, _) => Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(err.toString()),
                    const SizedBox(height: 8),
                    ElevatedButton(
                      onPressed: () =>
                          ref.invalidate(nutritionSummaryProvider(today)),
                      child: const Text('Retry'),
                    ),
                  ],
                ),
              ),
              data: (summary) => Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    children: [
                      _MacroRow(
                        label: 'Calories',
                        value: summary.totalCalories.toStringAsFixed(0),
                        unit: 'kcal',
                        color: Theme.of(context).colorScheme.primary,
                      ),
                      const Divider(),
                      _MacroRow(
                        label: 'Protein',
                        value: summary.totalProteinG.toStringAsFixed(1),
                        unit: 'g',
                        color: Colors.blue,
                      ),
                      _MacroRow(
                        label: 'Carbs',
                        value: summary.totalCarbsG.toStringAsFixed(1),
                        unit: 'g',
                        color: Colors.orange,
                      ),
                      _MacroRow(
                        label: 'Fat',
                        value: summary.totalFatG.toStringAsFixed(1),
                        unit: 'g',
                        color: Colors.red,
                      ),
                    ],
                  ),
                ),
              ),
            ),
            const SizedBox(height: 16),
            Text('Food Log', style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 8),
            logsAsync.when(
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (err, _) => Center(child: Text(err.toString())),
              data: (logs) => logs.isEmpty
                  ? const Center(
                      child: Padding(
                        padding: EdgeInsets.symmetric(vertical: 16),
                        child: Text('No food logged today. Tap + to add!'),
                      ),
                    )
                  : Column(
                      children: logs
                          .map(
                            (log) => Card(
                              margin: const EdgeInsets.only(bottom: 8),
                              child: ListTile(
                                title: Text(log.mealName),
                                subtitle: Text(
                                  log.loggedAt.hour.toString().padLeft(2, '0') +
                                      ':' +
                                      log.loggedAt.minute.toString().padLeft(
                                        2,
                                        '0',
                                      ),
                                ),
                                trailing: Text(
                                  '${log.calories.toStringAsFixed(0)} kcal',
                                  style: Theme.of(context).textTheme.bodyMedium
                                      ?.copyWith(fontWeight: FontWeight.w600),
                                ),
                              ),
                            ),
                          )
                          .toList(),
                    ),
            ),
            // Space for FAB
            const SizedBox(height: 80),
          ],
        ),
      ),
    );
  }
}

class _MacroRow extends StatelessWidget {
  final String label;
  final String value;
  final String unit;
  final Color color;

  const _MacroRow({
    required this.label,
    required this.value,
    required this.unit,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Container(
            width: 4,
            height: 20,
            decoration: BoxDecoration(
              color: color,
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          const SizedBox(width: 8),
          Expanded(child: Text(label)),
          Text(
            '$value $unit',
            style: const TextStyle(fontWeight: FontWeight.w600),
          ),
        ],
      ),
    );
  }
}
