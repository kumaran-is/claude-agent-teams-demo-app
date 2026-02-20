import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../../../core/mock/mock_data.dart';
import '../../../../core/router/demo_mode_provider.dart';
import '../../data/nutrition_repository.dart';
import '../../domain/nutrition_log_model.dart';
import '../../domain/nutrition_summary_model.dart';

part 'nutrition_provider.g.dart';

/// Daily nutrition summary for [date].
@riverpod
Future<NutritionSummaryModel> nutritionSummary(
  NutritionSummaryRef ref,
  DateTime date,
) {
  if (ref.watch(demoModeProvider))
    return Future.value(MockData.nutritionSummary);
  return ref.watch(nutritionRepositoryProvider).getDailySummary(date);
}

/// Logs for today (or a specific date).
@riverpod
Future<List<NutritionLogModel>> nutritionLogs(
  NutritionLogsRef ref, {
  DateTime? date,
}) {
  if (ref.watch(demoModeProvider)) return Future.value(MockData.nutritionLogs);
  return ref.watch(nutritionRepositoryProvider).getLogs(date: date);
}

/// Manages logging a new food entry (action-only notifier).
@riverpod
class NutritionLogNotifier extends _$NutritionLogNotifier {
  @override
  FutureOr<void> build() {}

  Future<void> logFood({
    required String mealName,
    required double calories,
    required double proteinG,
    required double carbsG,
    required double fatG,
  }) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      await ref
          .read(nutritionRepositoryProvider)
          .logFood(
            mealName: mealName,
            calories: calories,
            proteinG: proteinG,
            carbsG: carbsG,
            fatG: fatG,
          );
      ref.invalidate(nutritionLogsProvider);
      ref.invalidate(nutritionSummaryProvider);
    });
  }

  Future<void> deleteLog(String logId) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      await ref.read(nutritionRepositoryProvider).deleteLog(logId);
      ref.invalidate(nutritionLogsProvider);
      ref.invalidate(nutritionSummaryProvider);
    });
  }
}
