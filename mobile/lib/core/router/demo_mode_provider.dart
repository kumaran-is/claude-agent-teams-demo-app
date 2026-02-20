import 'package:flutter/foundation.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

part 'demo_mode_provider.g.dart';

/// Simple flag that lets the demo bypass Firebase auth.
@riverpod
class DemoMode extends _$DemoMode {
  @override
  bool build() => false;

  void enable() => state = true;
}

/// A ChangeNotifier that bridges [DemoMode] into GoRouter's refreshListenable.
class DemoModeNotifier extends ChangeNotifier {
  bool _active = false;
  bool get active => _active;

  void activate() {
    _active = true;
    notifyListeners();
  }
}
