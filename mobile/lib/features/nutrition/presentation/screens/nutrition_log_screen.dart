import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../providers/nutrition_provider.dart';

class NutritionLogScreen extends ConsumerStatefulWidget {
  const NutritionLogScreen({super.key});

  @override
  ConsumerState<NutritionLogScreen> createState() => _NutritionLogScreenState();
}

class _NutritionLogScreenState extends ConsumerState<NutritionLogScreen> {
  final _formKey = GlobalKey<FormState>();
  final _mealNameController = TextEditingController();
  final _caloriesController = TextEditingController(text: '0');
  final _proteinController = TextEditingController(text: '0');
  final _carbsController = TextEditingController(text: '0');
  final _fatController = TextEditingController(text: '0');

  @override
  void dispose() {
    _mealNameController.dispose();
    _caloriesController.dispose();
    _proteinController.dispose();
    _carbsController.dispose();
    _fatController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    await ref
        .read(nutritionLogNotifierProvider.notifier)
        .logFood(
          mealName: _mealNameController.text.trim(),
          calories: double.parse(_caloriesController.text),
          proteinG: double.parse(_proteinController.text),
          carbsG: double.parse(_carbsController.text),
          fatG: double.parse(_fatController.text),
        );

    // Only dismiss if the action succeeded — error state stays on screen.
    if (mounted && !ref.read(nutritionLogNotifierProvider).hasError) {
      Navigator.of(context).pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    ref.listen<AsyncValue<void>>(nutritionLogNotifierProvider, (_, next) {
      next.whenOrNull(
        error: (err, _) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text(err.toString()),
              backgroundColor: Theme.of(context).colorScheme.error,
            ),
          );
        },
      );
    });

    final logState = ref.watch(nutritionLogNotifierProvider);

    return Scaffold(
      appBar: AppBar(title: const Text('Log Food')),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            TextFormField(
              controller: _mealNameController,
              decoration: const InputDecoration(
                labelText: 'Meal Name',
                prefixIcon: Icon(Icons.restaurant),
              ),
              textCapitalization: TextCapitalization.words,
              validator: (v) =>
                  (v == null || v.trim().isEmpty) ? 'Required' : null,
            ),
            const SizedBox(height: 16),
            Text('Macros', style: Theme.of(context).textTheme.titleSmall),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: _MacroField(
                    controller: _caloriesController,
                    label: 'Calories',
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: _MacroField(
                    controller: _proteinController,
                    label: 'Protein (g)',
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: _MacroField(
                    controller: _carbsController,
                    label: 'Carbs (g)',
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: _MacroField(
                    controller: _fatController,
                    label: 'Fat (g)',
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: logState.isLoading ? null : _submit,
              child: logState.isLoading
                  ? const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Text('Log Food'),
            ),
          ],
        ),
      ),
    );
  }
}

class _MacroField extends StatelessWidget {
  final TextEditingController controller;
  final String label;

  const _MacroField({required this.controller, required this.label});

  @override
  Widget build(BuildContext context) {
    return TextFormField(
      controller: controller,
      decoration: InputDecoration(labelText: label),
      keyboardType: const TextInputType.numberWithOptions(decimal: true),
      validator: (v) => double.tryParse(v ?? '') == null ? 'Invalid' : null,
    );
  }
}
