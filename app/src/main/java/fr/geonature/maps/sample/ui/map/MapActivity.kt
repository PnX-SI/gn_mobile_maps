package fr.geonature.maps.sample.ui.map

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.maps.sample.R
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.MapFragment

/**
 * Home screen Activity.
 *
 * @see MapFragment
 */
class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapSettings = intent.getParcelableExtra<MapSettings?>(EXTRA_MAP_SETTINGS)

        if (mapSettings == null) {
            Toast.makeText(
                this,
                R.string.toast_settings_not_selected,
                Toast.LENGTH_LONG
            )
                .show()

            finish()
            return
        }

        intent.getStringExtra(EXTRA_MAP_TITLE)
            ?.also {
                supportActionBar?.subtitle = it
            }

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
            .replace(
                android.R.id.content,
                MapFragment.newInstance(mapSettings)
            )
            .commit()
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
        const val EXTRA_MAP_SETTINGS = "extra_map_settings"
        const val EXTRA_MAP_TITLE = "extra_map_title"

        fun newIntent(
            context: Context,
            mapSettings: MapSettings,
            title: String? = null
        ): Intent {
            return Intent(
                context,
                MapActivity::class.java
            ).apply {
                putExtra(
                    EXTRA_MAP_SETTINGS,
                    mapSettings
                )
                putExtra(
                    EXTRA_MAP_TITLE,
                    title
                )
            }
        }
    }
}