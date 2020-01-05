package fr.geonature.maps.sample.util

import android.content.Context
import android.os.Environment
import android.text.TextUtils
import android.util.Log
import fr.geonature.maps.BuildConfig
import java.io.File

/**
 * Helpers for [File] utilities.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object FileUtils {

    private val TAG = FileUtils::class.java.name

    /**
     * Construct a file from the set of name elements.
     *
     * @param directory the parent directory
     * @param names the name elements
     *
     * @return the corresponding file
     */
    fun getFile(
        directory: File,
        vararg names: String
    ): File {

        var file = directory

        for (name in names) {
            file = File(
                file,
                name
            )
        }

        return file
    }

    /**
     * Gets the relative path used by this context.
     *
     * @param context the current `Context`
     *
     * @return the relative path
     */
    fun getRelativeSharedPath(context: Context): String {

        return "Android" + File.separator + "data" + File.separator + context.packageName + File.separator
    }

    /**
     * Return the primary external storage.
     *
     * @return the primary external storage
     */
    fun getInternalStorage(): File {
        val externalStorage = System.getenv("EXTERNAL_STORAGE")

        if (TextUtils.isEmpty(externalStorage)) {
            val path = Environment.getExternalStorageDirectory()

            if (BuildConfig.DEBUG) {
                Log.d(
                    TAG,
                    "internal storage from API: ${path.absolutePath}"
                )
            }

            return path
        }

        val path = File(externalStorage)

        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "internal storage from system environment: ${path.absolutePath}"
            )
        }

        return path
    }

    /**
     * Gets the root folder as `File` used by this context.
     *
     * @param context the current `Context`
     *
     * @return the root folder as `File`
     */
    fun getRootFolder(
        context: Context
    ): File {

        return getFile(
            getInternalStorage(),
            getRelativeSharedPath(context)
        )
    }
}
