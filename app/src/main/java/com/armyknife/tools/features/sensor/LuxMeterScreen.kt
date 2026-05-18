package com.armyknife.tools.features.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LuxMeterScreen(viewModel: LuxMeterViewModel = viewModel()) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(SensorManager::class.java)
    val lightSensor by remember {
        mutableStateOf(sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT))
    }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_LIGHT) {
                    viewModel.onLuxChanged(event.values[0])
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    LaunchedEffect(lightSensor) {
        lightSensor?.let {
            sensorManager?.registerListener(
                sensorListener, it, SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    DisposableEffect(sensorManager, lightSensor) {
        onDispose {
            sensorManager?.unregisterListener(sensorListener)
        }
    }

    val uiState by viewModel.uiState

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Lux Meter",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (lightSensor == null) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "No Light Sensor",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This device does not have an ambient light sensor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            return
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gauge
        LuxGauge(currentLux = uiState.lux, lightLevel = uiState.lightLevel)

        Spacer(modifier = Modifier.height(16.dp))

        // lux Readout
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${uiState.lux.toInt()} lux",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = uiState.lightLevel,
                    style = MaterialTheme.typography.titleMedium,
                    color = getLuxColor(uiState.lightLevel)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Peak
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Peak Lux", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "${uiState.peakLux.toInt()} lux",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = viewModel::resetPeak) {
                    Text("Reset Peak")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History waveform
        AnimatedVisibility(visible = uiState.history.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
            LuxWaveform(history = uiState.history, modifier = Modifier.fillMaxWidth().height(80.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Reference table
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Reference Levels", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                ReferenceRow("Pitch Black", "0 lux", Color(0xFF9E9E9E))
                ReferenceRow("Dim room", "50 lux", Color(0xFF8BC34A))
                ReferenceRow("Office", "500 lux", Color(0xFFFFEB3B))
                ReferenceRow("Daylight", "1 000 lux", Color(0xFFFF9800))
                ReferenceRow("Direct Sun", "25 000+ lux", Color(0xFFF44336))
            }
        }
    }
}

@Composable
private fun ReferenceRow(label: String, value: String, dotColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = dotColor)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LuxGauge(currentLux: Float, lightLevel: String) {
    val density = LocalDensity.current
    val canvasSize = 240.dp
    val canvasSizePx = with(density) { canvasSize.toPx() }
    val center = canvasSizePx / 2f
    val radius = center - with(density) { 24.dp.toPx() }
    val needleWidth = with(density) { 4.dp.toPx() }
    val arcThickness = with(density) { 16.dp.toPx() }

    // Log scale: 0.1 to 50000 lux
    val minLog = Math.log10(0.1).toFloat()
    val maxLog = Math.log10(50000f).toFloat()
    val logVal = Math.log10(kotlin.math.max(0.1f, currentLux)).toFloat()
    val normalized = kotlin.math.max(0f, kotlin.math.min(1f, (logVal - minLog) / (maxLog - minLog)))

    val sweepAngle = 270f
    val startAngle = -135f

    val color = getLuxColor(lightLevel)
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

        // Ticks with log labels
        val tickValues = listOf(1f, 10f, 100f, 1000f, 10000f, 50000f)
        tickValues.forEach { tv ->
            val tvLog = Math.log10(tv).toFloat()
            val tickNorm = (tvLog - minLog) / (maxLog - minLog)
            val tickAngle = startAngle + tickNorm * sweepAngle
            val tickRad = Math.toRadians(tickAngle.toDouble()).toFloat()
            val innerR = radius * 0.78f
            val outerR = radius * 0.85f
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
                strokeWidth = with(density) { 1.5.dp.toPx() }
            )
        }
    }
}

@Composable
private fun LuxWaveform(history: List<Float>, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val color = MaterialTheme.colorScheme.primary
    val minLux = 0f
    val maxLux = kotlin.math.max(100f, history.maxOrNull() ?: 100f) * 1.1f

    Canvas(modifier = modifier) {
        if (history.size < 2) return@Canvas
        val padding = with(density) { 4.dp.toPx() }
        val width = size.width - padding * 2
        val height = size.height - padding * 2
        val stepX = width / (history.size - 1)

        val path = Path()
        var first = true
        history.forEachIndexed { i, lux ->
            val x = padding + i * stepX
            val norm = kotlin.math.max(0f, kotlin.math.min(1f, lux / maxLux))
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
private fun getLuxColor(level: String): Color {
    return when (level) {
        "Pitch Black" -> Color(0xFF9E9E9E)
        "Very Dark" -> Color(0xFF616161)
        "Dark" -> Color(0xFF455A64)
        "Dim" -> Color(0xFF8BC34A)
        "Low Light" -> Color(0xFFCDDC39)
        "Office" -> Color(0xFFFFEB3B)
        "Daylight" -> Color(0xFFFF9800)
        "Overcast" -> Color(0xFFFF5722)
        "Bright" -> Color(0xFFF44336)
        "Direct Sun" -> Color(0xFF9C27B0)
        "Extreme" -> Color(0xFFE91E63)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
