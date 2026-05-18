package com.armyknife.tools.features.media

import android.content.Context
import android.content.pm.PackageManager
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ApkExtractorScreen(viewModel: ApkExtractorViewModel = viewModel()) {
    val context = LocalContext.current
    val packageManager = remember { context.packageManager }

    // Load installed apps
    val allApps = remember {
        try {
            packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES).map { pkg ->
                ApkInfo(
                    packageName = pkg.packageName,
                    appName = pkg.applicationInfo.loadLabel(packageManager).toString(),
                    versionName = pkg.versionName ?: "unknown",
                    sizeBytes = try {
                        pkg.applicationInfo.sourceDir.let { path ->
                            java.io.File(path).length()
                        }
                    } catch (e: Exception) {
                        0L
                    },
                    installTime = pkg.firstInstallTime
                )
            }.filter { it.appName.isNotBlank() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    val filteredApps = remember(allApps, viewModel.searchQuery, viewModel.sortOrder) {
        viewModel.sortApps(viewModel.filterApps(allApps, viewModel.searchQuery), viewModel.sortOrder)
    }

    ApkExtractorContent(
        apps = filteredApps,
        totalCount = allApps.size,
        searchQuery = viewModel.searchQuery,
        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
        sortOrder = viewModel.sortOrder,
        onCycleSortOrder = { viewModel.cycleSortOrder() },
        onCopyPackage = { packageName ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("package", packageName))
            Toast.makeText(context, "Copied: $packageName", Toast.LENGTH_SHORT).show()
        }
    )
}

@Composable
private fun ApkExtractorContent(
    apps: List<ApkInfo>,
    totalCount: Int,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    sortOrder: ApkSortOrder,
    onCycleSortOrder: () -> Unit,
    onCopyPackage: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Header
        Text(
            text = "Installed Apps",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$apps.size${if (apps.size != totalCount) " / $totalCount" else ""}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = { Text("Search by name or package...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Text("✕", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sort button
        FilledTonalIconButton(onClick = onCycleSortOrder) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = when (sortOrder) {
                        ApkSortOrder.NAME -> "Name"
                        ApkSortOrder.SIZE -> "Size"
                        ApkSortOrder.DATE -> "Date"
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // App list
        if (apps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "No apps found" else "No apps installed",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    ApkItem(app = app, onCopyPackage = onCopyPackage)
                }
            }
        }
    }
}

@Composable
private fun ApkItem(app: ApkInfo, onCopyPackage: (String) -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Surface(
        shape = MaterialTheme.shapes.small,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                IconButton(
                    onClick = { onCopyPackage(app.packageName) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy package name",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "v${app.versionName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = ApkExtractorViewModel.formatBytes(app.sizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = dateFormat.format(Date(app.installTime)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
