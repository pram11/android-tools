package com.armyknife.tools.features.sensor

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SoundMeterScreen(viewModel: SoundMeterViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    // Auto-start when permission granted and not already recording
    LaunchedEffect(hasPermission) {
        if (hasPermission && !uiState.isRecording) {
            viewModel.startRecording()
        }
    }

    SoundMeterContent(
        uiState = uiState,
        hasPermission = hasPermission,
        onGrantPermission = {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        },
        onStopRecording = viewModel::stopRecording,
        onResetPeak = viewModel::resetPeak
    )
}

@Composable
private fun SoundMeterContent(
    uiState: SoundMeterUiState,
    hasPermission: Boolean,
    onGrantPermission: () -> Unit,
    onStopRecording: () -> Unit,
    onResetPeak: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sound Meter",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Permission not granted
        if (!hasPermission) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Microphone Access Required",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This tool needs microphone access to measure ambient sound levels.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onGrantPermission) {
                        Text("Grant Permission")
                    }
                }
            }
            return
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gauge
        SoundLevelGauge(currentDb = uiState.currentDb, levelCategory = uiState.levelCategory)

        Spacer(modifier = Modifier.height(16.dp))

        // dB Readout
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${uiState.currentDb.toInt()} dB",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.levelCategory,
                    style = MaterialTheme.typography.titleMedium,
                    color = getCategoryColor(uiState.levelCategory)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Peak + Amplitude
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Card(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Peak", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "${uiState.peakDb.toInt()} dB",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            Card(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Amplitude", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "${uiState.rawAmplitude}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History waveform
        AnimatedVisibility(visible = uiState.history.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
            WaveformChart(history = uiState.history, modifier = Modifier.fillMaxWidth().height(80.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Controls
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            if (uiState.isRecording) {
                FilledTonalButton(onClick = onStopRecording) {
                    Text("Stop")
                }
            }
            OutlinedButton(onClick = onResetPeak) {
                Text("Reset Peak")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Relative dB (0 = full scale)\nNot calibrated to true SPL",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SoundLevelGauge(currentDb: Float, levelCategory: String) {
    val density = LocalDensity.current
    val canvasSize = 240.dp
    val canvasSizePx = with(density) { canvasSize.toPx() }
    val center = canvasSizePx / 2f
    val radius = center - with(density) { 24.dp.toPx() }
    val needleWidth = with(density) { 4.dp.toPx() }
    val arcThickness = with(density) { 16.dp.toPx() }

    val dbMin = -120f
    val dbMax = 10f
    val normalized = kotlin.math.max(0f, kotlin.math.min(1f, (currentDb - dbMin) / (dbMax - dbMin)))

    val sweepAngle = 270f
    val startAngle = -135f

    val color = getCategoryColor(levelCategory)
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    val arcSize = Size(
        (radius + arcThickness / 2f) * 2,
        (radius + arcThickness / 2f) * 2
    )
    val arcOffset = Offset(
        center - radius - arcThickness / 2f,
        center - radius - arcThickness / 2f
    )

    Canvas(modifier = Modifier.size(canvasSize)) {
        // Background arc
        drawArc(
            color = surfaceColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = arcOffset,
            size = arcSize
        )

        // Filled arc
        val currentSweep = normalized * sweepAngle
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = currentSweep,
            useCenter = false,
            topLeft = arcOffset,
            size = arcSize
        )

        // Needle
        val needleAngle = startAngle + currentSweep
        val angleRad = Math.toRadians(needleAngle.toDouble()).toFloat()
        val needleLen = radius * 0.8f
        drawLine(
            color = onSurfaceColor,
            start = Offset(center, center),
            end = Offset(
                center + needleLen * kotlin.math.cos(angleRad),
                center + needleLen * kotlin.math.sin(angleRad)
            ),
            strokeWidth = needleWidth
        )

        // Center dot
        drawCircle(
            color = onSurfaceColor,
            radius = with(density) { 8.dp.toPx() },
            center = Offset(center, center)
        )

        // Tick marks
        for (i in 0..10) {
            val tickNorm = i / 10f
            val tickAngle = startAngle + tickNorm * sweepAngle
            val tickRad = Math.toRadians(tickAngle.toDouble()).toFloat()
            val innerR = radius * 0.75f
            val outerR = radius * 0.82f
            drawLine(
                color = onSurfaceColor,
                start = Offset(
                    center + innerR * kotlin.math.cos(tickRad),
                    center + innerR * kotlin.math.sin(tickRad)
                ),
                end = Offset(
                    center + outerR * kotlin.math.cos(tickRad),
                    center + outerR * kotlin.math.sin(tickRad)
                ),
                strokeWidth = if (i % 5 == 0) with(density) { 2.dp.toPx() } else with(density) { 1.dp.toPx() }
            )
        }
    }
}

@Composable
private fun WaveformChart(history: List<Float>, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val color = MaterialTheme.colorScheme.primary
    val dbMin = -120f
    val dbMax = 10f

    Canvas(modifier = modifier) {
        if (history.size < 2) return@Canvas
        val padding = with(density) { 4.dp.toPx() }
        val width = size.width - padding * 2
        val height = size.height - padding * 2
        val stepX = width / (history.size - 1)

        val path = Path()
        var first = true
        history.forEachIndexed { i, db ->
            val x = padding + i * stepX
            val norm = kotlin.math.max(0f, kotlin.math.min(1f, (db - dbMin) / (dbMax - dbMin)))
            val y = size.height - padding - norm * height
            if (first) {
                path.moveTo(x, y)
                first = false
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(path, color = color, style = Stroke(width = with(density) { 2.dp.toPx() }))
    }
}

@Composable
private fun getCategoryColor(category: String): Color {
    return when (category) {
        "Silent" -> Color(0xFF4CAF50)
        "Very Quiet" -> Color(0xFF8BC34A)
        "Quiet" -> Color(0xFFCDDC39)
        "Moderate" -> Color(0xFFFFEB3B)
        "Loud" -> Color(0xFFFF9800)
        "Very Loud" -> Color(0xFFF44336)
        "Dangerous" -> Color(0xFF9C27B0)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
