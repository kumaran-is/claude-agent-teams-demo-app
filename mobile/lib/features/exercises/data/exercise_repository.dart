import 'package:dio/dio.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../../core/network/api_client.dart';
import '../domain/exercise_model.dart';
import '../domain/muscle_group_model.dart';

part 'exercise_repository.g.dart';

class ExerciseRepositoryException implements Exception {
  final String message;
  const ExerciseRepositoryException(this.message);

  @override
  String toString() => message;
}

abstract class ExerciseRepository {
  Future<List<ExerciseModel>> getExercises({
    String? search,
    String? muscleGroupId,
    int page = 0,
    int size = 20,
  });
  Future<ExerciseModel> getExercise(String id);
  Future<List<MuscleGroupModel>> getMuscleGroups();
}

class ExerciseRepositoryImpl implements ExerciseRepository {
  final Dio _dio;

  const ExerciseRepositoryImpl(this._dio);

  @override
  Future<List<ExerciseModel>> getExercises({
    String? search,
    String? muscleGroupId,
    int page = 0,
    int size = 20,
  }) async {
    try {
      final response = await _dio.get(
        '/api/v1/exercises',
        queryParameters: {
          if (search != null && search.isNotEmpty) 'search': search,
          if (muscleGroupId != null) 'muscleGroup': muscleGroupId,
          'page': page,
          'size': size,
        },
      );
      final data = response.data;
      // Handle both paginated ({content: [...]} ) and plain list responses.
      final list = data is Map ? data['content'] as List : data as List;
      return list
          .map((e) => ExerciseModel.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw ExerciseRepositoryException(_mapDioError(e));
    }
  }

  @override
  Future<ExerciseModel> getExercise(String id) async {
    try {
      final response = await _dio.get('/api/v1/exercises/$id');
      return ExerciseModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw ExerciseRepositoryException(_mapDioError(e));
    }
  }

  @override
  Future<List<MuscleGroupModel>> getMuscleGroups() async {
    try {
      final response = await _dio.get('/api/v1/muscle-groups');
      final list = response.data as List;
      return list
          .map((e) => MuscleGroupModel.fromJson(e as Map<String, dynamic>))
          .toList();
    } on DioException catch (e) {
      throw ExerciseRepositoryException(_mapDioError(e));
    }
  }

  String _mapDioError(DioException e) {
    final status = e.response?.statusCode;
    if (status == 404) return 'Exercise not found.';
    if (status != null && status >= 500)
      return 'Server error. Please try again.';
    if (e.type == DioExceptionType.connectionError) {
      return 'Cannot reach server. Check your internet connection.';
    }
    return 'Unexpected error. Please try again.';
  }
}

@riverpod
ExerciseRepository exerciseRepository(ExerciseRepositoryRef ref) {
  return ExerciseRepositoryImpl(ref.read(apiClientProvider));
}
