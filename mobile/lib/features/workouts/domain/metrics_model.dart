import 'package:freezed_annotation/freezed_annotation.dart';

part 'metrics_model.freezed.dart';
part 'metrics_model.g.dart';

@freezed
class PersonalRecord with _$PersonalRecord {
  const factory PersonalRecord({
    required String exerciseId,
    required String exerciseName,
    required double maxWeightKg,
    required int repsAtMaxWeight,
  }) = _PersonalRecord;

  factory PersonalRecord.fromJson(Map<String, dynamic> json) =>
      _$PersonalRecordFromJson(json);
}

@freezed
class MetricsModel with _$MetricsModel {
  const factory MetricsModel({
    required int weeklySessions,
    required double weeklyVolumeKg,
    required int currentStreak,
    @Default([]) List<PersonalRecord> personalRecords,
  }) = _MetricsModel;

  factory MetricsModel.fromJson(Map<String, dynamic> json) =>
      _$MetricsModelFromJson(json);
}
