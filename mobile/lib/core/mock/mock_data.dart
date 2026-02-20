import '../../features/exercises/domain/exercise_model.dart';
import '../../features/exercises/domain/muscle_group_model.dart';
import '../../features/goals/domain/goal_model.dart';
import '../../features/nutrition/domain/nutrition_log_model.dart';
import '../../features/nutrition/domain/nutrition_summary_model.dart';
import '../../features/workouts/domain/metrics_model.dart';
import '../../features/workouts/domain/workout_session_model.dart';
import '../../features/workouts/domain/workout_set_model.dart';

/// Static demo data shown when running without a backend (demo mode).
abstract final class MockData {
  // ── Metrics ────────────────────────────────────────────────────────────────
  static final MetricsModel metrics = MetricsModel(
    weeklySessions: 4,
    weeklyVolumeKg: 2840,
    currentStreak: 7,
    personalRecords: [
      PersonalRecord(
        exerciseId: 'ex-1',
        exerciseName: 'Bench Press',
        maxWeightKg: 100,
        repsAtMaxWeight: 5,
      ),
      PersonalRecord(
        exerciseId: 'ex-2',
        exerciseName: 'Squat',
        maxWeightKg: 140,
        repsAtMaxWeight: 3,
      ),
      PersonalRecord(
        exerciseId: 'ex-3',
        exerciseName: 'Deadlift',
        maxWeightKg: 180,
        repsAtMaxWeight: 1,
      ),
    ],
  );

  // ── Nutrition ──────────────────────────────────────────────────────────────
  static const NutritionSummaryModel nutritionSummary = NutritionSummaryModel(
    totalCalories: 1850,
    totalProteinG: 145,
    totalCarbsG: 210,
    totalFatG: 62,
  );

  static final List<NutritionLogModel> nutritionLogs = [
    NutritionLogModel(
      id: 'log-1',
      userId: 'demo',
      mealName: 'Breakfast — Oats & Eggs',
      calories: 520,
      proteinG: 38,
      carbsG: 60,
      fatG: 14,
      loggedAt: DateTime.now().subtract(const Duration(hours: 6)),
    ),
    NutritionLogModel(
      id: 'log-2',
      userId: 'demo',
      mealName: 'Lunch — Chicken Rice Bowl',
      calories: 780,
      proteinG: 65,
      carbsG: 85,
      fatG: 22,
      loggedAt: DateTime.now().subtract(const Duration(hours: 3)),
    ),
    NutritionLogModel(
      id: 'log-3',
      userId: 'demo',
      mealName: 'Post-Workout Shake',
      calories: 320,
      proteinG: 42,
      carbsG: 30,
      fatG: 6,
      loggedAt: DateTime.now().subtract(const Duration(hours: 1)),
    ),
    NutritionLogModel(
      id: 'log-4',
      userId: 'demo',
      mealName: 'Dinner — Salmon & Veggies',
      calories: 230,
      proteinG: 0,
      carbsG: 35,
      fatG: 20,
      loggedAt: DateTime.now().subtract(const Duration(minutes: 20)),
    ),
  ];

  // ── Workout sessions ───────────────────────────────────────────────────────
  static final List<WorkoutSessionModel> workoutHistory = [
    WorkoutSessionModel(
      id: 'session-1',
      userId: 'demo',
      name: 'Push Day — Chest & Shoulders',
      startedAt: DateTime.now().subtract(const Duration(hours: 2)),
      completedAt: DateTime.now().subtract(const Duration(hours: 1)),
      sets: [
        WorkoutSetModel(
          id: 'set-1',
          sessionId: 'session-1',
          exerciseId: 'ex-1',
          exerciseName: 'Bench Press',
          setNumber: 1,
          reps: 5,
          weightKg: 100,
          completedAt: DateTime.now().subtract(
            const Duration(hours: 1, minutes: 50),
          ),
        ),
        WorkoutSetModel(
          id: 'set-2',
          sessionId: 'session-1',
          exerciseId: 'ex-1',
          exerciseName: 'Bench Press',
          setNumber: 2,
          reps: 5,
          weightKg: 100,
          completedAt: DateTime.now().subtract(
            const Duration(hours: 1, minutes: 45),
          ),
        ),
        WorkoutSetModel(
          id: 'set-3',
          sessionId: 'session-1',
          exerciseId: 'ex-4',
          exerciseName: 'Overhead Press',
          setNumber: 1,
          reps: 8,
          weightKg: 60,
          completedAt: DateTime.now().subtract(
            const Duration(hours: 1, minutes: 30),
          ),
        ),
      ],
    ),
    WorkoutSessionModel(
      id: 'session-2',
      userId: 'demo',
      name: 'Pull Day — Back & Biceps',
      startedAt: DateTime.now().subtract(const Duration(days: 2, hours: 1)),
      completedAt: DateTime.now().subtract(const Duration(days: 2)),
      sets: [
        WorkoutSetModel(
          id: 'set-4',
          sessionId: 'session-2',
          exerciseId: 'ex-3',
          exerciseName: 'Deadlift',
          setNumber: 1,
          reps: 1,
          weightKg: 180,
          completedAt: DateTime.now().subtract(
            const Duration(days: 2, minutes: 50),
          ),
        ),
        WorkoutSetModel(
          id: 'set-5',
          sessionId: 'session-2',
          exerciseId: 'ex-5',
          exerciseName: 'Pull-Up',
          setNumber: 1,
          reps: 10,
          weightKg: 0,
          completedAt: DateTime.now().subtract(
            const Duration(days: 2, minutes: 30),
          ),
        ),
      ],
    ),
    WorkoutSessionModel(
      id: 'session-3',
      userId: 'demo',
      name: 'Leg Day',
      startedAt: DateTime.now().subtract(const Duration(days: 4, hours: 1)),
      completedAt: DateTime.now().subtract(const Duration(days: 4)),
      sets: [
        WorkoutSetModel(
          id: 'set-6',
          sessionId: 'session-3',
          exerciseId: 'ex-2',
          exerciseName: 'Squat',
          setNumber: 1,
          reps: 3,
          weightKg: 140,
          completedAt: DateTime.now().subtract(
            const Duration(days: 4, minutes: 55),
          ),
        ),
        WorkoutSetModel(
          id: 'set-7',
          sessionId: 'session-3',
          exerciseId: 'ex-6',
          exerciseName: 'Leg Press',
          setNumber: 1,
          reps: 12,
          weightKg: 200,
          completedAt: DateTime.now().subtract(
            const Duration(days: 4, minutes: 35),
          ),
        ),
      ],
    ),
  ];

  // ── Exercises ──────────────────────────────────────────────────────────────
  static final List<MuscleGroupModel> muscleGroups = [
    const MuscleGroupModel(id: 'mg-1', name: 'Chest', description: 'Pectorals'),
    const MuscleGroupModel(
      id: 'mg-2',
      name: 'Back',
      description: 'Lats, traps, rhomboids',
    ),
    const MuscleGroupModel(
      id: 'mg-3',
      name: 'Legs',
      description: 'Quads, hamstrings, glutes',
    ),
    const MuscleGroupModel(
      id: 'mg-4',
      name: 'Shoulders',
      description: 'Deltoids',
    ),
    const MuscleGroupModel(
      id: 'mg-5',
      name: 'Arms',
      description: 'Biceps, triceps',
    ),
    const MuscleGroupModel(
      id: 'mg-6',
      name: 'Core',
      description: 'Abs, obliques',
    ),
  ];

  static final List<ExerciseModel> exercises = [
    ExerciseModel(
      id: 'ex-1',
      name: 'Bench Press',
      muscleGroupId: 'mg-1',
      muscleGroupName: 'Chest',
      description: 'Classic compound chest movement',
      instructions:
          'Lie flat, grip bar slightly wider than shoulder-width, lower to chest, press up.',
    ),
    ExerciseModel(
      id: 'ex-2',
      name: 'Squat',
      muscleGroupId: 'mg-3',
      muscleGroupName: 'Legs',
      description: 'King of lower body exercises',
      instructions:
          'Bar on upper back, feet shoulder-width, squat until thighs parallel, drive up.',
    ),
    ExerciseModel(
      id: 'ex-3',
      name: 'Deadlift',
      muscleGroupId: 'mg-2',
      muscleGroupName: 'Back',
      description: 'Full-body compound pull',
      instructions:
          'Hip-width stance, hinge at hips, grip bar, drive through heels.',
    ),
    ExerciseModel(
      id: 'ex-4',
      name: 'Overhead Press',
      muscleGroupId: 'mg-4',
      muscleGroupName: 'Shoulders',
      description: 'Strict press for shoulder strength',
      instructions:
          'Bar at collarbone, press overhead until lockout, lower with control.',
    ),
    ExerciseModel(
      id: 'ex-5',
      name: 'Pull-Up',
      muscleGroupId: 'mg-2',
      muscleGroupName: 'Back',
      description: 'Bodyweight lat pulldown',
      instructions: 'Hang from bar, pull chest to bar, lower slowly.',
    ),
    ExerciseModel(
      id: 'ex-6',
      name: 'Leg Press',
      muscleGroupId: 'mg-3',
      muscleGroupName: 'Legs',
      description: 'Machine compound leg exercise',
      instructions: 'Seat, feet on platform, lower to 90 degrees, push back.',
    ),
    ExerciseModel(
      id: 'ex-7',
      name: 'Dumbbell Curl',
      muscleGroupId: 'mg-5',
      muscleGroupName: 'Arms',
      description: 'Classic bicep isolation',
      instructions: 'Stand, curl dumbbells to shoulders, squeeze at top.',
    ),
    ExerciseModel(
      id: 'ex-8',
      name: 'Tricep Dip',
      muscleGroupId: 'mg-5',
      muscleGroupName: 'Arms',
      description: 'Compound tricep exercise',
      instructions:
          'Grip parallel bars, lower body by bending elbows, press back.',
    ),
    ExerciseModel(
      id: 'ex-9',
      name: 'Plank',
      muscleGroupId: 'mg-6',
      muscleGroupName: 'Core',
      description: 'Isometric core stability',
      instructions: 'Forearms on ground, body straight, hold position.',
    ),
    ExerciseModel(
      id: 'ex-10',
      name: 'Romanian Deadlift',
      muscleGroupId: 'mg-3',
      muscleGroupName: 'Legs',
      description: 'Hamstring-focused hinge',
      instructions:
          'Hip-width stance, hinge forward with soft knees, feel hamstring stretch.',
    ),
  ];

  // ── Goals ──────────────────────────────────────────────────────────────────
  static final List<GoalModel> goals = [
    GoalModel(
      id: 'goal-1',
      goalType: 'TARGET_WEIGHT',
      targetValue: 80,
      unit: 'kg',
      targetDate: DateTime.now().add(const Duration(days: 90)),
      createdAt: DateTime.now().subtract(const Duration(days: 30)),
    ),
    GoalModel(
      id: 'goal-2',
      goalType: 'WORKOUT_FREQUENCY',
      targetValue: 5,
      unit: 'sessions/week',
      targetDate: null,
      createdAt: DateTime.now().subtract(const Duration(days: 14)),
    ),
  ];
}
