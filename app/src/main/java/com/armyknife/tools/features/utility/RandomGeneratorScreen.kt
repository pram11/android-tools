package com.armyknife.tools.features.utility

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RandomGeneratorScreen(viewModel: RandomGeneratorViewModel = viewModel()) {
    val randomType = viewModel.randomType
    val result = viewModel.result
    val min = viewModel.min
    val max = viewModel.max
    val stringLength = viewModel.stringLength

    var minText by remember { mutableStateOf(min.toString()) }
    var maxText by remember { mutableStateOf(max.toString()) }
    var lengthText by remember { mutableStateOf(stringLength.toString()) }

    LaunchedEffect(min) { minText = min.toString() }
    LaunchedEffect(max) { maxText = max.toString() }
    LaunchedEffect(stringLength) { lengthText = stringLength.toString() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Random Generator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Type selector
        FilledTonalButton(
            onClick = { viewModel.cycleType() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Type: ${randomType.label}")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Settings based on type
        when (randomType) {
            RandomType.NUMBER -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minText,
                        onValueChange = {
                            minText = it
                            viewModel.updateMin(it.toIntOrNull() ?: 1)
                        },
                        label = { Text("Min") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxText,
                        onValueChange = {
                            maxText = it
                            viewModel.updateMax(it.toIntOrNull() ?: 100)
                        },
                        label = { Text("Max") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
            RandomType.STRING -> {
                OutlinedTextField(
                    value = lengthText,
                    onValueChange = {
                        lengthText = it
                        viewModel.updateStringLength(it.toIntOrNull() ?: 8)
                    },
                    label = { Text("Length (1-64)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            else -> { /* No settings for UUID, Dice, Coin */ }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Result display
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Box(
                modifier = Modifier.padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                SelectionContainer {
                    Text(
                        text = result.ifEmpty { "Tap generate" },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Generate button
        Button(
            onClick = {
                when (randomType) {
                    RandomType.NUMBER -> viewModel.generateRandomInt()
                    RandomType.STRING -> viewModel.generateRandomString()
                    RandomType.UUID -> viewModel.generateUuid()
                    RandomType.DICE -> viewModel.rollDice()
                    RandomType.COIN -> viewModel.flipCoin()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            viewModel.resetToDefaults()
            minText = "1"
            maxText = "100"
            lengthText = "1"
        }) {
            Text("Reset")
        }
    }
}
