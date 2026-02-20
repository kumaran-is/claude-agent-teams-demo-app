import 'package:freezed_annotation/freezed_annotation.dart';
import 'workout_set_model.dart';

part 'workout_session_model.freezed.dart';
part 'workout_session_model.g.dart';

@freezed
class WorkoutSessionModel with _$WorkoutSessionModel {
  const factory WorkoutSessionModel({
    required String id,
    required String userId,
    String? name,
    required DateTime startedAt,
    DateTime? completedAt,
    String? notes,
    @Default([]) List<WorkoutSetModel> sets,
  }) = _WorkoutSessionModel;

  factory WorkoutSessionModel.fromJson(Map<String, dynamic> json) =>
      _$WorkoutSessionModelFromJson(json);
}
