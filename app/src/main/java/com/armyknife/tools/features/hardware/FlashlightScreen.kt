package com.armyknife.tools.features.hardware

import android.content.Context
import android.hardware.camera2.CameraManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FlashlightScreen(viewModel: FlashlightViewModel = viewModel()) {
    val context = LocalContext.current
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }

    // Discover camera ID on launch
    LaunchedEffect(Unit) {
        try {
            cameraManager.cameraIdList.firstOrNull()?.let {
                viewModel.setCameraId(it)
            }
        } catch (e: Exception) {
            // Camera not available
        }
    }

    // Torch state sync with CameraManager
    LaunchedEffect(viewModel.isTorchOn, viewModel.cameraId) {
        if (viewModel.isSosMode) return@LaunchedEffect
        viewModel.cameraId?.let { id ->
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cameraManager.setTorchMode(id, viewModel.isTorchOn)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // SOS mode coroutine
    LaunchedEffect(viewModel.isSosMode) {
        if (viewModel.isSosMode && viewModel.cameraId != null) {
            runSOS(viewModel, cameraManager, context)
        }
    }

    FlashlightContent(
        viewModel = viewModel,
        onToggle = { viewModel.toggleTorch() },
        onSosToggle = {
            if (viewModel.isSosMode) {
                viewModel.disableSos()
            } else {
                viewModel.enableSos()
            }
        },
        hasCamera = viewModel.hasTorch
    )
}

private suspend fun runSOS(viewModel: FlashlightViewModel, cameraManager: CameraManager, context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
    val cameraId = viewModel.cameraId ?: return

    while (viewModel.isSosMode) {
        for (duration in FlashlightViewModel.sosPattern) {
            if (!viewModel.isSosMode) return
            try {
                cameraManager.setTorchMode(cameraId, true)
                delay(duration.toLong())
                cameraManager.setTorchMode(cameraId, false)
                delay(100L)
            } catch (e: Exception) {
                viewModel.disableSos()
                break
            }
        }
        // Pause between SOS cycles
        delay(1000L)
    }
}

@Composable
private fun FlashlightContent(
    viewModel: FlashlightViewModel,
    onToggle: () -> Unit,
    onSosToggle: () -> Unit,
    hasCamera: Boolean
) {
    if (!hasCamera) {
        NoFlashlightScreen()
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Torch status icon
        if (viewModel.isSosMode) {
            SosIndicator()
        } else if (viewModel.isTorchOn) {
            Icon(
                Icons.Default.BrightnessHigh,
                contentDescription = "Flashlight ON",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(96.dp)
            )
        } else {
            Icon(
                Icons.Default.BrightnessLow,
                contentDescription = "Flashlight OFF",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(96.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (viewModel.isSosMode) "SOS Active" else if (viewModel.isTorchOn) "Flashlight ON" else "Flashlight OFF",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Toggle button
        FilledTonalButton(
            onClick = onToggle,
            enabled = !viewModel.isSosMode,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Icon(
                imageVector = if (viewModel.isTorchOn) Icons.Default.BrightnessLow else Icons.Default.BrightnessHigh,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (viewModel.isTorchOn) "Turn Off" else "Turn On")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SOS button
        if (viewModel.isSosMode) {
            FilledTonalButton(
                onClick = onSosToggle,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop SOS")
            }
        } else {
            OutlinedButton(
                onClick = onSosToggle,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start SOS")
            }
        }
    }
}

@Composable
private fun SosIndicator() {
    var blinkOn by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            blinkOn = !blinkOn
            delay(400)
        }
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (blinkOn) MaterialTheme.colorScheme.error.copy(alpha = 0.3f) else Color.Transparent,
        modifier = Modifier.size(96.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SOS",
                style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun NoFlashlightScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.BrightnessLow,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(96.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Flashlight Available",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This device does not have a camera flashlight.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
