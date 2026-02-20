import 'package:freezed_annotation/freezed_annotation.dart';

part 'nutrition_summary_model.freezed.dart';
part 'nutrition_summary_model.g.dart';

@freezed
class NutritionSummaryModel with _$NutritionSummaryModel {
  const factory NutritionSummaryModel({
    required double totalCalories,
    required double totalProteinG,
    required double totalCarbsG,
    required double totalFatG,
  }) = _NutritionSummaryModel;

  factory NutritionSummaryModel.fromJson(Map<String, dynamic> json) =>
      _$NutritionSummaryModelFromJson(json);
}
