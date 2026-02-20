import 'package:freezed_annotation/freezed_annotation.dart';

part 'nutrition_log_model.freezed.dart';
part 'nutrition_log_model.g.dart';

@freezed
class NutritionLogModel with _$NutritionLogModel {
  const factory NutritionLogModel({
    required String id,
    required String userId,
    required String mealName,
    required double calories,
    required double proteinG,
    required double carbsG,
    required double fatG,
    required DateTime loggedAt,
  }) = _NutritionLogModel;

  factory NutritionLogModel.fromJson(Map<String, dynamic> json) =>
      _$NutritionLogModelFromJson(json);
}
