package fr.geonature.maps.sample.settings

import android.app.Application
import android.util.Log
import fr.geonature.maps.sample.settings.io.AppSettingsJsonReader
import fr.geonature.maps.sample.util.FileUtils.getFile
import fr.geonature.maps.sample.util.FileUtils.getRootFolder
import java.io.File
import java.io.FileReader
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Manage [IAppSettings].
 * - Read [IAppSettings] from `JSON` file
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AppSettingsManager<T : IAppSettings>(
    private val application: Application,
    onAppSettingsJsonJsonReaderListener: AppSettingsJsonReader.OnAppSettingsJsonReaderListener<T>
) {

    private val appSettingsJsonReader: AppSettingsJsonReader<T> =
        AppSettingsJsonReader(onAppSettingsJsonJsonReaderListener)

    init {
        GlobalScope.launch(Dispatchers.Main) {
            withContext(IO) {
                val rootFolder = getRootFolder(application)

                Log.i(
                    TAG,
                    "create root folder ${rootFolder.absolutePath}"
                )

                rootFolder.mkdirs()
            }
        }
    }

    fun getAppSettingsFilename(): String {
        val packageName = application.packageName

        return "settings_${packageName.substring(packageName.lastIndexOf('.') + 1)}.json"
    }

    /**
     * Loads [IAppSettings] from `JSON` file.
     *
     * @return [IAppSettings] or `null` if not found
     */
    suspend fun loadAppSettings(): T? =
        withContext(IO) {
            val settingsJsonFile = getAppSettingsAsFile()

            if (!settingsJsonFile.exists()) {
                Log.w(
                    TAG,
                    "'${settingsJsonFile.absolutePath}' not found"
                )
                null
            } else {
                try {
                    appSettingsJsonReader.read(FileReader(settingsJsonFile))
                } catch (e: IOException) {
                    Log.w(
                        TAG,
                        "Failed to load '${settingsJsonFile.name}'"
                    )

                    null
                }
            }
        }

    internal fun getAppSettingsAsFile(): File {
        return getFile(
            getRootFolder(
                application
            ),
            getAppSettingsFilename()
        )
    }

    companion object {
        private val TAG = AppSettingsManager::class.java.name
    }
}
