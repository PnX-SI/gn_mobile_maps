package fr.geonature.maps.sample.util

import android.os.Environment
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import fr.geonature.maps.sample.BuildConfig
import fr.geonature.maps.sample.R
import fr.geonature.mountpoint.util.MountPointUtils.getExternalStorage
import fr.geonature.mountpoint.util.MountPointUtils.getInternalStorage
import java.text.DateFormat
import java.util.Date

/**
 * Helper about application settings through [Preference].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object PreferencesUtils {

    fun updatePreferences(preferenceScreen: PreferenceScreen) {
        val context = preferenceScreen.context

        preferenceScreen.findPreference<Preference?>(context.getString(R.string.preference_category_storage_internal_key))
            ?.summary = getInternalStorage(preferenceScreen.context).mountPath.absolutePath
        getExternalStorage(
            preferenceScreen.context,
            Environment.MEDIA_MOUNTED,
            Environment.MEDIA_MOUNTED_READ_ONLY
        )?.also { mountPoint ->
            preferenceScreen.findPreference<Preference?>(context.getString(R.string.preference_category_storage_external_key))
                ?.also {
                    it.summary = mountPoint.mountPath.absolutePath
                    it.isEnabled = true
                }
        }

        preferenceScreen.findPreference<Preference?>(context.getString(R.string.preference_category_about_app_version_key))?.summary =
            context.getString(
                R.string.app_version,
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                DateFormat.getDateTimeInstance()
                    .format(Date(BuildConfig.BUILD_DATE.toLong()))
            )
    }
}
