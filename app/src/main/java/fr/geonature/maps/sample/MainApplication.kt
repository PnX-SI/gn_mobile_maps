package fr.geonature.maps.sample

import android.app.Application
import android.os.Environment
import dagger.hilt.android.HiltAndroidApp
import fr.geonature.mountpoint.util.FileUtils.getRelativeSharedPath
import fr.geonature.mountpoint.util.MountPointUtils
import fr.geonature.mountpoint.util.getFile
import org.tinylog.Logger
import java.io.File
import kotlin.system.exitProcess

/**
 * Base class to maintain global application state.
 *
 * @author S. Grimault
 */
@HiltAndroidApp
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        configureLogger()

        Logger.info {
            "internal storage: '${MountPointUtils.getInternalStorage(this)}'"
        }
        Logger.info {
            "external storage: '${MountPointUtils.getExternalStorage(this)}'"
        }
    }

    private fun configureLogger() {
        val directoryForLogs: File = Environment.getExternalStorageDirectory()
            .getFile(
                getRelativeSharedPath(applicationContext.packageName),
                "logs"
            )
            .also {
                it.mkdirs()
            }

        System.setProperty(
            "tinylog.directory",
            directoryForLogs.absolutePath
        )
        System.setProperty(
            "tinylog.level",
            if (BuildConfig.DEBUG) "debug" else "info"
        )

        Thread.setDefaultUncaughtExceptionHandler(TinylogUncaughtExceptionHandler())

        Logger.info { "starting ${BuildConfig.APPLICATION_ID} (version ${BuildConfig.VERSION_NAME})..." }
        Logger.info { "logs directory: '$directoryForLogs'" }
    }

    private class TinylogUncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
        override fun uncaughtException(
            thread: Thread,
            ex: Throwable
        ) {
            Logger.error(ex)
            exitProcess(1)
        }
    }
}