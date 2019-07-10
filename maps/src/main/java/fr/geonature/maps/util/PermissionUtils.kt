package fr.geonature.maps.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

/**
 * Helper class about Android permissions.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object PermissionUtils {

    /**
     * Checks that all given permissions have been granted by verifying that each entry in the
     * given array is of the value [PackageManager.PERMISSION_GRANTED].
     *
     * @see Activity.onRequestPermissionsResult
     */
    fun checkPermissions(grantResults: IntArray): Boolean {
        // At least one result must be checked.
        if (grantResults.isEmpty()) {
            return false
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }

    /**
     * Determines whether the user have been granted a set of permissions.
     *
     * @param context the current `Context`.
     * @param permissions a set of permissions being checked
     */
    fun checkSelfPermissions(
        context: Context,
        vararg permissions: String
    ): Boolean {
        var granted = true
        val iterator = permissions.iterator()

        while (iterator.hasNext() && granted) {
            granted = ActivityCompat.checkSelfPermission(
                context,
                iterator.next()
            ) == PackageManager.PERMISSION_GRANTED
        }

        return granted
    }
}