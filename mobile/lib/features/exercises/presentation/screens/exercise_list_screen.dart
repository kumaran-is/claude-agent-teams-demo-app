import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../providers/exercises_provider.dart';

class ExerciseListScreen extends ConsumerStatefulWidget {
  const ExerciseListScreen({super.key});

  @override
  ConsumerState<ExerciseListScreen> createState() => _ExerciseListScreenState();
}

class _ExerciseListScreenState extends ConsumerState<ExerciseListScreen> {
  final _searchController = TextEditingController();

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final exercisesAsync = ref.watch(exercisesNotifierProvider);
    final muscleGroupsAsync = ref.watch(muscleGroupsProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Exercises')),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 4),
            child: TextField(
              controller: _searchController,
              decoration: const InputDecoration(
                hintText: 'Search exercises…',
                prefixIcon: Icon(Icons.search),
                isDense: true,
              ),
              onChanged: (q) =>
                  ref.read(exercisesNotifierProvider.notifier).search(q),
            ),
          ),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
            child: muscleGroupsAsync.when(
              loading: () => const SizedBox.shrink(),
              error: (_, __) => const SizedBox.shrink(),
              data: (groups) => DropdownButtonFormField<String>(
                decoration: const InputDecoration(
                  hintText: 'All muscle groups',
                  isDense: true,
                ),
                value: null,
                items: [
                  const DropdownMenuItem<String>(
                    value: null,
                    child: Text('All muscle groups'),
                  ),
                  ...groups.map(
                    (g) => DropdownMenuItem<String>(
                      value: g.id,
                      child: Text(g.name),
                    ),
                  ),
                ],
                onChanged: (id) => ref
                    .read(exercisesNotifierProvider.notifier)
                    .filterByMuscleGroup(id),
              ),
            ),
          ),
          Expanded(
            child: exercisesAsync.when(
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (err, _) => Center(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(err.toString()),
                    const SizedBox(height: 8),
                    ElevatedButton(
                      onPressed: () =>
                          ref.invalidate(exercisesNotifierProvider),
                      child: const Text('Retry'),
                    ),
                  ],
                ),
              ),
              data: (exercises) => exercises.isEmpty
                  ? const Center(child: Text('No exercises found.'))
                  : ListView.separated(
                      itemCount: exercises.length,
                      separatorBuilder: (_, __) => const Divider(height: 1),
                      itemBuilder: (context, index) {
                        final ex = exercises[index];
                        return ListTile(
                          title: Text(ex.name),
                          subtitle: ex.muscleGroupName != null
                              ? Text(ex.muscleGroupName!)
                              : null,
                          trailing: const Icon(Icons.chevron_right),
                          onTap: () => context.push('/exercises/${ex.id}'),
                        );
                      },
                    ),
            ),
          ),
        ],
      ),
    );
  }
}
