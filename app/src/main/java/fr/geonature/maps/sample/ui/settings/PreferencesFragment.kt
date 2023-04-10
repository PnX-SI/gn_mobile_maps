package fr.geonature.maps.sample.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import fr.geonature.compat.os.getParcelableCompat
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

        setDefaultPreferences(arguments?.getParcelableCompat(ARG_MAP_SETTINGS))
        updatePreferences(preferenceScreen)
        configurePermissions()
    }

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        addPreferencesFromResource(fr.geonature.maps.R.xml.map_preferences)
        addPreferencesFromResource(R.xml.preferences_permissions)
        addPreferencesFromResource(R.xml.preferences_storage)
        addPreferencesFromResource(R.xml.preferences_about)
    }

    private fun setDefaultPreferences(appSettings: MapSettings?) {
        val context = context ?: return

        MapSettingsPreferencesUtils.setDefaultPreferences(
            context,
            MapSettings.Builder.newInstance()
                .from(appSettings)
                .build(),
            preferenceScreen
        )
    }

    private fun configurePermissions() {
        preferenceScreen
            .findPreference<Preference>(getString(R.string.preference_category_permissions_configure_key))
            ?.apply {
                onPreferenceClickListener = Preference.OnPreferenceClickListener {
                    startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts(
                                "package",
                                it.context.packageName,
                                null
                            )
                        )
                    )

                    true
                }
            }
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
