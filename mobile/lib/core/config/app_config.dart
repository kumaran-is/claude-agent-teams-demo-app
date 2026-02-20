/// Application-wide configuration.
/// Values injected at build time via --dart-define.
class AppConfig {
  const AppConfig._();

  /// Backend API base URL.
  /// Override with: flutter run --dart-define=API_BASE_URL=http://localhost:8080
  static const String apiBaseUrl = String.fromEnvironment(
    'API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080', // Android emulator → localhost
  );

  static const Duration connectionTimeout = Duration(seconds: 30);
  static const Duration receiveTimeout = Duration(seconds: 30);
}
