import 'dart:async';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:riverpod_annotation/riverpod_annotation.dart';

import '../../features/auth/presentation/screens/login_screen.dart';
import '../../features/auth/presentation/screens/register_screen.dart';
import '../../features/dashboard/presentation/screens/dashboard_screen.dart';
import '../../features/exercises/presentation/screens/exercise_detail_screen.dart';
import '../../features/exercises/presentation/screens/exercise_list_screen.dart';
import '../../features/goals/presentation/screens/goals_screen.dart';
import '../../features/nutrition/presentation/screens/nutrition_log_screen.dart';
import '../../features/nutrition/presentation/screens/nutrition_summary_screen.dart';
import '../../features/shell/presentation/screens/main_shell_screen.dart';
import '../../features/notifications/presentation/screens/notification_preferences_screen.dart';
import '../../features/workouts/presentation/screens/active_workout_screen.dart';
import '../../features/workouts/presentation/screens/metrics_dashboard_screen.dart';
import '../../features/workouts/presentation/screens/workout_history_screen.dart';
import 'demo_mode_provider.dart';

part 'app_router.g.dart';

/// Mirrors Firebase auth state changes so GoRouter re-runs redirect on sign-in/out.
class _AuthChangeNotifier extends ChangeNotifier {
  late final StreamSubscription<User?> _sub;
  final DemoModeNotifier demoNotifier;

  _AuthChangeNotifier(this.demoNotifier) {
    _sub = FirebaseAuth.instance.authStateChanges().listen((_) {
      notifyListeners();
    });
  }

  @override
  void dispose() {
    _sub.cancel();
    super.dispose();
  }
}

const _authPaths = {'/login', '/register'};

@riverpod
GoRouter appRouter(AppRouterRef ref) {
  final demoNotifier = DemoModeNotifier();
  final authNotifier = _AuthChangeNotifier(demoNotifier);
  ref.onDispose(() {
    authNotifier.dispose();
    demoNotifier.dispose();
  });

  // Watch demo mode — rebuilds the provider (and thus the router) when toggled.
  final isDemoMode = ref.watch(demoModeProvider);

  // Expose the DemoModeNotifier so LoginScreen can activate it.
  ref.watch(demoModeProvider.notifier);

  return GoRouter(
    initialLocation: '/dashboard',
    refreshListenable: Listenable.merge([authNotifier, demoNotifier]),
    redirect: (context, state) {
      final isLoggedIn =
          FirebaseAuth.instance.currentUser != null || isDemoMode;
      final path = state.uri.path;
      final isAuthPath = _authPaths.contains(path);

      if (!isLoggedIn && !isAuthPath) return '/login';
      if (isLoggedIn && isAuthPath) return '/dashboard';
      return null;
    },
    routes: [
      // ── Auth routes (no shell / bottom nav) ──────────────────────────────
      GoRoute(
        path: '/login',
        name: 'login',
        builder: (context, state) => const LoginScreen(),
      ),
      GoRoute(
        path: '/register',
        name: 'register',
        builder: (context, state) => const RegisterScreen(),
      ),

      // ── Workout routes outside the shell (full-screen) ────────────────────
      GoRoute(
        path: '/workouts/active',
        name: 'activeWorkout',
        builder: (context, state) => const ActiveWorkoutScreen(),
      ),

      // ── Main shell with bottom navigation ────────────────────────────────
      ShellRoute(
        builder: (context, state, child) => MainShellScreen(child: child),
        routes: [
          GoRoute(
            path: '/dashboard',
            name: 'dashboard',
            builder: (context, state) => const DashboardScreen(),
          ),
          GoRoute(
            path: '/workouts/history',
            name: 'workoutHistory',
            builder: (context, state) => const WorkoutHistoryScreen(),
          ),
          GoRoute(
            path: '/workouts/metrics',
            name: 'workoutMetrics',
            builder: (context, state) => const MetricsDashboardScreen(),
          ),
          GoRoute(
            path: '/exercises',
            name: 'exercises',
            builder: (context, state) => const ExerciseListScreen(),
          ),
          GoRoute(
            path: '/exercises/:id',
            name: 'exerciseDetail',
            builder: (context, state) =>
                ExerciseDetailScreen(exerciseId: state.pathParameters['id']!),
          ),
          GoRoute(
            path: '/nutrition',
            name: 'nutrition',
            builder: (context, state) => const NutritionSummaryScreen(),
          ),
          GoRoute(
            path: '/nutrition/log',
            name: 'nutritionLog',
            builder: (context, state) => const NutritionLogScreen(),
          ),
          GoRoute(
            path: '/goals',
            name: 'goals',
            builder: (context, state) => const GoalsScreen(),
          ),
          GoRoute(
            path: '/settings/notifications',
            name: 'notificationPreferences',
            builder: (context, state) => const NotificationPreferencesScreen(),
          ),
        ],
      ),
    ],
  );
}
