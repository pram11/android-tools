package com.armyknife.tools.features.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CompassScreen(viewModel: CompassViewModel = viewModel()) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(SensorManager::class.java) as? SensorManager

    val accelerometer by remember {
        mutableStateOf(sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))
    }
    val magnetometer by remember {
        mutableStateOf(sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD))
    }

    var accelValues by remember { mutableStateOf(FloatArray(3)) }
    var magValues by remember { mutableStateOf(FloatArray(3)) }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> accelValues = event.values.clone()
                    Sensor.TYPE_MAGNETIC_FIELD -> magValues = event.values.clone()
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    // Register sensor listeners
    LaunchedEffect(accelerometer, magnetometer) {
        accelerometer?.let { sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME) }
        magnetometer?.let { sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    // Cleanup on dispose
    DisposableEffect(sensorManager, accelerometer, magnetometer) {
        onDispose {
            sensorManager?.unregisterListener(sensorListener)
        }
    }

    // Calculate bearing from sensor data
    LaunchedEffect(accelValues, magValues) {
        val r = FloatArray(9)
        if (SensorManager.getRotationMatrix(r, null, accelValues, magValues)) {
            viewModel.updateBearing(viewModel.calculateBearing(r))
        }
    }

    val uiState = viewModel.uiState

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Compass",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        CompassRose(bearing = uiState.bearing)

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.direction,
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${uiState.bearing.toInt()}°",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (accelerometer == null || magnetometer == null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No compass sensor available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun CompassRose(
    bearing: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val canvasSize = 240.dp
    val canvasSizePx = with(density) { canvasSize.toPx() }
    val center = canvasSizePx / 2f
    val radius = center - with(density) { 20.dp.toPx() }
    val strokeWidth = with(density) { 2.dp.toPx() }
    val needleWidth = with(density) { 6.dp.toPx() }
    val dotRadius = with(density) { 4.dp.toPx() }
    val centerDotRadius = with(density) { 6.dp.toPx() }

    val bearingRad = Math.toRadians(bearing.toDouble()).toFloat()

    // Hoist Compose-dependent values outside Canvas draw scope
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier.size(canvasSize)) {
        // Outer circle
        drawCircle(
            color = outlineColor,
            radius = radius,
            style = Stroke(width = strokeWidth)
        )

        // Inner circle
        drawCircle(
            color = surfaceColor,
            radius = radius * 0.5f
        )

        // Cardinal directions
        val cardinalPoints = listOf(
            Triple(0f, "N", Color(0xFFF44336)),
            Triple(90f, "E", onSurfaceVariantColor),
            Triple(180f, "S", onSurfaceVariantColor),
            Triple(270f, "W", onSurfaceVariantColor)
        )

        cardinalPoints.forEach { (angle, _, color) ->
            val angleRad = Math.toRadians(angle.toDouble()).toFloat()
            val x = center + radius * 0.75f * sin(angleRad)
            val y = center - radius * 0.75f * cos(angleRad)

            drawCircle(
                color = color,
                radius = dotRadius,
                center = Offset(x, y)
            )
        }

        // Needle
        val needleLength = radius * 0.85f
        val northTip = Offset(
            center + needleLength * sin(bearingRad),
            center - needleLength * cos(bearingRad)
        )
        val southTip = Offset(
            center - needleLength * sin(bearingRad),
            center + needleLength * cos(bearingRad)
        )
        val centerOffset = Offset(center, center)

        // South half (blue)
        drawLine(
            color = Color(0xFF2196F3),
            start = centerOffset,
            end = southTip,
            strokeWidth = needleWidth
        )

        // North half (red)
        drawLine(
            color = Color(0xFFF44336),
            start = centerOffset,
            end = northTip,
            strokeWidth = needleWidth
        )

        // Center dot
        drawCircle(
            color = onSurfaceColor,
            radius = centerDotRadius,
            center = centerOffset
        )
    }
}
