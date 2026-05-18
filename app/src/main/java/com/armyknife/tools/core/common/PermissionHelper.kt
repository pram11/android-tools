package com.armyknife.tools.core.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Permission requirement descriptor for tools.
 */
data class PermissionRequirement(
    val permission: String,
    val label: String,
    val rationale: String
)

/**
 * Maps ToolItem requiredPermissions to PermissionRequirement objects
 * with human-readable labels and rationales.
 */
object PermissionHelper {

    private val permissionMap: Map<String, PermissionRequirement> = mapOf(
        Manifest.permission.RECORD_AUDIO to PermissionRequirement(
            Manifest.permission.RECORD_AUDIO,
            "Microphone",
            "Required for sound meter and voice recorder"
        ),
        Manifest.permission.CAMERA to PermissionRequirement(
            Manifest.permission.CAMERA,
            "Camera",
            "Required for QR scanner, magnifier, flashlight, and mirror"
        ),
        Manifest.permission.ACCESS_FINE_LOCATION to PermissionRequirement(
            Manifest.permission.ACCESS_FINE_LOCATION,
            "Location",
            "Required for speedometer GPS tracking"
        ),
        Manifest.permission.ACCESS_COARSE_LOCATION to PermissionRequirement(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            "Location (approximate)",
            "Required for speedometer GPS tracking"
        )
    )

    /**
     * Get human-readable permission requirements from a list of raw permission strings.
     */
    fun getPermissionRequirements(permissions: List<String>): List<PermissionRequirement> =
        permissions.mapNotNull { perm -> permissionMap[perm] }

    /**
     * Check if all required permissions are granted.
     */
    fun arePermissionsGranted(context: Context, permissions: List<String>): Boolean =
        permissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }

    /**
     * Check if a single permission is granted.
     */
    fun isPermissionGranted(context: Context, permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    /**
     * Get list of permissions that are NOT granted from the required set.
     */
    fun getDeniedPermissions(context: Context, permissions: List<String>): List<String> =
        permissions.filter { perm ->
            ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED
        }

    /**
     * Get the human-readable label for a permission string.
     */
    fun getPermissionLabel(permission: String): String =
        permissionMap[permission]?.label ?: permission
}
