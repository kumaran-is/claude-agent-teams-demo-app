import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../../../core/mock/mock_data.dart';
import '../../../../core/router/demo_mode_provider.dart';
import '../../data/workout_repository.dart';
import '../../domain/metrics_model.dart';
import '../../domain/workout_session_model.dart';

part 'workout_provider.g.dart';

/// Active workout session state.
@riverpod
class ActiveWorkoutNotifier extends _$ActiveWorkoutNotifier {
  @override
  FutureOr<WorkoutSessionModel?> build() => null;

  Future<void> startSession({String? name}) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(
      () => ref.read(workoutRepositoryProvider).startSession(name: name),
    );
  }

  Future<void> logSet({
    required String exerciseId,
    required int reps,
    required double weightKg,
  }) async {
    final session = state.valueOrNull;
    if (session == null) return;

    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      final newSet = await ref
          .read(workoutRepositoryProvider)
          .logSet(
            session.id,
            exerciseId: exerciseId,
            reps: reps,
            weightKg: weightKg,
          );
      return session.copyWith(sets: [...session.sets, newSet]);
    });
  }

  Future<void> completeSession() async {
    final session = state.valueOrNull;
    if (session == null) return;
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(
      () => ref.read(workoutRepositoryProvider).completeSession(session.id),
    );
  }

  void clear() => state = const AsyncValue.data(null);
}

/// Workout session history.
@riverpod
Future<List<WorkoutSessionModel>> workoutHistory(WorkoutHistoryRef ref) {
  if (ref.watch(demoModeProvider)) return Future.value(MockData.workoutHistory);
  return ref.watch(workoutRepositoryProvider).getSessions();
}

/// Workout metrics (weekly stats, streak, PRs).
@riverpod
Future<MetricsModel> workoutMetrics(WorkoutMetricsRef ref) {
  if (ref.watch(demoModeProvider)) return Future.value(MockData.metrics);
  return ref.watch(workoutRepositoryProvider).getMetrics();
}

/// Single session details.
@riverpod
Future<WorkoutSessionModel> workoutSession(
  WorkoutSessionRef ref,
  String sessionId,
) {
  if (ref.watch(demoModeProvider)) {
    return Future.value(
      MockData.workoutHistory.firstWhere(
        (s) => s.id == sessionId,
        orElse: () => MockData.workoutHistory.first,
      ),
    );
  }
  return ref.watch(workoutRepositoryProvider).getSession(sessionId);
}
