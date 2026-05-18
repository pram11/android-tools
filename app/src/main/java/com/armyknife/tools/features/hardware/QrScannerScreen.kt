package com.armyknife.tools.features.hardware

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun QrScannerScreen(viewModel: QrScannerViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val keyboardController = LocalSoftwareKeyboardController.current

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
                text = "QR Scanner needs camera access to scan barcodes and QR codes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Grant Permission")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = {
                // Allow switching to generator even without permission
                viewModel.switchMode(QrScannerMode.GENERATE)
            }) {
                Text("Skip & Use QR Generator")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TabRow(
                selectedTabIndex = if (viewModel.uiState.mode == QrScannerMode.SCAN) 0 else 1,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = viewModel.uiState.mode == QrScannerMode.SCAN,
                    onClick = { viewModel.switchMode(QrScannerMode.SCAN) }
                ) {
                    Text("Scan")
                }
                Tab(
                    selected = viewModel.uiState.mode == QrScannerMode.GENERATE,
                    onClick = { viewModel.switchMode(QrScannerMode.GENERATE) }
                ) {
                    Text("Generate")
                }
            }
        }
    ) { innerPadding ->
        when (viewModel.uiState.mode) {
            QrScannerMode.SCAN -> ScanTab(
                modifier = Modifier.padding(innerPadding),
                context = context,
                lifecycleOwner = lifecycleOwner,
                viewModel = viewModel
            )
            QrScannerMode.GENERATE -> GenerateTab(
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel,
                context = context,
                keyboardController = keyboardController
            )
        }
    }
}

@Composable
private fun ScanTab(
    modifier: Modifier = Modifier,
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    viewModel: QrScannerViewModel
) {
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val mainExecutor = remember { ContextCompat.getMainExecutor(context) }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also { previewView ->
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val provider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val barcodeOptions = BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                            .build()
                        val scanner = BarcodeScanning.getClient(barcodeOptions)

                        val analysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        analysis.setAnalyzer(cameraExecutor) { proxy ->
                            val mediaImage = proxy.image
                            if (mediaImage != null) {
                                val image = InputImage.fromMediaImage(
                                    mediaImage,
                                    proxy.imageInfo.rotationDegrees
                                )
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        barcodes.forEach { barcode ->
                                            barcode.displayValue?.let { value ->
                                                viewModel.onBarcodeDetected(
                                                    value,
                                                    barcode.format.toString()
                                                )
                                            }
                                        }
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(ctx, "Scan failed", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnCompleteListener {
                                        proxy.close()
                                    }
                            }
                        }

                        val selector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                selector,
                                preview,
                                analysis
                            )
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }, mainExecutor)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Scanned results overlay
        if (viewModel.uiState.scanResults.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Results (${viewModel.uiState.scanResults.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(
                                onClick = { viewModel.clearResults() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Clear results",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        LazyColumn(
                            modifier = Modifier.heightIn(0.dp, 200.dp)
                        ) {
                            items(viewModel.uiState.scanResults, key = { it }) { result ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = result,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 2
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("scan", result))
                                            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Crosshair overlay
        CrosshairOverlay()
    }
}

@Composable
private fun CrosshairOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(240.dp, 160.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.medium
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                )
        )
    }
}

@Composable
private fun GenerateTab(
    modifier: Modifier = Modifier,
    viewModel: QrScannerViewModel,
    context: Context,
    keyboardController: androidx.compose.ui.platform.SoftwareKeyboardController?
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "QR Code Generator",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = viewModel.qrInput,
            onValueChange = { viewModel.onQrInputChanged(it) },
            label = { Text("Enter text or URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 4,
            trailingIcon = {
                if (viewModel.qrInput.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQrInputChanged("") }) {
                        Text("✕", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                keyboardController?.hide()
                viewModel.generateQrBitmap()
            },
            enabled = viewModel.qrInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generate QR Code")
        }

        Spacer(modifier = Modifier.height(24.dp))

        viewModel.uiState.generatedQrBitmap?.let { bitmap ->
            QrCodeImage(bitmap = bitmap)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("qr", viewModel.qrInput))
                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy text")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Text")
                }
            }
        }

        if (viewModel.qrInput.isNotBlank() && viewModel.uiState.generatedQrBitmap == null) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Enter text and tap Generate",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QrCodeImage(bitmap: Bitmap) {
    AndroidView(
        factory = { ctx ->
            ImageView(ctx).apply {
                setImageBitmap(bitmap)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
        },
        modifier = Modifier.size(300.dp)
    )
}
