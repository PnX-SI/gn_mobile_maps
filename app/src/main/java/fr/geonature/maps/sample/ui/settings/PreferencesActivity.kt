package fr.geonature.maps.sample.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.util.getParcelableExtraCompat

/**
 * Global settings.
 *
 * @author S. Grimault
 * @see PreferencesFragment
 */
class PreferencesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapSettings: MapSettings? = intent.getParcelableExtraCompat(EXTRA_MAP_SETTINGS)

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
            .replace(
                android.R.id.content,
                PreferencesFragment.newInstance(mapSettings)
            )
            .commit()
    }

    override fun finish() {
        setResult(Activity.RESULT_OK)
        super.finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {

        private const val EXTRA_MAP_SETTINGS = "extra_map_settings"

        fun newIntent(context: Context, mapSettings: MapSettings? = null): Intent {
            return Intent(
                context,
                PreferencesActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_MAP_SETTINGS,
                    mapSettings
                )
            }
        }
    }
}
