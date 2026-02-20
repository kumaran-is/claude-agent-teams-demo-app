# Flutter Mobile Agent Memory — Fitness App

## Project: fitness-app-dev

### Structure established in Task #10

```
mobile/
  pubspec.yaml
  lib/
    main.dart                        # Firebase.initializeApp(), ProviderScope, MaterialApp.router
    core/
      config/app_config.dart         # API_BASE_URL via --dart-define
      network/api_client.dart        # Dio + authInterceptor
      network/auth_interceptor.dart  # Firebase JWT injection + 401 retry
      router/app_router.dart         # GoRouter + _AuthChangeNotifier
      theme/app_theme.dart           # Material3 light/dark
    features/
      auth/data/auth_repository.dart      # FirebaseAuthRepository
      auth/data/user_repository.dart      # REST /api/v1/auth/*
      auth/domain/user_model.dart         # @freezed
      auth/presentation/providers/auth_provider.dart
      auth/presentation/screens/login_screen.dart
      auth/presentation/screens/register_screen.dart
      dashboard/presentation/screens/dashboard_screen.dart  # placeholder
  test/
    auth/auth_provider_test.dart
```

### Key Provider Pattern

- `authStateChangesProvider` → `Stream<User?>` (Firebase auth state)
- `AuthNotifier` → `AsyncNotifier<void>` for actions (signIn/register/signOut)
- GoRouter uses `_AuthChangeNotifier` (ChangeNotifier wrapping Firebase stream) as `refreshListenable`
- Redirect: not logged in → `/login`; logged in + auth path → `/dashboard`

### Backend API Mapping

- POST /api/v1/auth/register — sync Firebase user to DB, returns UserModel
- GET /api/v1/auth/profile — get current user profile

### Error Pattern

- `AuthException` (FirebaseAuth errors, human-readable messages)
- `UserRepositoryException` (Dio/backend errors, human-readable messages)
- Screens use `ref.listen` + `whenOrNull(error:)` to show SnackBar
- Buttons disabled while `authState.isLoading`

### Dependencies (pubspec.yaml)

- flutter_riverpod: ^2.6.1, riverpod_annotation: ^2.3.5
- go_router: ^14.0.0, dio: ^5.7.0
- freezed_annotation: ^2.4.1, json_annotation: ^4.9.0
- firebase_core: ^3.6.0, firebase_auth: ^5.3.0, firebase_messaging: ^15.1.0
- dev: build_runner ^2.4.0, freezed ^2.5.0, riverpod_generator ^2.4.0, json_serializable ^6.8.0, mocktail ^1.0.0

### Setup Commands

```bash
flutter create . --project-name fitness_app --org com.fitnessapp --platforms ios,android
flutter pub get
dart run build_runner build --delete-conflicting-outputs
```

### Tasks remaining: #11 (workouts+exercises), #12 (nutrition+goals), #13 (push notifications)
