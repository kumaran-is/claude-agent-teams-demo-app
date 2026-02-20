import 'package:firebase_auth/firebase_auth.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

part 'auth_repository.g.dart';

/// Domain error for Firebase Auth failures.
class AuthException implements Exception {
  final String message;
  const AuthException(this.message);

  @override
  String toString() => message;
}

abstract class AuthRepository {
  Stream<User?> authStateChanges();
  Future<UserCredential> signInWithEmail(String email, String password);
  Future<UserCredential> createUserWithEmail(
    String email,
    String password,
    String displayName,
  );
  Future<void> signOut();
  User? get currentUser;
}

class FirebaseAuthRepository implements AuthRepository {
  final FirebaseAuth _auth;

  const FirebaseAuthRepository(this._auth);

  @override
  Stream<User?> authStateChanges() => _auth.authStateChanges();

  @override
  Future<UserCredential> signInWithEmail(String email, String password) async {
    try {
      return await _auth.signInWithEmailAndPassword(
        email: email,
        password: password,
      );
    } on FirebaseAuthException catch (e) {
      throw AuthException(_mapError(e.code));
    }
  }

  @override
  Future<UserCredential> createUserWithEmail(
    String email,
    String password,
    String displayName,
  ) async {
    try {
      final credential = await _auth.createUserWithEmailAndPassword(
        email: email,
        password: password,
      );
      await credential.user?.updateDisplayName(displayName);
      return credential;
    } on FirebaseAuthException catch (e) {
      throw AuthException(_mapError(e.code));
    }
  }

  @override
  Future<void> signOut() async {
    await _auth.signOut();
  }

  @override
  User? get currentUser => _auth.currentUser;

  String _mapError(String code) => switch (code) {
    'user-not-found' => 'No account found with this email.',
    'wrong-password' || 'invalid-credential' => 'Incorrect email or password.',
    'email-already-in-use' => 'An account already exists with this email.',
    'invalid-email' => 'Invalid email address.',
    'weak-password' => 'Password must be at least 6 characters.',
    'too-many-requests' => 'Too many failed attempts. Please try again later.',
    'user-disabled' => 'This account has been disabled.',
    _ => 'Authentication failed. Please try again.',
  };
}

@riverpod
AuthRepository authRepository(AuthRepositoryRef ref) {
  return FirebaseAuthRepository(FirebaseAuth.instance);
}
