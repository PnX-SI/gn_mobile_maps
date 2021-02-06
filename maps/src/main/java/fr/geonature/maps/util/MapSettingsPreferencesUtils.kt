package fr.geonature.maps.util

import android.content.Context
import androidx.preference.Preference
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import fr.geonature.maps.R
import fr.geonature.maps.settings.MapSettings

/**
 * Helper about application settings through [Preference].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object MapSettingsPreferencesUtils {

    /**
     * Sets default preferences settings values from [MapSettings].
     *
     * @param context the current context
     * @param mapSettings the [MapSettings] to read
     * @param preferenceScreen preference screen to update if any
     */
    fun setDefaultPreferences(
        context: Context,
        mapSettings: MapSettings,
        preferenceScreen: PreferenceScreen? = null
    ) {
        mutableMapOf(
            Pair(
                context.getString(R.string.preference_category_map_use_default_online_source_key),
                mapSettings.useDefaultOnlineTileSource
            ),
            Pair(
                context.getString(R.string.preference_category_map_show_compass_key),
                mapSettings.showCompass
            ),
            Pair(
                context.getString(R.string.preference_category_map_show_scale_key),
                mapSettings.showScale
            ),
            Pair(
                context.getString(R.string.preference_category_map_show_zoom_key),
                mapSettings.showZoom
            ),
        ).forEach {
            if (!getDefaultSharedPreferences(context).contains(it.key)) {
                getDefaultSharedPreferences(context).edit()
                    .putBoolean(
                        it.key,
                        it.value
                    )
                    .apply()
            }

            preferenceScreen?.findPreference<SwitchPreference>(it.key)?.isChecked = it.value
        }
    }

    /**
     * Whether to use the default online tiles source (default: [MapSettings.Builder.useDefaultOnlineTileSource]).
     *
     * @param context the current context
     *
     * @return `true` if the default online tiles source can be used
     */
    fun useDefaultOnlineSource(context: Context): Boolean {
        return getDefaultSharedPreferences(context)
            .getBoolean(
                context.getString(R.string.preference_category_map_use_default_online_source_key),
                MapSettings.Builder.newInstance().useDefaultOnlineTileSource
            )
    }

    /**
     * Whether to show north compass during map rotation (default: [MapSettings.Builder.showCompass]).
     *
     * @param context the current context
     *
     * @return `true` if the map compass should be shown
     */
    fun showCompass(context: Context): Boolean {
        return getDefaultSharedPreferences(context)
            .getBoolean(
                context.getString(R.string.preference_category_map_show_compass_key),
                MapSettings.Builder.newInstance().showCompass
            )
    }

    /**
     * Whether to show the map scale (default: [MapSettings.Builder.showScale]).
     *
     * @param context the current context
     *
     * @return `true` if the map scale should be shown
     */
    fun showScale(context: Context): Boolean {
        return getDefaultSharedPreferences(context)
            .getBoolean(
                context.getString(R.string.preference_category_map_show_scale_key),
                MapSettings.Builder.newInstance().showScale
            )
    }

    /**
     * Whether to show the zoom control (default: [MapSettings.Builder.showZoom]).
     *
     * @param context the current context
     *
     * @return `true` if the zoom control should be shown
     */
    fun showZoom(context: Context): Boolean {
        return getDefaultSharedPreferences(context)
            .getBoolean(
                context.getString(R.string.preference_category_map_show_zoom_key),
                MapSettings.Builder.newInstance().showZoom
            )
    }
}