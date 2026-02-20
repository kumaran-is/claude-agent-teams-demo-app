# Flutter Mobile Agent Memory — Fitness App

## Project Structure
```
mobile/lib/
  core/
    config/app_config.dart          # String.fromEnvironment('API_BASE_URL')
    network/api_client.dart         # @riverpod Dio with AuthInterceptor
    network/auth_interceptor.dart   # Firebase JWT + 401 retry
    router/app_router.dart          # GoRouter with ShellRoute + AuthChangeNotifier
    theme/app_theme.dart            # Material 3 light+dark
  features/
    auth/           domain/ data/ presentation/providers/ presentation/screens/
    dashboard/      presentation/screens/
    exercises/      domain/ data/ presentation/providers/ presentation/screens/
    goals/          domain/ data/ presentation/providers/ presentation/screens/
    notifications/  domain/ data/ services/ presentation/providers/ presentation/screens/
    nutrition/      domain/ data/ presentation/providers/ presentation/screens/
    shell/          presentation/screens/main_shell_screen.dart
    workouts/       domain/ data/ presentation/providers/ presentation/screens/
  main.dart
```

## Riverpod Patterns
- `@riverpod` annotation + `part '*.g.dart'` — always use code generation
- Action-only notifier: `FutureOr<void> build() {}` then `AsyncValue.guard()` in methods
- Data notifier: `Future<T> build()` for auto-loading, same guard pattern in mutations
- `ref.listen<AsyncValue<T>>(provider, (_, next) => next.whenOrNull(error: snackBar))` for errors
- `ref.watch(provider)` in `build()`, `ref.read(provider)` in action methods
- Parameterised providers: `@riverpod Future<T> foo(FooRef ref, String id)` → `fooProvider(id)`
- Invalidate after mutations: `ref.invalidate(provider)` then re-fetch

## GoRouter Patterns
- `ShellRoute` wraps tabbed screens; `MainShellScreen` holds `NavigationBar`
- `_AuthChangeNotifier extends ChangeNotifier` wraps `authStateChanges()` stream as `refreshListenable`
- `redirect` uses `FirebaseAuth.instance.currentUser` (sync check)
- Full-screen routes (active workout) placed **outside** the ShellRoute
- `state.pathParameters['id']!` for path params; `context.push()` for nested, `context.go()` for tab switches

## Repository Pattern
- Each repo gets `final Dio _dio` injected via `@riverpod` provider watching `apiClientProvider`
- `DioException` caught → domain exception rethrown (never silent failures)
- `switch (e.response?.statusCode)` Dart 3 expression for error mapping
- Paginated responses: check for `{content: [...]}` (Spring Page) vs plain list

## FCM Setup
- `@pragma('vm:entry-point')` on background handler (top-level function)
- `initFcmProvider` watched in `FitnessApp.build()` — initialises lazily after ProviderScope ready
- Token refresh via `onTokenRefresh` stream — re-registers with backend
- Non-fatal token errors: log + continue (don't crash app)

## Backend API Mapping
- Auth:         `/api/v1/auth/register`, `/api/v1/auth/profile`
- Exercises:    `/api/v1/exercises`, `/api/v1/muscle-groups`
- Workouts:     `/api/v1/workouts` (CRUD + `/sessions/:id/sets`, `/metrics`)
- Nutrition:    `/api/v1/nutrition/logs`, `/api/v1/nutrition/summary`
- Goals:        `/api/v1/goals` (CRUD + `/:id/progress`)
- Notifications:`/api/v1/notifications/preferences`, `/api/v1/notifications/fcm-token`

## Freezed Models
- Always: `@freezed class Foo with _$Foo`, `const factory Foo({...}) = _Foo`, `fromJson` factory
- `part 'foo.freezed.dart'` + `part 'foo.g.dart'` — both needed for JSON serialisation
- Nested lists: type must also be Freezed (or plain Dart serialisable)

## Key Dependencies (pubspec.yaml)
- flutter_riverpod ^2.6.1, riverpod_annotation ^2.3.5, riverpod_generator ^2.4.0
- go_router ^14.0.0
- dio ^5.7.0
- freezed_annotation ^2.4.1, freezed ^2.5.0 (dev)
- json_annotation ^4.9.0, json_serializable ^6.8.0 (dev)
- firebase_core ^3.6.0, firebase_auth ^5.3.0, firebase_messaging ^15.1.0
- build_runner ^2.4.0 (dev), mocktail ^1.0.0 (dev)

## Code Generation
```bash
flutter pub run build_runner build --delete-conflicting-outputs
```
Run after any change to `@riverpod`, `@freezed`, or `@JsonSerializable` annotated files.

## Common Gotchas
- `DropdownButtonFormField` with nullable `value: null` needs a null `DropdownMenuItem` as first item
- `MediaQuery.of(context).viewInsets.bottom` in bottom sheets to avoid keyboard overlap
- `addPostFrameCallback` in `initState` when reading providers that trigger async actions
- `NavigationBar` (Material 3) not `BottomNavigationBar` — use `selectedIndex` + `onDestinationSelected`
- `GoRouterState.of(context).uri.path` to get current path inside ShellRoute for tab highlighting
