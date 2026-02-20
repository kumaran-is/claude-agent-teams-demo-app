import 'package:dio/dio.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../../core/network/api_client.dart';
import '../domain/goal_model.dart';

part 'goal_repository.g.dart';

class GoalException implements Exception {
  final String message;
  const GoalException(this.message);

  @override
  String toString() => message;
}

class GoalRepository {
  GoalRepository(this._dio);

  final Dio _dio;

  Future<List<GoalModel>> getGoals() async {
    try {
      final response = await _dio.get<List<dynamic>>('/api/v1/goals');
      return (response.data ?? [])
          .cast<Map<String, dynamic>>()
          .map(GoalModel.fromJson)
          .toList();
    } on DioException catch (e) {
      throw GoalException(_mapDioError(e));
    }
  }

  Future<GoalModel> createGoal({
    required String goalType,
    required double targetValue,
    required String unit,
    DateTime? targetDate,
  }) async {
    try {
      final response = await _dio.post<Map<String, dynamic>>(
        '/api/v1/goals',
        data: {
          'goalType': goalType,
          'targetValue': targetValue,
          'unit': unit,
          if (targetDate != null) 'targetDate': targetDate.toIso8601String(),
        },
      );
      return GoalModel.fromJson(response.data!);
    } on DioException catch (e) {
      throw GoalException(_mapDioError(e));
    }
  }

  Future<void> deleteGoal(String goalId) async {
    try {
      await _dio.delete<void>('/api/v1/goals/$goalId');
    } on DioException catch (e) {
      throw GoalException(_mapDioError(e));
    }
  }

  String _mapDioError(DioException e) {
    return switch (e.response?.statusCode) {
      400 => 'Invalid goal data',
      401 => 'Please sign in again',
      404 => 'Goal not found',
      _ => 'Goal error: ${e.message}',
    };
  }
}

@riverpod
GoalRepository goalRepository(GoalRepositoryRef ref) {
  return GoalRepository(ref.watch(apiClientProvider));
}
