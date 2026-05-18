package com.armyknife.tools.features.media

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun VoiceRecorderScreen(viewModel: VoiceRecorderViewModel = viewModel()) {
    VoiceRecorderScreen(
        state = viewModel.state,
        durationMs = viewModel.durationMs,
        volumeLevel = viewModel.volumeLevel,
        sampleRate = viewModel.sampleRate,
        formatDuration = VoiceRecorderViewModel.formatDuration(viewModel.durationMs),
        estimatedSize = viewModel.estimateFileSize(viewModel.durationMs),
        onStart = { viewModel.startRecording() },
        onPause = { viewModel.pauseRecording() },
        onResume = { viewModel.resumeRecording() },
        onStop = { viewModel.stopRecording() },
        onCycleSampleRate = { viewModel.cycleSampleRate() },
        onReset = { viewModel.reset() }
    )
}

@Composable
fun VoiceRecorderScreen(
    state: RecordingState,
    durationMs: Long,
    volumeLevel: Int,
    sampleRate: Int,
    formatDuration: String,
    estimatedSize: Long,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onCycleSampleRate: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Voice Recorder",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Record audio with MediaRecorder",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Timer display
        Text(
            text = formatDuration,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            color = when (state) {
                RecordingState.RECORDING -> MaterialTheme.colorScheme.error
                RecordingState.PAUSED -> MaterialTheme.colorScheme.tertiary
                RecordingState.IDLE -> MaterialTheme.colorScheme.onSurface
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Volume meter
        LinearProgressIndicator(
            progress = volumeLevel / 100f,
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = if (volumeLevel > 80) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Volume: $volumeLevel%",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Control buttons
        when (state) {
            RecordingState.IDLE -> {
                Button(
                    onClick = onStart,
                    modifier = Modifier.size(72.dp),
                    shape = androidx.compose.foundation.shape.CircleShape
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Start recording",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
            RecordingState.RECORDING -> {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FilledTonalButton(
                        onClick = onPause,
                        modifier = Modifier.size(64.dp),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Icon(
                            Icons.Default.Pause,
                            contentDescription = "Pause recording",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Button(
                        onClick = onStop,
                        modifier = Modifier.size(64.dp),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "Stop recording",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
            RecordingState.PAUSED -> {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = onResume,
                        modifier = Modifier.size(64.dp),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Resume recording",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onError
                        )
                    }
                    FilledTonalButton(
                        onClick = onStop,
                        modifier = Modifier.size(64.dp),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "Stop recording",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Sample rate
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sample Rate")
                    FilledTonalIconButton(onClick = onCycleSampleRate) {
                        Text(
                            text = "${sampleRate / 1000}kHz",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Estimated file size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Est. Size")
                    Text(
                        text = VoiceRecorderViewModel.formatBytes(estimatedSize),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Reset button
        if (state != RecordingState.IDLE || durationMs > 0) {
            TextButton(onClick = onReset) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset")
            }
        }
    }
}

/**
 * Format bytes to human-readable string.
 */
fun VoiceRecorderViewModel.Companion.formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        else -> "%.1f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
