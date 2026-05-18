package com.armyknife.tools.core.common

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Dialog shown when a tool requires permissions that are not granted.
 */
@Composable
fun PermissionRequestDialog(
    permissions: List<String>,
    onGrant: () -> Unit,
    onDeny: () -> Unit
) {
    val requirements = PermissionHelper.getPermissionRequirements(permissions)

    AlertDialog(
        onDismissRequest = { onDeny() },
        icon = {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
        },
        title = {
            Text(
                text = "Permission Required",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("This tool needs the following permissions to work:")
                requirements.forEach { req ->
                    Text(
                        text = "• ${req.label}: ${req.rationale}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onGrant) {
                Text("Grant")
            }
        },
        dismissButton = {
            TextButton(onClick = onDeny) {
                Text("Cancel")
            }
        }
    )
}
