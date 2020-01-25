package fr.geonature.maps.util

import android.content.Context
import androidx.preference.Preference
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import fr.geonature.maps.R

/**
 * Helper about application settings through [Preference].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object SettingsUtils {

    /**
     * Whether to show north compass during map rotation (default: `true`).
     *
     * @param context the current context
     *
     * @return `true` if the map compass should be shown
     */
    fun showCompass(context: Context): Boolean {
        return getDefaultSharedPreferences(context)
            .getBoolean(
                context.getString(R.string.preference_category_map_show_compass_key),
                true
            )
    }

    /**
     * Whether to show the map scale (default: `true`).
     *
     * @param context the current context
     *
     * @return `true` if the map scale should be shown
     */
    fun showScale(context: Context): Boolean {
        return getDefaultSharedPreferences(context)
            .getBoolean(
                context.getString(R.string.preference_category_map_show_scale_key),
                true
            )
    }

    /**
     * Whether to show the zoom control (default: `false`).
     *
     * @param context the current context
     *
     * @return `true` if the zoom control should be shown
     */
    fun showZoom(context: Context): Boolean {
        return getDefaultSharedPreferences(context)
            .getBoolean(
                context.getString(R.string.preference_category_map_show_zoom_key),
                false
            )
    }
}