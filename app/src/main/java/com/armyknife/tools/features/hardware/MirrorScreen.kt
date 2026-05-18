package com.armyknife.tools.features.hardware

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flip
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

@Composable
fun MirrorScreen(viewModel: MirrorViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionGranted = isGranted
    }

    LaunchedEffect(Unit) {
        permissionGranted = checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    if (!permissionGranted) {
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
                text = "Mirror needs camera access to display a live front-facing view.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Grant Permission")
            }
        }
    } else {
        MirrorCameraView(viewModel = viewModel, context = context, lifecycleOwner = lifecycleOwner)
    }
}

@Composable
private fun MirrorCameraView(
    viewModel: MirrorViewModel,
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner
) {
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    // Set mirror effect via scale
                    previewView.implementationMode = androidx.camera.view.PreviewView.ImplementationMode.PERFORMANCE
                    previewView.scaleType = androidx.camera.view.PreviewView.ScaleType.FILL_CENTER
                    if (viewModel.flipHorizontal) {
                        previewView.scaleX = -1f
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val provider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val selector = CameraSelector.DEFAULT_FRONT_CAMERA

                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview
                            )
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }, mainExecutor)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Flip button overlay
        FilledTonalIconButton(
            onClick = { viewModel.toggleFlipHorizontal() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp)
        ) {
            Icon(
                Icons.Default.Flip,
                contentDescription = if (viewModel.flipHorizontal) "Un-mirror" else "Mirror",
                modifier = Modifier.size(28.dp)
            )
        }

        // Label
        Surface(
            shape = MaterialTheme.shapes.small,
            tonalElevation = 2.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        ) {
            Text(
                text = if (viewModel.flipHorizontal) "Mirror" else "Live View",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
