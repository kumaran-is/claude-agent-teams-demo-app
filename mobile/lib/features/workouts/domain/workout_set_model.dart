import 'package:freezed_annotation/freezed_annotation.dart';

part 'workout_set_model.freezed.dart';
part 'workout_set_model.g.dart';

@freezed
class WorkoutSetModel with _$WorkoutSetModel {
  const factory WorkoutSetModel({
    required String id,
    required String sessionId,
    required String exerciseId,
    String? exerciseName,
    required int setNumber,
    required int reps,
    required double weightKg,
    required DateTime completedAt,
  }) = _WorkoutSetModel;

  factory WorkoutSetModel.fromJson(Map<String, dynamic> json) =>
      _$WorkoutSetModelFromJson(json);
}
