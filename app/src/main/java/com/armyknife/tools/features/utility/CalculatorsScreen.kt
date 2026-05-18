package com.armyknife.tools.features.utility

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun CalculatorsScreen(viewModel: CalculatorsViewModel = viewModel()) {
    val calcType = viewModel.calculatorType

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Text(
            text = "Calculators",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Type selector
        FilledTonalButton(
            onClick = { viewModel.cycleCalculator() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Type: ${calcType.label}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (calcType) {
            CalculatorType.BMI -> BmiCalculator(viewModel)
            CalculatorType.AGE -> AgeCalculator(viewModel)
            CalculatorType.DISCOUNT -> DiscountCalculator(viewModel)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { viewModel.resetToDefaults() }) {
            Text("Reset")
        }
    }
}

@Composable
private fun BmiCalculator(vm: CalculatorsViewModel) {
    var weightText by remember { mutableStateOf("") }
    var heightText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = weightText,
                onValueChange = {
                    weightText = it
                    vm.updateWeight(it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Weight (kg)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = heightText,
                onValueChange = {
                    heightText = it
                    vm.updateHeight(it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Height (m)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (vm.weightKg > 0 && vm.heightM > 0) {
                ResultRow("BMI", vm.formatValue(vm.bmiValue))
                Spacer(modifier = Modifier.height(4.dp))
                ResultRow("Category", vm.bmiCategory)
            }
        }
    }
}

@Composable
private fun AgeCalculator(vm: CalculatorsViewModel) {
    var dayText by remember { mutableStateOf("") }
    var monthText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Birth date (year 1990 assumed)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = dayText,
                onValueChange = {
                    dayText = it
                    vm.updateBirthDay(it.toIntOrNull() ?: 1)
                },
                label = { Text("Day") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = monthText,
                onValueChange = {
                    monthText = it
                    vm.updateBirthMonth(it.toIntOrNull() ?: 1)
                },
                label = { Text("Month (1-12)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            ResultRow("Years", "${vm.ageYears}")
            Spacer(modifier = Modifier.height(4.dp))
            ResultRow("Days", "${vm.ageDays}")
            Spacer(modifier = Modifier.height(4.dp))
            ResultRow("Hours", "${vm.ageHours}")
        }
    }
}

@Composable
private fun DiscountCalculator(vm: CalculatorsViewModel) {
    var priceText by remember { mutableStateOf("") }
    var percentText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = priceText,
                onValueChange = {
                    priceText = it
                    vm.updateOriginalPrice(it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Original Price") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = percentText,
                onValueChange = {
                    percentText = it
                    vm.updateDiscountPercent(it.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Discount (%)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (vm.originalPrice > 0) {
                ResultRow("You Save", "${vm.formatValue(vm.discountAmount)}")
                Spacer(modifier = Modifier.height(4.dp))
                ResultRow("Final Price", "${vm.formatValue(vm.finalPrice)}", true)
            }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
