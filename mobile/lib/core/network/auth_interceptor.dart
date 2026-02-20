import 'dart:developer';

import 'package:dio/dio.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

part 'auth_interceptor.g.dart';

@riverpod
AuthInterceptor authInterceptor(AuthInterceptorRef ref) {
  return AuthInterceptor();
}

/// Dio interceptor that injects a Firebase ID token as a Bearer token
/// on every outgoing request. Also refreshes the token on 401 responses.
class AuthInterceptor extends Interceptor {
  @override
  void onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    try {
      final user = FirebaseAuth.instance.currentUser;
      if (user != null) {
        // getIdToken() returns cached token and auto-refreshes when near expiry.
        final token = await user.getIdToken();
        options.headers['Authorization'] = 'Bearer $token';
      }
    } catch (e) {
      log('Failed to get ID token: $e', name: 'AuthInterceptor');
      // Continue without header — server will return 401 which triggers refresh.
    }
    handler.next(options);
  }

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) async {
    if (err.response?.statusCode == 401) {
      try {
        final user = FirebaseAuth.instance.currentUser;
        if (user != null) {
          // Force-refresh the token in case it expired between requests.
          final token = await user.getIdToken(true);

          // Rebuild options with refreshed token. The original requestOptions
          // already carry the resolved baseUrl, path, and query params.
          final opts = err.requestOptions
            ..headers['Authorization'] = 'Bearer $token';

          // Use a fresh Dio so this interceptor does not run again (avoids
          // infinite retry loop). opts.uri is the fully-resolved URL.
          final response = await Dio().fetch(opts);
          return handler.resolve(response);
        }
      } catch (e) {
        log('Token refresh failed: $e', name: 'AuthInterceptor');
        // Propagate the original 401 so GoRouter can redirect to login.
        return handler.next(
          DioException(
            requestOptions: err.requestOptions,
            response: err.response,
            type: err.type,
            error: e,
          ),
        );
      }
    }
    handler.next(err);
  }
}
