import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import 'package:fitness_app/features/auth/data/auth_repository.dart';
import 'package:fitness_app/features/auth/data/user_repository.dart';
import 'package:fitness_app/features/auth/domain/user_model.dart';
import 'package:fitness_app/features/auth/presentation/providers/auth_provider.dart';

// --- Mocks ---

class MockAuthRepository extends Mock implements AuthRepository {}

class MockUserRepository extends Mock implements UserRepository {}

class MockUserCredential extends Mock implements UserCredential {}

class MockFirebaseUser extends Mock implements User {}

// --- Tests ---

void main() {
  late MockAuthRepository mockAuthRepo;
  late MockUserRepository mockUserRepo;

  setUp(() {
    mockAuthRepo = MockAuthRepository();
    mockUserRepo = MockUserRepository();

    // Default: no authenticated user
    when(
      () => mockAuthRepo.authStateChanges(),
    ).thenAnswer((_) => Stream.value(null));
  });

  ProviderContainer makeContainer() => ProviderContainer(
    overrides: [
      authRepositoryProvider.overrideWith((_) => mockAuthRepo),
      userRepositoryProvider.overrideWith((_) => mockUserRepo),
    ],
  );

  group('authStateChangesProvider', () {
    test('emits null when no user is signed in', () async {
      final container = makeContainer();
      addTearDown(container.dispose);

      final stream = container.read(authStateChangesProvider.stream);
      expect(await stream.first, isNull);
    });

    test('emits User when signed in', () async {
      final mockUser = MockFirebaseUser();
      when(
        () => mockAuthRepo.authStateChanges(),
      ).thenAnswer((_) => Stream.value(mockUser));

      final container = makeContainer();
      addTearDown(container.dispose);

      final stream = container.read(authStateChangesProvider.stream);
      expect(await stream.first, equals(mockUser));
    });
  });

  group('AuthNotifier.signIn', () {
    test(
      'calls authRepository.signInWithEmail and sets data on success',
      () async {
        when(
          () => mockAuthRepo.signInWithEmail('test@example.com', 'password123'),
        ).thenAnswer((_) async => MockUserCredential());

        final container = makeContainer();
        addTearDown(container.dispose);

        await container
            .read(authNotifierProvider.notifier)
            .signIn('test@example.com', 'password123');

        expect(container.read(authNotifierProvider).hasValue, isTrue);
        verify(
          () => mockAuthRepo.signInWithEmail('test@example.com', 'password123'),
        ).called(1);
      },
    );

    test('sets error state when signIn throws AuthException', () async {
      when(
        () => mockAuthRepo.signInWithEmail(any(), any()),
      ).thenThrow(const AuthException('Incorrect email or password.'));

      final container = makeContainer();
      addTearDown(container.dispose);

      await container
          .read(authNotifierProvider.notifier)
          .signIn('test@example.com', 'wrong');

      final state = container.read(authNotifierProvider);
      expect(state.hasError, isTrue);
      expect(state.error, isA<AuthException>());
      expect(
        (state.error as AuthException).message,
        'Incorrect email or password.',
      );
    });
  });

  group('AuthNotifier.register', () {
    test('creates Firebase user and syncs with backend on success', () async {
      final mockUser = MockFirebaseUser();
      final mockCredential = MockUserCredential();
      when(() => mockCredential.user).thenReturn(mockUser);

      when(
        () => mockAuthRepo.createUserWithEmail(any(), any(), any()),
      ).thenAnswer((_) async => mockCredential);

      final fakeUserModel = UserModel(
        id: 'uuid-123',
        email: 'new@example.com',
        displayName: 'New User',
        createdAt: DateTime(2025),
      );
      when(
        () => mockUserRepo.registerUser(any()),
      ).thenAnswer((_) async => fakeUserModel);

      final container = makeContainer();
      addTearDown(container.dispose);

      await container
          .read(authNotifierProvider.notifier)
          .register('new@example.com', 'securepass', 'New User');

      expect(container.read(authNotifierProvider).hasValue, isTrue);
      verify(
        () => mockAuthRepo.createUserWithEmail(
          'new@example.com',
          'securepass',
          'New User',
        ),
      ).called(1);
      verify(() => mockUserRepo.registerUser('New User')).called(1);
    });

    test('sets error state when Firebase signup fails', () async {
      when(
        () => mockAuthRepo.createUserWithEmail(any(), any(), any()),
      ).thenThrow(
        const AuthException('An account already exists with this email.'),
      );

      final container = makeContainer();
      addTearDown(container.dispose);

      await container
          .read(authNotifierProvider.notifier)
          .register('existing@example.com', 'pass', 'User');

      final state = container.read(authNotifierProvider);
      expect(state.hasError, isTrue);
      expect(state.error, isA<AuthException>());
    });
  });

  group('AuthNotifier.signOut', () {
    test('calls authRepository.signOut and sets data on success', () async {
      when(() => mockAuthRepo.signOut()).thenAnswer((_) async {});

      final container = makeContainer();
      addTearDown(container.dispose);

      await container.read(authNotifierProvider.notifier).signOut();

      expect(container.read(authNotifierProvider).hasValue, isTrue);
      verify(() => mockAuthRepo.signOut()).called(1);
    });
  });
}
