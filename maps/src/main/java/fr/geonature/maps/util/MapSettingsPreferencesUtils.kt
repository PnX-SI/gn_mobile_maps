package fr.geonature.maps.util

import android.content.Context
import androidx.preference.Preference
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.preference.PreferenceScreen
import androidx.preference.SwitchPreference
import fr.geonature.maps.R
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.MapSettings

/**
 * Helper about application settings through [Preference].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object MapSettingsPreferencesUtils {

    private val KEY_SELECTED_LAYERS = "key_selected_layers"

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
                context.getString(R.string.preference_category_map_use_online_layers_key),
                Pair(
                    mapSettings.useOnlineLayers,
                    mapSettings.getOnlineLayers()
                        .isNotEmpty()
                )
            ),
            Pair(
                context.getString(R.string.preference_category_map_show_compass_key),
                Pair(
                    mapSettings.showCompass,
                    true
                )
            ),
            Pair(
                context.getString(R.string.preference_category_map_show_scale_key),
                Pair(
                    mapSettings.showScale,
                    true
                )
            ),
            Pair(
                context.getString(R.string.preference_category_map_show_zoom_key),
                Pair(
                    mapSettings.showZoom,
                    true
                )
            ),
        ).forEach {
            if (!getDefaultSharedPreferences(context).contains(it.key)) {
                getDefaultSharedPreferences(context).edit()
                    .putBoolean(
                        it.key,
                        it.value.first
                    )
                    .apply()
            }

            preferenceScreen?.findPreference<SwitchPreference>(it.key)
                ?.apply {
                    isChecked = getDefaultSharedPreferences(context).getBoolean(
                        it.key,
                        it.value.first
                    )
                    isEnabled = it.value.second
                }
        }
    }

    /**
     * Whether to use online layers (default: [MapSettings.Builder.useOnlineLayers]).
     *
     * @param context the current context
     *
     * @return `true` if online layers can be used
     */
    fun useOnlineLayers(
        context: Context,
        defaultValue: Boolean = MapSettings.Builder.newInstance().useOnlineLayers
    ): Boolean {
        return getDefaultSharedPreferences(context)
            .getBoolean(
                context.getString(R.string.preference_category_map_use_online_layers_key),
                defaultValue
            )
    }

    /**
     * Updates 'use online layers' preference.
     *
     * @param context the current context
     */
    fun setUseOnlineLayers(
        context: Context,
        useOnlineLayer: Boolean
    ) {
        getDefaultSharedPreferences(context).edit()
            .putBoolean(
                context.getString(R.string.preference_category_map_use_online_layers_key),
                useOnlineLayer
            )
            .apply()
    }

    /**
     * Returns the current selected layers.
     *
     * @return a list of selected [LayerSettings] or an empty list if no selection was made.
     */
    fun getSelectedLayers(context: Context, mapSettings: MapSettings): List<LayerSettings> {
        return getDefaultSharedPreferences(context).getStringSet(
            KEY_SELECTED_LAYERS,
            emptySet()
        )
            ?.asSequence()
            ?.map { mapSettings.layersSettings.firstOrNull { layerSettings -> layerSettings.label == it } }
            ?.filterNotNull()
            ?.toList() ?: emptyList()
    }

    /**
     * Updates selected layers preference.
     */
    fun setSelectedLayers(context: Context, selectedLayers: List<LayerSettings>) {
        getDefaultSharedPreferences(context).edit()
            .putStringSet(
                KEY_SELECTED_LAYERS,
                selectedLayers.asSequence()
                    .map { it.label }
                    .toSet()
            )
            .apply()
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