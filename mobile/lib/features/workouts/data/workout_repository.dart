import 'package:dio/dio.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../../core/network/api_client.dart';
import '../domain/metrics_model.dart';
import '../domain/workout_session_model.dart';
import '../domain/workout_set_model.dart';

part 'workout_repository.g.dart';

class WorkoutRepositoryException implements Exception {
  final String message;
  const WorkoutRepositoryException(this.message);

  @override
  String toString() => message;
}

abstract class WorkoutRepository {
  Future<WorkoutSessionModel> startSession({String? name});
  Future<WorkoutSessionModel> completeSession(String sessionId);
  Future<List<WorkoutSessionModel>> getSessions({int page = 0, int size = 20});
  Future<WorkoutSessionModel> getSession(String sessionId);
  Future<WorkoutSetModel> logSet(
    String sessionId, {
    required String exerciseId,
    required int reps,
    required double weightKg,
  });
  Future<MetricsModel> getMetrics();
}

class WorkoutRepositoryImpl implements WorkoutRepository {
  final Dio _dio;

  const WorkoutRepositoryImpl(this._dio);

  @override
  Future<WorkoutSessionModel> startSession({String? name}) async {
    try {
      final response = await _dio.post(
        '/api/v1/workouts/sessions',
        data: {if (name != null) 'name': name},
      );
      return WorkoutSessionModel.fromJson(
        response.data as Map<String, dynamic>,
      );
    } on DioException catch (e) {
      throw WorkoutRepositoryException(_mapDioError(e));
    }
  }

  @override
  Future<WorkoutSessionModel> completeSession(String sessionId) async {
    try {
      final response = await _dio.patch(
        '/api/v1/workouts/sessions/$sessionId/complete',
      );
      return WorkoutSessionModel.fromJson(
        response.data as Map<String, dynamic>,
      );
    } on DioException catch (e) {
      throw WorkoutRepositoryException(_mapDioError(e));
    }
  }

  @override
  Future<List<WorkoutSessionModel>> getSessions({
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await _dio.get(
        '/api/v1/workouts/sessions',
        queryParameters: {'page': page, 'size': size},
      );
      final data = response.data;
      final list = data is Map ? data['content'] as List : data as List;
      return list
          .map((e) => WorkoutSessionModel.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw WorkoutRepositoryException(_mapDioError(e));
    }
  }

  @override
  Future<WorkoutSessionModel> getSession(String sessionId) async {
    try {
      final response = await _dio.get('/api/v1/workouts/sessions/$sessionId');
      return WorkoutSessionModel.fromJson(
        response.data as Map<String, dynamic>,
      );
    } on DioException catch (e) {
      throw WorkoutRepositoryException(_mapDioError(e));
    }
  }

  @override
  Future<WorkoutSetModel> logSet(
    String sessionId, {
    required String exerciseId,
    required int reps,
    required double weightKg,
  }) async {
    try {
      final response = await _dio.post(
        '/api/v1/workouts/sessions/$sessionId/sets',
        data: {'exerciseId': exerciseId, 'reps': reps, 'weightKg': weightKg},
      );
      return WorkoutSetModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw WorkoutRepositoryException(_mapDioError(e));
    }
  }

  @override
  Future<MetricsModel> getMetrics() async {
    try {
      final response = await _dio.get('/api/v1/workouts/metrics');
      return MetricsModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw WorkoutRepositoryException(_mapDioError(e));
    }
  }

  String _mapDioError(DioException e) {
    final status = e.response?.statusCode;
    if (status == 403)
      return 'You do not have permission to access this session.';
    if (status == 404) return 'Workout session not found.';
    if (status != null && status >= 500)
      return 'Server error. Please try again.';
    if (e.type == DioExceptionType.connectionError) {
      return 'Cannot reach server. Check your internet connection.';
    }
    return 'Unexpected error. Please try again.';
  }
}

@riverpod
WorkoutRepository workoutRepository(WorkoutRepositoryRef ref) {
  return WorkoutRepositoryImpl(ref.read(apiClientProvider));
}
