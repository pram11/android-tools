package com.armyknife.tools.features.sensor

import android.location.Location
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
import com.google.android.gms.location.*
import kotlinx.coroutines.tasks.await

/**
 * Speedometer screen using GPS Location for speed tracking.
 */
@Composable
fun SpeedometerScreen(viewModel: SpeedometerViewModel = viewModel()) {
    val context = LocalContext.current
    val uiState = viewModel.uiState

    var gpsAvailable by remember { mutableStateOf(true) }
    var permissionGranted by remember { mutableStateOf(false) }

    // FusedLocationProviderClient setup
    val locationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Check for location permission
    LaunchedEffect(Unit) {
        try {
            val location = locationClient.lastLocation.await()
            if (location != null) {
                viewModel.onSpeedChanged(location.speed)
                viewModel.onAccuracyChanged(location.accuracy)
            }
            permissionGranted = true
        } catch (e: Exception) {
            // Permission denied or no location available
            gpsAvailable = false
        }
    }

    // Location updates
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    viewModel.onSpeedChanged(location.speed)
                    viewModel.onAccuracyChanged(location.accuracy)
                }
            }
        }
    }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            val request = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                1000L // 1 second interval
            ).build()
            try {
                locationClient.requestLocationUpdates(request, locationCallback, null)
            } catch (e: Exception) {
                gpsAvailable = false
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                locationClient.removeLocationUpdates(locationCallback)
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Speedometer",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SpeedometerGauge(
            currentSpeed = uiState.currentSpeedKmh,
            unit = uiState.selectedUnit,
            isMoving = uiState.isMoving
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Speed display card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%.1f", uiState.displaySpeed(uiState.selectedUnit)),
                    style = MaterialTheme.typography.displayLarge,
                    color = if (uiState.isMoving) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = uiState.selectedUnit.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Max speed card
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Max Speed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${uiState.maxSpeedKmh.toInt()} km/h",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // GPS accuracy indicator
        if (uiState.accuracy > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "GPS Accuracy: ±${uiState.accuracy.toInt()}m",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Unit selector
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpeedUnit.entries.forEach { unit ->
                FilterChip(
                    selected = unit == uiState.selectedUnit,
                    onClick = { viewModel.switchUnit(unit) },
                    label = { Text(unit.label) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset button
        Button(onClick = { viewModel.reset() }) {
            Text("Reset")
        }

        // GPS status
        if (!gpsAvailable) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "GPS location unavailable or permission denied",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun SpeedometerGauge(
    currentSpeed: Float,
    unit: SpeedUnit,
    isMoving: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val canvasSize = 240.dp
    val canvasSizePx = with(density) { canvasSize.toPx() }
    val center = canvasSizePx / 2f
    val radius = center - with(density) { 24.dp.toPx() }
    val arcThickness = with(density) { 16.dp.toPx() }
    val needleWidth = with(density) { 4.dp.toPx() }

    // Speed range based on unit
    val maxSpeed = when (unit) {
        SpeedUnit.KMH -> 200f
        SpeedUnit.MPH -> 120f
        SpeedUnit.KNOTS -> 100f
    }
    val displaySpeed = if (unit == SpeedUnit.KMH) currentSpeed
    else if (unit == SpeedUnit.MPH) currentSpeed / 1.60934f
    else currentSpeed / 1.852f

    val normalized = kotlin.math.max(0f, kotlin.math.min(1f, displaySpeed / maxSpeed))

    val sweepAngle = 270f
    val startAngle = -135f

    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val needleColor = if (isMoving) Color(0xFFF44336) else outlineColor

    val arcSize = androidx.compose.ui.geometry.Size(
        (radius + arcThickness / 2f) * 2,
        (radius + arcThickness / 2f) * 2
    )
    val arcOffset = Offset(
        center - radius - arcThickness / 2f,
        center - radius - arcThickness / 2f
    )

    Canvas(modifier = modifier.size(canvasSize)) {
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
        if (normalized > 0) {
            val currentSweep = normalized * sweepAngle
            val arcColor = when {
                normalized < 0.5f -> primaryColor
                normalized < 0.75f -> Color(0xFFFFA000)
                else -> Color(0xFFF44336)
            }
            drawArc(
                color = arcColor,
                startAngle = startAngle,
                sweepAngle = currentSweep,
                useCenter = false,
                topLeft = arcOffset,
                size = arcSize
            )
        }

        // Tick marks
        for (i in 0..10) {
            val tickNorm = i / 10f
            val tickAngle = startAngle + tickNorm * sweepAngle
            val tickRad = Math.toRadians(tickAngle.toDouble()).toFloat()
            val innerR = radius * 0.85f
            val outerR = radius * 0.92f
            drawLine(
                color = outlineColor,
                start = Offset(
                    center + innerR * kotlin.math.cos(tickRad),
                    center + innerR * kotlin.math.sin(tickRad)
                ),
                end = Offset(
                    center + outerR * kotlin.math.cos(tickRad),
                    center + outerR * kotlin.math.sin(tickRad)
                ),
                strokeWidth = if (i % 5 == 0) with(density) { 2.dp.toPx() }
                else with(density) { 1.dp.toPx() }
            )
        }

        // Needle
        val needleAngle = startAngle + normalized * sweepAngle
        val angleRad = Math.toRadians(needleAngle.toDouble()).toFloat()
        val needleLen = radius * 0.75f
        drawLine(
            color = needleColor,
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
            radius = with(density) { 6.dp.toPx() },
            center = Offset(center, center)
        )
    }
}
