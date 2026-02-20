import 'package:freezed_annotation/freezed_annotation.dart';

part 'muscle_group_model.freezed.dart';
part 'muscle_group_model.g.dart';

@freezed
class MuscleGroupModel with _$MuscleGroupModel {
  const factory MuscleGroupModel({
    required String id,
    required String name,
    String? description,
  }) = _MuscleGroupModel;

  factory MuscleGroupModel.fromJson(Map<String, dynamic> json) =>
      _$MuscleGroupModelFromJson(json);
}
