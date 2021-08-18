package fr.geonature.maps.sample.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import fr.geonature.maps.sample.R
import fr.geonature.maps.sample.util.PreferencesUtils.updatePreferences
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.util.MapSettingsPreferencesUtils

/**
 * Global settings.
 *
 * @author S. Grimault
 */
class PreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setDefaultPreferences(arguments?.getParcelable(ARG_MAP_SETTINGS))
        updatePreferences(preferenceScreen)
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        addPreferencesFromResource(fr.geonature.maps.R.xml.map_preferences)
        addPreferencesFromResource(R.xml.preferences)
    }

    private fun setDefaultPreferences(appSettings: MapSettings?) {
        val context = context ?: return

        MapSettingsPreferencesUtils.setDefaultPreferences(
            context,
            MapSettings.Builder.newInstance().from(appSettings).build(),
            preferenceScreen
        )
    }

    companion object {

        private const val ARG_MAP_SETTINGS = "arg_map_settings"

        /**
         * Use this factory method to create a new instance of [PreferencesFragment].
         *
         * @return A new instance of [PreferencesFragment]
         */
        @JvmStatic
        fun newInstance(mapSettings: MapSettings? = null) = PreferencesFragment().apply {
            arguments = Bundle().apply {
                putParcelable(
                    ARG_MAP_SETTINGS,
                    mapSettings
                )
            }
        }
    }
}
