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
fun MorseCodeScreen(viewModel: MorseCodeViewModel = viewModel()) {
    val mode = viewModel.mode
    val input = viewModel.input
    val output = viewModel.output

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Text(
            text = "Morse Code",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Mode toggle
        FilledTonalButton(
            onClick = { viewModel.toggleMode() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Mode: ${mode.label}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (mode == MorseMode.TEXT_TO_MORSE) "Text Input" else "Morse Input",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { viewModel.updateInput(it) },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 6,
                    placeholder = {
                        Text(if (mode == MorseMode.TEXT_TO_MORSE) "Type text..." else "Type morse (e.g. .- ..--)")
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Output
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (mode == MorseMode.TEXT_TO_MORSE) "Morse Output" else "Text Output",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                SelectionContainer {
                    Text(
                        text = output.ifEmpty { "Result will appear here" },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Morse reference
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Quick Reference",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A: .-  B: -...  C: -.-.  D: -..  E: .  F: ..-.\n" +
                            "G: --.  H: ....  I: ..  J: .---  K: -.-  L: .-..\n" +
                            "M: --  N: -.  O: ---  P: .--.  Q: --.-  R: .-.\n" +
                            "S: ...  T: -  U: ..-  V: ...-  W: .--  X: -..-\n" +
                            "Y: -.--  Z: --..",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { viewModel.resetToDefaults() }) {
            Text("Reset")
        }
    }
}
