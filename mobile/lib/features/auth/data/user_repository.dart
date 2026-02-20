import 'package:dio/dio.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../../core/network/api_client.dart';
import '../domain/user_model.dart';

part 'user_repository.g.dart';

/// Domain error for backend user API failures.
class UserRepositoryException implements Exception {
  final String message;
  const UserRepositoryException(this.message);

  @override
  String toString() => message;
}

abstract class UserRepository {
  Future<UserModel> registerUser(String displayName);
  Future<UserModel> getProfile();
}

class UserRepositoryImpl implements UserRepository {
  final Dio _dio;

  const UserRepositoryImpl(this._dio);

  @override
  Future<UserModel> registerUser(String displayName) async {
    try {
      final response = await _dio.post(
        '/api/v1/auth/register',
        data: {'displayName': displayName},
      );
      return UserModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw UserRepositoryException(_mapDioError(e));
    }
  }

  @override
  Future<UserModel> getProfile() async {
    try {
      final response = await _dio.get('/api/v1/auth/profile');
      return UserModel.fromJson(response.data as Map<String, dynamic>);
    } on DioException catch (e) {
      throw UserRepositoryException(_mapDioError(e));
    }
  }

  String _mapDioError(DioException e) {
    final status = e.response?.statusCode;
    if (status == 401) return 'Session expired. Please sign in again.';
    if (status == 404) return 'User profile not found.';
    if (status == 409) return 'Account already registered.';
    if (status != null && status >= 500)
      return 'Server error. Please try again.';
    if (e.type == DioExceptionType.connectionTimeout ||
        e.type == DioExceptionType.sendTimeout ||
        e.type == DioExceptionType.receiveTimeout) {
      return 'Request timed out. Check your connection.';
    }
    if (e.type == DioExceptionType.connectionError) {
      return 'Cannot reach server. Check your internet connection.';
    }
    return 'Unexpected error. Please try again.';
  }
}

@riverpod
UserRepository userRepository(UserRepositoryRef ref) {
  return UserRepositoryImpl(ref.read(apiClientProvider));
}
