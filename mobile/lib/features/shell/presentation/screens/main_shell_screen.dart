import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

/// Persistent bottom navigation shell wrapping the main app tabs.
class MainShellScreen extends StatelessWidget {
  final Widget child;

  const MainShellScreen({super.key, required this.child});

  static const _tabs = [
    _TabItem(
      label: 'Dashboard',
      icon: Icons.dashboard,
      path: '/dashboard',
      matchPrefix: '/dashboard',
    ),
    _TabItem(
      label: 'Workouts',
      icon: Icons.fitness_center,
      path: '/workouts/history',
      matchPrefix: '/workouts',
    ),
    _TabItem(
      label: 'Exercises',
      icon: Icons.list_alt,
      path: '/exercises',
      matchPrefix: '/exercises',
    ),
    _TabItem(
      label: 'Nutrition',
      icon: Icons.restaurant_menu,
      path: '/nutrition',
      matchPrefix: '/nutrition',
    ),
    _TabItem(
      label: 'Goals',
      icon: Icons.track_changes,
      path: '/goals',
      matchPrefix: '/goals',
    ),
  ];

  int _currentIndex(BuildContext context) {
    final location = GoRouterState.of(context).uri.path;
    for (var i = 0; i < _tabs.length; i++) {
      if (location.startsWith(_tabs[i].matchPrefix)) return i;
    }
    return 0;
  }

  @override
  Widget build(BuildContext context) {
    final currentIndex = _currentIndex(context);

    return Scaffold(
      body: child,
      bottomNavigationBar: NavigationBar(
        selectedIndex: currentIndex,
        onDestinationSelected: (i) => context.go(_tabs[i].path),
        destinations: _tabs
            .map(
              (t) => NavigationDestination(icon: Icon(t.icon), label: t.label),
            )
            .toList(),
      ),
    );
  }
}

class _TabItem {
  final String label;
  final IconData icon;
  final String path;
  final String matchPrefix;

  const _TabItem({
    required this.label,
    required this.icon,
    required this.path,
    required this.matchPrefix,
  });
}
