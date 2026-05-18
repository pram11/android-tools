package com.armyknife.tools.features.media

import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ImageConverterScreen(viewModel: ImageConverterViewModel = viewModel()) {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<String?>(null) }
    var originalDimensions by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var originalFileSize by remember { mutableStateOf<Long?>(null) }
    var conversionResult by remember { mutableStateOf<ConversionResult?>(null) }
    var isConverting by remember { mutableStateOf(false) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = inputStream?.use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream)
                }

                if (bitmap != null) {
                    selectedImageUri = it.toString()
                    originalDimensions = bitmap.width to bitmap.height
                    originalFileSize = try {
                        context.contentResolver.openInputStream(it)?.use { stream ->
                            stream.available().toLong()
                        } ?: bitmap.byteCount.toLong()
                    } catch (e: Exception) {
                        bitmap.byteCount.toLong()
                    }
                    // Reset conversion result when new image selected
                    conversionResult = null
                } else {
                    Toast.makeText(context, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    ImageConverterContent(
        selectedImageUri = selectedImageUri,
        originalDimensions = originalDimensions,
        originalFileSize = originalFileSize,
        targetFormat = viewModel.targetFormat,
        quality = viewModel.quality,
        resizeWidth = viewModel.resizeWidth,
        resizeHeight = viewModel.resizeHeight,
        maintainAspect = viewModel.maintainAspect,
        conversionResult = conversionResult,
        isConverting = isConverting,
        onPickImage = { pickImageLauncher.launch("image/*") },
        onCycleFormat = { viewModel.cycleFormat() },
        onQualityChanged = { viewModel.updateQuality(it) },
        onResizeWidthChanged = { viewModel.updateResizeWidth(it.toIntOrNull() ?: 0) },
        onResizeHeightChanged = { viewModel.updateResizeHeight(it.toIntOrNull() ?: 0) },
        onToggleMaintainAspect = { viewModel.toggleMaintainAspect() },
        onReset = {
            viewModel.resetToDefaults()
            selectedImageUri = null
            originalDimensions = null
            originalFileSize = null
            conversionResult = null
        },
        onConvert = {
            originalDimensions?.let { (ow, oh) ->
                isConverting = true
                val config = viewModel.buildConfig(ow, oh)
                try {
                    val result = convertImage(context, config, selectedImageUri ?: "")
                    conversionResult = result
                } catch (e: Exception) {
                    Toast.makeText(context, "Conversion failed: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isConverting = false
                }
            }
        }
    )
}

private sealed class ConversionResult {
    data class Success(val outputUri: String, val newSize: Long, val ratio: Float) : ConversionResult()
    data class Error(val message: String) : ConversionResult()
}

private fun convertImage(context: android.content.Context, config: ConversionConfig, inputUri: String): ConversionResult {
    try {
        val inputStream = context.contentResolver.openInputStream(android.net.Uri.parse(inputUri))
        val bitmap = inputStream?.use {
            android.graphics.BitmapFactory.decodeStream(it)
        } ?: throw IllegalArgumentException("Cannot open image")

        // Resize if needed
        val finalBitmap = if (config.width != config.originalWidth || config.height != config.originalHeight) {
            Bitmap.createScaledBitmap(bitmap, config.width, config.height, true)
        } else {
            bitmap
        }

        // Create output file
        val outputFormat = when (config.format) {
            ImageFormat.PNG -> android.graphics.Bitmap.CompressFormat.PNG
            ImageFormat.JPEG -> android.graphics.Bitmap.CompressFormat.JPEG
            ImageFormat.WEBP -> android.graphics.Bitmap.CompressFormat.WEBP
        }

        val fileName = "converted_${System.currentTimeMillis()}.${config.format.extension}"
        val outputStream = context.openFileOutput(fileName, android.content.Context.MODE_PRIVATE)

        finalBitmap.compress(outputFormat, config.quality, outputStream)
        outputStream.close()
        bitmap.recycle()
        if (finalBitmap != bitmap) finalBitmap.recycle()

        val outputFile = context.getFileStreamPath(fileName)
        val newSize = outputFile.length()

        return ConversionResult.Success(
            outputUri = outputFile.absolutePath,
            newSize = newSize,
            ratio = 0f // Will be calculated by caller
        )
    } catch (e: Exception) {
        return ConversionResult.Error(e.message ?: "Unknown error")
    }
}

@Composable
private fun ImageConverterContent(
    selectedImageUri: String?,
    originalDimensions: Pair<Int, Int>?,
    originalFileSize: Long?,
    targetFormat: ImageFormat,
    quality: Int,
    resizeWidth: Int,
    resizeHeight: Int,
    maintainAspect: Boolean,
    conversionResult: ConversionResult?,
    isConverting: Boolean,
    onPickImage: () -> Unit,
    onCycleFormat: () -> Unit,
    onQualityChanged: (Int) -> Unit,
    onResizeWidthChanged: (String) -> Unit,
    onResizeHeightChanged: (String) -> Unit,
    onToggleMaintainAspect: () -> Unit,
    onReset: () -> Unit,
    onConvert: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text(
            text = "Image Converter",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Pick image button
        Button(
            onClick = onPickImage,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Photo, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (selectedImageUri != null) "Change Image" else "Select Image")
        }

        if (selectedImageUri != null) {
            Spacer(modifier = Modifier.height(12.dp))

            // Image info
            InfoCard(
                title = "Original",
                lines = buildList {
                    originalDimensions?.let { add("${it.first} × ${it.second}") }
                    originalFileSize?.let { add(formatBytes(it)) }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Format selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Output Format",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalButton(onClick = onCycleFormat) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(targetFormat.extension.uppercase())
                    }
                }
            }

            // Quality slider
            Text(
                text = "Quality: $quality",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Slider(
                value = quality.toFloat(),
                onValueChange = { onQualityChanged(it.toInt()) },
                valueRange = 0f..100f,
                steps = 99
            )

            // Resize options
            Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Resize (optional)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = if (resizeWidth > 0) resizeWidth.toString() else "",
                            onValueChange = onResizeWidthChanged,
                            label = { Text("Width") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp).align(Alignment.CenterVertically))
                        OutlinedTextField(
                            value = if (resizeHeight > 0) resizeHeight.toString() else "",
                            onValueChange = onResizeHeightChanged,
                            label = { Text("Height") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = maintainAspect,
                            onCheckedChange = { onToggleMaintainAspect() }
                        )
                        Text(
                            text = "Maintain aspect ratio",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Convert button
            Button(
                onClick = onConvert,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isConverting
            ) {
                if (isConverting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Converting...")
                } else {
                    Text("Convert")
                }
            }

            // Result
            if (conversionResult != null) {
                Spacer(modifier = Modifier.height(16.dp))
                when (conversionResult) {
                    is ConversionResult.Success -> {
                        val result = conversionResult as ConversionResult.Success
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Conversion Complete",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Size: ${formatBytes(result.newSize)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                originalFileSize?.let { origSize ->
                                    val ratio = ImageConverterViewModel().computeCompressionRatio(origSize, result.newSize)
                                    Text(
                                        text = "Change: ${"%.1f".format(ratio)}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (ratio > 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    is ConversionResult.Error -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = "Error: ${conversionResult.message}",
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        } else {
            // No image selected state
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Photo,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Select an image to convert",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Supports PNG, JPEG, WEBP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Reset button
        if (selectedImageUri != null) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Over")
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, lines: List<String>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            lines.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        else -> "%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
