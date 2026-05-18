package com.armyknife.tools.features.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BubbleLevelScreen(viewModel: BubbleLevelViewModel = viewModel()) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(SensorManager::class.java)
    val accelerometer by remember {
        mutableStateOf(sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))
    }

    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    viewModel.onAccelerometerChanged(
                        event.values[0], event.values[1], event.values[2]
                    )
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    LaunchedEffect(accelerometer) {
        accelerometer?.let {
            sensorManager?.registerListener(
                sensorListener, it, SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    DisposableEffect(sensorManager, accelerometer) {
        onDispose {
            sensorManager?.unregisterListener(sensorListener)
        }
    }

    val uiState = viewModel.uiState

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bubble Level",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        BubbleLevelView(
            uiState.isLevel,
            uiState.bubbleX,
            uiState.bubbleY,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (uiState.isLevel) "✓ Level" else "✗ Tilted",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (uiState.isLevel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("X: ${uiState.tiltX.toInt()}°", style = MaterialTheme.typography.bodyLarge)
                    Text("Y: ${uiState.tiltY.toInt()}°", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        if (accelerometer == null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No accelerometer available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun BubbleLevelView(
    isLevel: Boolean,
    bubbleX: Float,
    bubbleY: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val canvasSize = 280.dp
    val canvasSizePx = with(density) { canvasSize.toPx() }
    val center = canvasSizePx / 2f
    val radius = center - with(density) { 20.dp.toPx() }

    val animatedBubbleX by animateFloatAsState(targetValue = bubbleX, label = "bubbleX")
    val animatedBubbleY by animateFloatAsState(targetValue = bubbleY, label = "bubbleY")

    val bubbleCenterX = animatedBubbleX * canvasSizePx
    val bubbleCenterY = animatedBubbleY * canvasSizePx

    val bubbleRadius = with(density) { 16.dp.toPx() }
    val crosshairStroke = with(density) { 1.5.dp.toPx() }
    val outlineStroke = with(density) { 2.dp.toPx() }

    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier.size(canvasSize)) {
        // Outer circle
        drawCircle(
            color = outlineColor,
            radius = radius,
            style = Stroke(width = outlineStroke)
        )

        // Inner circle (wells)
        drawCircle(
            color = surfaceColor,
            radius = radius * 0.9f
        )

        // Concentric level rings
        drawCircle(
            color = outlineColor.copy(alpha = 0.3f),
            radius = radius * 0.5f,
            style = Stroke(width = crosshairStroke)
        )
        drawCircle(
            color = outlineColor.copy(alpha = 0.3f),
            radius = radius * 0.25f,
            style = Stroke(width = crosshairStroke)
        )

        // Crosshair lines
        drawLine(
            color = outlineColor.copy(alpha = 0.3f),
            start = Offset(center, 0f),
            end = Offset(center, canvasSizePx),
            strokeWidth = crosshairStroke
        )
        drawLine(
            color = outlineColor.copy(alpha = 0.3f),
            start = Offset(0f, center),
            end = Offset(canvasSizePx, center),
            strokeWidth = crosshairStroke
        )

        // Bubble
        val bubbleColor = if (isLevel) Color(0xFF4CAF50) else primaryColor
        drawCircle(
            color = bubbleColor,
            radius = bubbleRadius,
            center = Offset(bubbleCenterX, bubbleCenterY)
        )

        // Center dot
        drawCircle(
            color = onSurfaceColor,
            radius = with(density) { 3.dp.toPx() },
            center = Offset(center, center)
        )
    }
}
