import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../exercises/presentation/providers/exercises_provider.dart';
import '../providers/workout_provider.dart';

class ActiveWorkoutScreen extends ConsumerStatefulWidget {
  const ActiveWorkoutScreen({super.key});

  @override
  ConsumerState<ActiveWorkoutScreen> createState() =>
      _ActiveWorkoutScreenState();
}

class _ActiveWorkoutScreenState extends ConsumerState<ActiveWorkoutScreen> {
  @override
  void initState() {
    super.initState();
    // Start a new session when this screen opens.
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final sessionState = ref.read(activeWorkoutNotifierProvider);
      if (sessionState.valueOrNull == null && !sessionState.isLoading) {
        ref.read(activeWorkoutNotifierProvider.notifier).startSession();
      }
    });
  }

  void _showAddSetSheet(BuildContext context, String sessionId) {
    showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      builder: (_) => _AddSetSheet(sessionId: sessionId),
    );
  }

  @override
  Widget build(BuildContext context) {
    ref.listen<AsyncValue<Object?>>(activeWorkoutNotifierProvider, (_, next) {
      next.whenOrNull(
        error: (err, _) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(err.toString()),
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
          );
        },
      );
    });

    final workoutAsync = ref.watch(activeWorkoutNotifierProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Active Workout'),
        actions: [
          TextButton(
            onPressed: workoutAsync.isLoading
                ? null
                : () async {
                    await ref
                        .read(activeWorkoutNotifierProvider.notifier)
                        .completeSession();
                    if (context.mounted) {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('Workout completed!')),
                      );
                      context.go('/workouts/history');
                    }
                  },
            child: const Text('Finish'),
          ),
        ],
      ),
      body: workoutAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, _) => Center(child: Text(err.toString())),
        data: (session) {
          if (session == null) {
            return const Center(child: CircularProgressIndicator());
          }
          return session.sets.isEmpty
              ? const Center(
                  child: Text(
                    'No sets logged yet.\nTap + to add your first set.',
                    textAlign: TextAlign.center,
                  ),
                )
              : ListView.builder(
                  padding: const EdgeInsets.all(16),
                  itemCount: session.sets.length,
                  itemBuilder: (context, i) {
                    final s = session.sets[i];
                    return Card(
                      child: ListTile(
                        leading: CircleAvatar(child: Text('${s.setNumber}')),
                        title: Text(s.exerciseName ?? s.exerciseId),
                        subtitle: Text('${s.reps} reps × ${s.weightKg} kg'),
                      ),
                    );
                  },
                );
        },
      ),
      floatingActionButton: workoutAsync.whenOrNull(
        data: (session) => session == null
            ? null
            : FloatingActionButton(
                onPressed: () => _showAddSetSheet(context, session.id),
                child: const Icon(Icons.add),
              ),
      ),
    );
  }
}

class _AddSetSheet extends ConsumerStatefulWidget {
  final String sessionId;
  const _AddSetSheet({required this.sessionId});

  @override
  ConsumerState<_AddSetSheet> createState() => _AddSetSheetState();
}

class _AddSetSheetState extends ConsumerState<_AddSetSheet> {
  String? _selectedExerciseId;
  String? _selectedExerciseName;
  final _repsController = TextEditingController(text: '10');
  final _weightController = TextEditingController(text: '0');

  @override
  void dispose() {
    _repsController.dispose();
    _weightController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final reps = int.tryParse(_repsController.text);
    final weight = double.tryParse(_weightController.text);
    if (_selectedExerciseId == null || reps == null || weight == null) return;

    await ref
        .read(activeWorkoutNotifierProvider.notifier)
        .logSet(exerciseId: _selectedExerciseId!, reps: reps, weightKg: weight);
    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    final exercisesAsync = ref.watch(exercisesNotifierProvider);

    return Padding(
      padding: EdgeInsets.only(
        bottom: MediaQuery.of(context).viewInsets.bottom + 16,
        left: 16,
        right: 16,
        top: 16,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text('Log Set', style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 16),
          exercisesAsync.when(
            loading: () => const CircularProgressIndicator(),
            error: (err, _) => Text(err.toString()),
            data: (exercises) => DropdownButtonFormField<String>(
              decoration: const InputDecoration(labelText: 'Exercise'),
              value: _selectedExerciseId,
              items: exercises
                  .map(
                    (e) => DropdownMenuItem<String>(
                      value: e.id,
                      child: Text(e.name),
                    ),
                  )
                  .toList(),
              onChanged: (id) {
                setState(() {
                  _selectedExerciseId = id;
                  _selectedExerciseName = exercises
                      .firstWhere((e) => e.id == id)
                      .name;
                });
              },
            ),
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              Expanded(
                child: TextFormField(
                  controller: _repsController,
                  decoration: const InputDecoration(labelText: 'Reps'),
                  keyboardType: TextInputType.number,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: TextFormField(
                  controller: _weightController,
                  decoration: const InputDecoration(labelText: 'Weight (kg)'),
                  keyboardType: const TextInputType.numberWithOptions(
                    decimal: true,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: _selectedExerciseId == null ? null : _submit,
            child: const Text('Log Set'),
          ),
        ],
      ),
    );
  }
}
