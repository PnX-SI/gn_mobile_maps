package fr.geonature.maps.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

/**
 * Helper class about Android permissions.
 *
 * @author S. Grimault
 */
object PermissionUtils {

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