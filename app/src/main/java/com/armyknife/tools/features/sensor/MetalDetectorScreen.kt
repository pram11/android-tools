package com.armyknife.tools.features.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateColorAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MetalDetectorScreen(viewModel: MetalDetectorViewModel = viewModel()) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(SensorManager::class.java)
    val magSensor by remember {
        mutableStateOf(sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD))
    }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                    viewModel.onMagneticFieldChanged(
                        event.values[0], event.values[1], event.values[2]
                    )
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    LaunchedEffect(magSensor) {
        magSensor?.let {
            sensorManager?.registerListener(
                sensorListener, it, SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    DisposableEffect(sensorManager, magSensor) {
        onDispose {
            sensorManager?.unregisterListener(sensorListener)
        }
    }

    // Auto-start calibration on first launch
    LaunchedEffect(Unit) {
        if (!viewModel.uiState.isCalibrated) {
            viewModel.startCalibration()
        }
    }

    val uiState by viewModel.uiState

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Metal Detector",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (magSensor == null) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "No Magnetic Sensor",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This device does not have a magnetometer sensor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            return
        }

        // Calibration indicator
        if (!uiState.isCalibrated) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Calibrating… move device slowly",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Alert banner
        if (uiState.isDetecting) {
            val alertColor = getAlertColor(uiState.alert)
            val animatedColor by animateColorAsState(alertColor, label = "alertColor")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = animatedColor.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚠ DETECTED — ${uiState.alert.name}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = alertColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Gauge
        MetalGauge(deviation = uiState.deviation, baseline = uiState.baseline)

        Spacer(modifier = Modifier.height(16.dp))

        // Readout
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${uiState.fieldIntensity.toInt()} µT",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Deviation: ${uiState.deviation.toInt()} µT",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Card(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Baseline", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "${uiState.baseline.toInt()} µT",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
            Card(modifier = Modifier.weight(1f).padding(start = 4.dp)) {
                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Peak", style = MaterialTheme.typography.labelMedium)
                    Text(
                        text = "${uiState.peakIntensity.toInt()} µT",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History waveform
        AnimatedVisibility(visible = uiState.history.isNotEmpty() && uiState.history.size > 2, enter = fadeIn(), exit = fadeOut()) {
            DeviationWaveform(history = uiState.history, modifier = Modifier.fillMaxWidth().height(80.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Controls
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = viewModel::startCalibration) {
                Text("Recalibrate")
            }
            OutlinedButton(onClick = viewModel::resetPeak) {
                Text("Reset Peak")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Detects ferrous metal via magnetic field anomalies.\nKeep away from strong magnets.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetalGauge(deviation: Float, baseline: Float) {
    val density = LocalDensity.current
    val canvasSize = 240.dp
    val canvasSizePx = with(density) { canvasSize.toPx() }
    val center = canvasSizePx / 2f
    val radius = center - with(density) { 24.dp.toPx() }
    val needleWidth = with(density) { 4.dp.toPx() }
    val arcThickness = with(density) { 16.dp.toPx() }

    // Normalize deviation: ±50% of baseline → full sweep
    val range = if (baseline > 0) baseline * 0.5f else 1f
    val normalized = kotlin.math.max(0f, kotlin.math.min(1f, (deviation / range + 1f) / 2f))

    val sweepAngle = 270f
    val startAngle = -135f

    val color = if (deviation == 0f)
        MaterialTheme.colorScheme.outlineVariant
    else
        getAlertColor(
            when {
                kotlin.math.abs(deviation / baseline) < 0.05f -> AlertLevel.NONE
                kotlin.math.abs(deviation / baseline) < 0.15f -> AlertLevel.WEAK
                kotlin.math.abs(deviation / baseline) < 0.30f -> AlertLevel.MODERATE
                kotlin.math.abs(deviation / baseline) < 0.60f -> AlertLevel.STRONG
                else -> AlertLevel.EXTREME
            }
        )

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
        drawArc(
            color = surfaceColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = arcOffset,
            size = arcSize
        )

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

        drawCircle(
            color = onSurfaceColor,
            radius = with(density) { 8.dp.toPx() },
            center = Offset(center, center)
        )

        // Center zero mark
        val zeroAngle = startAngle + sweepAngle / 2f
        val zeroRad = Math.toRadians(zeroAngle.toDouble()).toFloat()
        drawLine(
            color = MaterialTheme.colorScheme.primary,
            start = Offset(
                center + radius * 0.7f * kotlin.math.cos(zeroRad),
                center + radius * 0.7f * kotlin.math.sin(zeroRad)
            ),
            end = Offset(
                center + radius * 0.85f * kotlin.math.cos(zeroRad),
                center + radius * 0.85f * kotlin.math.sin(zeroRad)
            ),
            strokeWidth = with(density) { 3.dp.toPx() }
        )
    }
}

@Composable
private fun DeviationWaveform(history: List<Float>, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val color = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val maxDev = kotlin.math.max(1f, history.maxOf { kotlin.math.abs(it) }) * 1.2f

    Canvas(modifier = modifier) {
        if (history.size < 2) return@Canvas
        val padding = with(density) { 4.dp.toPx() }
        val width = size.width - padding * 2
        val height = size.height - padding * 2
        val midY = size.height / 2f
        val stepX = width / (history.size - 1)

        // Zero line
        drawLine(
            color = onSurfaceColor.copy(alpha = 0.3f),
            start = Offset(0f, midY),
            end = Offset(size.width, midY),
            strokeWidth = 1f
        )

        // Deviation path
        val path = Path()
        var first = true
        history.forEachIndexed { i, dev ->
            val x = padding + i * stepX
            val norm = kotlin.math.max(-1f, kotlin.math.min(1f, dev / maxDev))
            val y = midY - norm * height / 2f
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
private fun getAlertColor(level: AlertLevel): Color {
    return when (level) {
        AlertLevel.NONE -> Color(0xFF4CAF50)
        AlertLevel.WEAK -> Color(0xFFFFEB3B)
        AlertLevel.MODERATE -> Color(0xFFFF9800)
        AlertLevel.STRONG -> Color(0xFFF44336)
        AlertLevel.EXTREME -> Color(0xFF9C27B0)
    }
}
