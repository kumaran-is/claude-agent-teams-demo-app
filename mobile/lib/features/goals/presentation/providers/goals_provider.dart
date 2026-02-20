import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../../../core/mock/mock_data.dart';
import '../../../../core/router/demo_mode_provider.dart';
import '../../data/goal_repository.dart';
import '../../domain/goal_model.dart';

part 'goals_provider.g.dart';

/// All goals for the current user.
@riverpod
class GoalsNotifier extends _$GoalsNotifier {
  @override
  Future<List<GoalModel>> build() {
    if (ref.watch(demoModeProvider)) return Future.value(MockData.goals);
    return ref.watch(goalRepositoryProvider).getGoals();
  }

  Future<void> createGoal({
    required String goalType,
    required double targetValue,
    required String unit,
    DateTime? targetDate,
  }) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      await ref
          .read(goalRepositoryProvider)
          .createGoal(
            goalType: goalType,
            targetValue: targetValue,
            unit: unit,
            targetDate: targetDate,
          );
      return ref.read(goalRepositoryProvider).getGoals();
    });
  }

  Future<void> deleteGoal(String goalId) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      await ref.read(goalRepositoryProvider).deleteGoal(goalId);
      return ref.read(goalRepositoryProvider).getGoals();
    });
  }
}
