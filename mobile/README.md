# Fitness App — Flutter Mobile

Cross-platform fitness tracking app for iOS and Android.

## Tech Stack

- Flutter 3.38 / Dart 3.11
- Riverpod 2.x (state management, `@riverpod` code generation)
- Freezed (immutable models)
- GoRouter 14.x (declarative routing with auth guard)
- Dio 5.x (HTTP client with Firebase JWT interceptor)
- Firebase Auth 5.x + Firebase Messaging 15.x

## Project Setup

### 1. Create platform files

Run in this directory to generate `android/` and `ios/` platform folders:

```bash
flutter create . --project-name fitness_app --org com.fitnessapp --platforms ios,android
```

### 2. Install dependencies

```bash
flutter pub get
```

### 3. Run code generation

```bash
dart run build_runner build --delete-conflicting-outputs
```

### 4. Firebase configuration

- Add `google-services.json` to `android/app/`
- Add `GoogleService-Info.plist` to `ios/Runner/`
- Enable Email/Password auth in Firebase Console

### 5. Run the app

```bash
# Android emulator or device
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080

# iOS simulator
flutter run --dart-define=API_BASE_URL=http://localhost:8080
```

## Project Structure

```
lib/
  main.dart                         # App entry point, Firebase init, ProviderScope
  core/
    config/app_config.dart          # API base URL (injected via --dart-define)
    network/
      api_client.dart               # Dio instance provider
      auth_interceptor.dart         # Firebase JWT injection on every request
    router/app_router.dart          # GoRouter with auth guard
    theme/app_theme.dart            # Material light + dark themes
  features/
    auth/
      data/
        auth_repository.dart        # Firebase Auth operations
        user_repository.dart        # REST calls to /api/v1/auth
      domain/user_model.dart        # Freezed user model
      presentation/
        providers/auth_provider.dart
        screens/login_screen.dart
        screens/register_screen.dart
    dashboard/
      presentation/screens/dashboard_screen.dart  # Placeholder (expanded in later tasks)
    exercises/      # Added in Task #11
    workouts/       # Added in Task #11
    nutrition/      # Added in Task #12
    goals/          # Added in Task #12
    notifications/  # Added in Task #13
test/
  auth/auth_provider_test.dart
```

## Common Commands

```bash
flutter run                                                    # Run (debug)
flutter test                                                   # Run tests
flutter analyze                                                # Static analysis
dart run build_runner build --delete-conflicting-outputs       # Regenerate code
flutter clean && flutter pub get                               # Clean rebuild
```
