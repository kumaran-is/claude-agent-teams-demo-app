import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../../../core/mock/mock_data.dart';
import '../../../../core/router/demo_mode_provider.dart';
import '../../data/exercise_repository.dart';
import '../../domain/exercise_model.dart';
import '../../domain/muscle_group_model.dart';

part 'exercises_provider.g.dart';

@riverpod
Future<List<MuscleGroupModel>> muscleGroups(MuscleGroupsRef ref) {
  if (ref.watch(demoModeProvider)) return Future.value(MockData.muscleGroups);
  return ref.watch(exerciseRepositoryProvider).getMuscleGroups();
}

@riverpod
class ExercisesNotifier extends _$ExercisesNotifier {
  String _search = '';
  String? _muscleGroupId;

  @override
  Future<List<ExerciseModel>> build() {
    if (ref.watch(demoModeProvider)) {
      return Future.value(_applyFilters(MockData.exercises));
    }
    return ref
        .watch(exerciseRepositoryProvider)
        .getExercises(search: _search, muscleGroupId: _muscleGroupId);
  }

  List<ExerciseModel> _applyFilters(List<ExerciseModel> all) {
    return all.where((e) {
      final matchesSearch =
          _search.isEmpty ||
          e.name.toLowerCase().contains(_search.toLowerCase());
      final matchesMuscle =
          _muscleGroupId == null || e.muscleGroupId == _muscleGroupId;
      return matchesSearch && matchesMuscle;
    }).toList();
  }

  Future<void> search(String query) async {
    _search = query;
    if (ref.read(demoModeProvider)) {
      state = AsyncValue.data(_applyFilters(MockData.exercises));
      return;
    }
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(
      () => ref
          .read(exerciseRepositoryProvider)
          .getExercises(search: _search, muscleGroupId: _muscleGroupId),
    );
  }

  Future<void> filterByMuscleGroup(String? muscleGroupId) async {
    _muscleGroupId = muscleGroupId;
    if (ref.read(demoModeProvider)) {
      state = AsyncValue.data(_applyFilters(MockData.exercises));
      return;
    }
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(
      () => ref
          .read(exerciseRepositoryProvider)
          .getExercises(search: _search, muscleGroupId: _muscleGroupId),
    );
  }
}

@riverpod
Future<ExerciseModel> exerciseDetail(ExerciseDetailRef ref, String id) {
  if (ref.watch(demoModeProvider)) {
    return Future.value(
      MockData.exercises.firstWhere(
        (e) => e.id == id,
        orElse: () => MockData.exercises.first,
      ),
    );
  }
  return ref.watch(exerciseRepositoryProvider).getExercise(id);
}
