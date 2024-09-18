package fr.geonature.mountpoint.util

import android.content.Context
import android.os.Environment
import fr.geonature.mountpoint.model.MountPoint
import fr.geonature.mountpoint.util.MountPointUtils.getExternalStorage
import fr.geonature.mountpoint.util.MountPointUtils.getInternalStorage
import org.tinylog.Logger
import java.io.File

/**
 * Helpers for [File] utilities.
 *
 * @author S. Grimault
 */
object FileUtils {

    /**
     * Gets the relative path used by given package ID.
     *
     * @param packageId the package ID
     *
     * @return the relative path
     */
    fun getRelativeSharedPath(packageId: String): String {
        return arrayOf(
            "Android",
            "data",
            packageId
        ).joinToString(
            File.separator,
            "",
            File.separator
        )
    }

    /**
     * Tries to find the mount point used by external storage as `File`.
     * If not, returns the default `Environment.getExternalStorageDirectory()`.
     *
     * @param context the current `Context`
     *
     * @return the mount point as `File` used by external storage if available
     */
    fun getExternalStorageDirectory(context: Context): File {

        val externalMountPoint = getExternalStorage(
            context,
            Environment.MEDIA_MOUNTED,
            Environment.MEDIA_MOUNTED_READ_ONLY
        )

        if (externalMountPoint == null) {
            Logger.warn { "getExternalStorageDirectory: external mount point is not available. Use default: " + getInternalStorage(context) }

            return getInternalStorage(context).mountPath
        }

        return externalMountPoint.mountPath
    }

    /**
     * Gets the root folder as `File` from given package ID.
     * If package ID is `null` use given context.
     *
     * @param context the current `Context`
     * @param storageType the [MountPoint.StorageType] to use
     * @param packageId the package ID (may be `null`)
     *
     * @return the root folder as `File`
     */
    fun getRootFolder(
        context: Context,
        storageType: MountPoint.StorageType,
        packageId: String? = null
    ): File {
        return (if (storageType === MountPoint.StorageType.EXTERNAL) getExternalStorageDirectory(context)
        else getInternalStorage(context).mountPath).getFile(
            getRelativeSharedPath(if (packageId.isNullOrBlank()) context.packageName else packageId)
        )
    }
}
