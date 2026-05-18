package com.armyknife.tools.features.utility

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun UnitConverterScreen(viewModel: UnitConverterViewModel = viewModel()) {
    val category = viewModel.category
    val units = remember(category) { UnitDefinitions.getUnits(category) }
    val fromUnit = remember(units, viewModel.fromUnitIndex) { units.getOrNull(viewModel.fromUnitIndex) }
    val toUnit = remember(units, viewModel.toUnitIndex) { units.getOrNull(viewModel.toUnitIndex) }
    val inputValue = viewModel.inputValue
    val result = viewModel.result
    val formattedResult = viewModel.formatResult()

    var inputText by remember { mutableStateOf("0") }

    LaunchedEffect(inputValue) {
        inputText = if (inputValue == 0.0) "0" else inputValue.toString()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Text(
            text = "Unit Converter",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Category selector
        FilledTonalButton(
            onClick = { viewModel.cycleCategory() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Category: ${category.label}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // From section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "From",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        viewModel.updateInput(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                UnitSelector(
                    units = units,
                    selectedIndex = viewModel.fromUnitIndex,
                    onSelect = { viewModel.setFromUnit(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Swap button
        IconButton(
            onClick = { viewModel.swapUnits() },
            modifier = Modifier.align(Alignment.CenterHorizontally).size(48.dp)
        ) {
            Icon(Icons.Default.SwapHoriz, contentDescription = "Swap units")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // To section
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "To",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formattedResult,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                UnitSelector(
                    units = units,
                    selectedIndex = viewModel.toUnitIndex,
                    onSelect = { viewModel.setToUnit(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Formula display
        if (fromUnit != null && toUnit != null && inputValue != 0.0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "$inputValue ${fromUnit.name} = $formattedResult ${toUnit.name}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset
        TextButton(onClick = {
            viewModel.resetToDefaults()
            inputText = "0"
        }) {
            Text("Reset")
        }
    }
}

@Composable
private fun UnitSelector(
    units: List<UnitDef>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(units, key = { it.name }) { unit ->
            val isSelected = units.indexOf(unit) == selectedIndex
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(units.indexOf(unit)) },
                label = { Text(unit.name, style = MaterialTheme.typography.labelSmall) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
