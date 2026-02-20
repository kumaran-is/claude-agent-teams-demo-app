import 'package:firebase_auth/firebase_auth.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';
import '../../data/auth_repository.dart';
import '../../data/user_repository.dart';

part 'auth_provider.g.dart';

/// Streams Firebase authentication state changes.
/// Used by [appRouterProvider] for redirect logic and UI guards.
@riverpod
Stream<User?> authStateChanges(AuthStateChangesRef ref) {
  return ref.watch(authRepositoryProvider).authStateChanges();
}

/// Handles auth actions: sign-in, register, sign-out.
///
/// State type is [AsyncValue<void>]:
///   - [AsyncLoading] while an action is in flight
///   - [AsyncData]    after success (GoRouter handles navigation)
///   - [AsyncError]   on failure (screens show a SnackBar)
@riverpod
class AuthNotifier extends _$AuthNotifier {
  @override
  FutureOr<void> build() {
    // No initial async work needed; state starts as AsyncData(null).
  }

  Future<void> signIn(String email, String password) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      await ref.read(authRepositoryProvider).signInWithEmail(email, password);
    });
  }

  Future<void> register(
    String email,
    String password,
    String displayName,
  ) async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      await ref
          .read(authRepositoryProvider)
          .createUserWithEmail(email, password, displayName);

      // Sync the newly created Firebase user with the backend.
      await ref.read(userRepositoryProvider).registerUser(displayName);
    });
  }

  Future<void> signOut() async {
    state = const AsyncValue.loading();
    state = await AsyncValue.guard(() async {
      await ref.read(authRepositoryProvider).signOut();
    });
  }
}
