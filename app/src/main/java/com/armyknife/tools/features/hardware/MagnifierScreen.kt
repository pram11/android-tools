package com.armyknife.tools.features.hardware

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraFront
import androidx.compose.material.icons.filled.CameraRear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.concurrent.Executors

@Composable
fun MagnifierScreen(viewModel: MagnifierViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionGranted by remember { mutableStateOf(false) }
    var cameraHolder by remember { mutableStateOf<Camera?>(null) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionGranted = isGranted
    }

    LaunchedEffect(Unit) {
        permissionGranted = checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    // Sync zoom ratio to camera
    LaunchedEffect(viewModel.zoomRatio, cameraHolder) {
        cameraHolder?.cameraControl?.setZoomRatio(viewModel.zoomRatio)
    }

    // Rebind camera when facing changes
    var cameraFacing by remember { mutableStateOf(viewModel.cameraFacing) }
    LaunchedEffect(cameraFacing) {
        // Trigger rebuild by updating state
    }

    if (!permissionGranted) {
        PermissionScreen(
            onGrant = { permissionLauncher.launch(Manifest.permission.CAMERA) }
        )
    } else {
        MagnifierCameraView(
            viewModel = viewModel,
            context = context,
            lifecycleOwner = lifecycleOwner,
            cameraFacing = viewModel.cameraFacing,
            onCameraReady = { cameraHolder = it }
        )
    }
}

@Composable
private fun PermissionScreen(onGrant: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Magnifier needs camera access to display a zoomed live view.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Button(onClick = onGrant) {
            Text("Grant Permission")
        }
    }
}

@Composable
private fun MagnifierCameraView(
    viewModel: MagnifierViewModel,
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    cameraFacing: CameraFacing,
    onCameraReady: (Camera) -> Unit
) {
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val provider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val selector = CameraSelector.Builder()
                            .requireLensFacing(
                                if (cameraFacing == CameraFacing.FRONT) {
                                    CameraSelector.LENS_FACING_FRONT
                                } else {
                                    CameraSelector.LENS_FACING_BACK
                                }
                            )
                            .build()

                        try {
                            provider.unbindAll()
                            val camera = provider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview
                            )

                            // Use a reasonable default max zoom
                            viewModel.maxZoomRatio = 5f
                            viewModel.setZoomRatio(1f)

                            onCameraReady(camera)
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }, mainExecutor)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Zoom level indicator - center top
        Surface(
            shape = MaterialTheme.shapes.small,
            tonalElevation = 2.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        ) {
            Text(
                text = viewModel.zoomLevelString,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Slider(
                value = viewModel.zoomRatio,
                onValueChange = { viewModel.setZoomRatio(it) },
                valueRange = 1f..viewModel.maxZoomRatio,
                steps = ((viewModel.maxZoomRatio - 1) * 10).toInt().coerceAtLeast(0),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = { viewModel.resetZoom() },
                    enabled = viewModel.zoomRatio > 1.0f
                ) {
                    Text("1x", style = MaterialTheme.typography.labelLarge)
                }

                FilledTonalIconButton(onClick = { viewModel.flipCamera() }) {
                    Icon(
                        imageVector = if (viewModel.cameraFacing == CameraFacing.FRONT) {
                            Icons.Default.CameraRear
                        } else {
                            Icons.Default.CameraFront
                        },
                        contentDescription = "Flip camera"
                    )
                }
            }
        }
    }
}
