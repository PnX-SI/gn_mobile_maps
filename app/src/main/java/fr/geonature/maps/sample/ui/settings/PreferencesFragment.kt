package fr.geonature.maps.sample.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import fr.geonature.maps.sample.R

/**
 * Global settings.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class PreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        addPreferencesFromResource(fr.geonature.maps.R.xml.map_preferences)
        addPreferencesFromResource(R.xml.preferences)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of [PreferencesFragment].
         *
         * @return A new instance of [PreferencesFragment]
         */
        @JvmStatic
        fun newInstance() = PreferencesFragment()
    }
}
