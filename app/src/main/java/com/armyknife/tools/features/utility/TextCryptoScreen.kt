package com.armyknife.tools.features.utility

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast

@Composable
fun TextCryptoScreen(viewModel: TextCryptoViewModel = viewModel()) {
    val context = LocalContext.current
    val operation = viewModel.operation
    val input = viewModel.input
    val output = viewModel.output
    val hasError = viewModel.hasError

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Text(
            text = "Text Crypto",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Operation selector
        FilledTonalButton(
            onClick = { viewModel.cycleOperation() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Operation: ${operation.label}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Input",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { viewModel.updateInput(it) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 5
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Output
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (hasError) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.surfaceContainerHighest
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Output",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (output.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy output", modifier = Modifier.size(18.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                SelectionContainer {
                    Text(
                        text = output.ifEmpty { "Result will appear here" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (hasError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Process button
        Button(
            onClick = { viewModel.process() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Process")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Reset
        TextButton(onClick = { viewModel.resetToDefaults() }) {
            Text("Reset")
        }
    }
}
