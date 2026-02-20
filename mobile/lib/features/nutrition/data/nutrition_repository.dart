import 'package:dio/dio.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../../core/network/api_client.dart';
import '../domain/nutrition_log_model.dart';
import '../domain/nutrition_summary_model.dart';

part 'nutrition_repository.g.dart';

class NutritionException implements Exception {
  final String message;
  const NutritionException(this.message);

  @override
  String toString() => message;
}

class NutritionRepository {
  NutritionRepository(this._dio);

  final Dio _dio;

  Future<NutritionLogModel> logFood({
    required String mealName,
    required double calories,
    required double proteinG,
    required double carbsG,
    required double fatG,
  }) async {
    try {
      final response = await _dio.post<Map<String, dynamic>>(
        '/api/v1/nutrition/logs',
        data: {
          'mealName': mealName,
          'calories': calories,
          'proteinG': proteinG,
          'carbsG': carbsG,
          'fatG': fatG,
          'loggedAt': DateTime.now().toIso8601String(),
        },
      );
      return NutritionLogModel.fromJson(response.data!);
    } on DioException catch (e) {
      throw NutritionException(_mapDioError(e));
    }
  }

  Future<NutritionSummaryModel> getDailySummary(DateTime date) async {
    try {
      final dateStr =
          '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
      final response = await _dio.get<Map<String, dynamic>>(
        '/api/v1/nutrition/summary',
        queryParameters: {'date': dateStr},
      );
      return NutritionSummaryModel.fromJson(response.data!);
    } on DioException catch (e) {
      throw NutritionException(_mapDioError(e));
    }
  }

  Future<List<NutritionLogModel>> getLogs({DateTime? date}) async {
    try {
      final queryParameters = <String, dynamic>{};
      if (date != null) {
        queryParameters['date'] =
            '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
      }
      final response = await _dio.get<List<dynamic>>(
        '/api/v1/nutrition/logs',
        queryParameters: queryParameters,
      );
      return (response.data ?? [])
          .cast<Map<String, dynamic>>()
          .map(NutritionLogModel.fromJson)
          .toList();
    } on DioException catch (e) {
      throw NutritionException(_mapDioError(e));
    }
  }

  Future<void> deleteLog(String logId) async {
    try {
      await _dio.delete<void>('/api/v1/nutrition/logs/$logId');
    } on DioException catch (e) {
      throw NutritionException(_mapDioError(e));
    }
  }

  String _mapDioError(DioException e) {
    return switch (e.response?.statusCode) {
      400 => 'Invalid nutrition data',
      401 => 'Please sign in again',
      404 => 'Log entry not found',
      _ => 'Nutrition error: ${e.message}',
    };
  }
}

@riverpod
NutritionRepository nutritionRepository(NutritionRepositoryRef ref) {
  return NutritionRepository(ref.watch(apiClientProvider));
}
