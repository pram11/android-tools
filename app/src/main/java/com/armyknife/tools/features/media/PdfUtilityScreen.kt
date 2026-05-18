package com.armyknife.tools.features.media

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun PdfUtilityScreen(viewModel: PdfUtilityViewModel = viewModel()) {
    val pageSize = viewModel.pageSize
    val fontSize = viewModel.fontSize
    val lineSpacing = viewModel.lineSpacing
    val marginTop = viewModel.marginTop
    val marginBottom = viewModel.marginBottom
    val marginLeft = viewModel.marginLeft
    val marginRight = viewModel.marginRight
    val documentTitle = viewModel.documentTitle
    val documentAuthor = viewModel.documentAuthor
    val text = viewModel.text
    val estimatedPages = remember(text, fontSize, lineSpacing, marginTop, marginBottom, marginLeft, marginRight, pageSize) {
        viewModel.estimatePageCount(text)
    }
    val lineHeight = viewModel.calculateLineHeight()
    val charsPerLine = viewModel.estimateCharsPerLine()
    val linesPerPage = viewModel.estimateLinesPerPage()
    val usableWidth = viewModel.calculateUsableWidth()
    val usableHeight = viewModel.calculateUsableHeight()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Text(
            text = "PDF Utility",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Create PDF documents with configurable page settings",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Document metadata
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Document Metadata",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = documentTitle,
                    onValueChange = { viewModel.updateDocumentTitle(it) },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = documentAuthor,
                    onValueChange = { viewModel.updateDocumentAuthor(it) },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Page settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Page Settings",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Page size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Page Size")
                    FilledTonalButton(
                        onClick = { viewModel.cyclePageSize() },
                        modifier = Modifier.defaultMinSize(minWidth = 100.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(pageSize.label)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Font size slider
                Text(text = "Font Size: $fontSize pt")
                Slider(
                    value = fontSize.toFloat(),
                    onValueChange = { viewModel.updateFontSize(it.toInt()) },
                    valueRange = 8f..72f,
                    steps = 64
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Line spacing slider
                Text(text = "Line Spacing: ${"%.1f".format(lineSpacing)}x")
                Slider(
                    value = lineSpacing,
                    onValueChange = { viewModel.updateLineSpacing(it) },
                    valueRange = 1.0f..3.0f,
                    steps = 19
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Margins
                Text(text = "Margins: ${"%.0f".format(marginTop)}pt")
                Slider(
                    value = (marginTop + marginBottom + marginLeft + marginRight) / 4,
                    onValueChange = {
                        val m = it.toInt().toFloat()
                        viewModel.updateMargins(m, m, m, m)
                    },
                    valueRange = 0f..200f,
                    steps = 40
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Text input
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Content",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { viewModel.updateText(it) },
                    label = { Text("Enter text content...") },
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    maxLines = 10
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Statistics
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Estimation",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                PdfStatRow("Pages", "$estimatedPages")
                PdfStatRow("Chars/Line", "$charsPerLine")
                PdfStatRow("Lines/Page", "$linesPerPage")
                PdfStatRow("Line Height", "${"%.1f".format(lineHeight)} pt")
                PdfStatRow("Usable Width", "${"%.0f".format(usableWidth)} pt")
                PdfStatRow("Usable Height", "${"%.0f".format(usableHeight)} pt")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(
                onClick = { viewModel.resetToDefaults() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset to defaults")
            }
            Button(
                onClick = {},
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Generate PDF")
            }
        }
    }
}

@Composable
private fun PdfStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
