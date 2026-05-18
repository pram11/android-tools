package com.armyknife.tools.core.common

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

/**
 * Composable wrapper that handles runtime permission requests.
 * Returns true if permissions are granted, false otherwise.
 */
@Composable
fun PermissionHandler(
    permissions: List<String>,
    content: @Composable (isGranted: Boolean) -> Unit
) {
    val context = LocalContext.current
    var granted by remember { mutableStateOf(PermissionHelper.arePermissionsGranted(context, permissions)) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { results ->
            granted = results.values.all { it }
        }
    )

    if (granted) {
        content(true)
    } else {
        PermissionRequestDialog(
            permissions = permissions,
            onGrant = {
                val permArray = permissions.toTypedArray()
                launcher.launch(permArray)
            },
            onDeny = { /* dismissed, stay in dialog */ }
        )
    }
}
