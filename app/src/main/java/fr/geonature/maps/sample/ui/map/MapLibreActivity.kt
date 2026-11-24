package fr.geonature.maps.sample.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.content.IntentCompat
import fr.geonature.maps.maplibre.ui.MapLibreFragment
import fr.geonature.maps.sample.R
import fr.geonature.maps.sample.ui.settings.PreferencesActivity
import fr.geonature.maps.settings.MapSettings

/**
 * Hosts a [fr.geonature.maps.maplibre.ui.MapLibreFragment].
 *
 * @author S. Grimault
 * @see MapLibreFragment
 */
class MapLibreActivity : AppCompatActivity() {

    private var mapSettings: MapSettings? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mapSettings = IntentCompat.getParcelableExtra(
            intent,
            EXTRA_MAP_SETTINGS,
            MapSettings::class.java
        )

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

        mapSettings?.also {
            // display the fragment as the main content
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(
                android.R.id.content,
                MapLibreFragment.newInstance(it)
            )
            transaction.commit()
        }
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(
            R.menu.settings,
            menu
        )

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            R.id.menu_settings -> {
                startActivity(
                    PreferencesActivity.newIntent(
                        this,
                        mapSettings
                    )
                )
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
                MapLibreActivity::class.java
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
