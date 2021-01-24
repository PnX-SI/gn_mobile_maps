package fr.geonature.maps.sample.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.lifecycle.lifecycleScope
import fr.geonature.maps.sample.R
import fr.geonature.maps.sample.ui.settings.PreferencesActivity
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.settings.io.MapSettingsReader
import fr.geonature.maps.ui.MapFragment
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Home screen Activity.
 *
 * @see HomeListFragment
 */
class HomeActivity : AppCompatActivity(), HomeListFragment.OnHomeListFragmentListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    android.R.id.content,
                    HomeListFragment.newInstance()
                )
                .commit()
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
            R.id.menu_settings -> {
                startActivity(PreferencesActivity.newIntent(this))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSelectedMapSettings(mapSettings: MapSettings?) {
        lifecycleScope.launch {
            (mapSettings ?: openMapSettingsFromStorage())?.also {
                loadMapFragment(it)
            }
        }
    }

    override fun onBackPressed() {
        val mapFragment =
            supportFragmentManager.findFragmentByTag(MapFragment::class.java.simpleName)

        if (mapFragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(mapFragment)
                .commit()
        } else {
            super.onBackPressed()
        }
    }

    private suspend fun openMapSettingsFromStorage() = suspendCoroutine<MapSettings?> {
        registerForActivityResult(StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_CANCELED -> {
                    Toast.makeText(
                        this,
                        R.string.toast_settings_not_selected,
                        Toast.LENGTH_LONG
                    )
                        .show()
                    it.resume(null)
                }
                RESULT_OK -> {
                    val uri = result.data?.data

                    if (uri == null) {
                        Toast.makeText(
                            this,
                            R.string.toast_settings_not_found,
                            Toast.LENGTH_LONG
                        )
                            .show()
                        it.resume(null)

                        return@registerForActivityResult
                    }

                    try {
                        val mapSettings =
                            MapSettingsReader().read(InputStreamReader(contentResolver.openInputStream(uri)))

                        it.resume(mapSettings)
                    } catch (e: Exception) {
                        Log.w(
                            TAG,
                            "unable to load settings from $uri",
                            e
                        )

                        Toast.makeText(
                            this,
                            R.string.toast_settings_not_found,
                            Toast.LENGTH_LONG
                        )
                            .show()
                        it.resume(null)
                    }
                }
            }
        }.also { resultLauncher ->
            resultLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            })
        }
    }

    private fun loadMapFragment(mapSettings: MapSettings) {
        supportFragmentManager.beginTransaction()
            .add(
                android.R.id.content,
                MapFragment.newInstance(mapSettings),
                MapFragment::class.java.simpleName
            )
            .commit()
    }

    companion object {
        private val TAG = HomeActivity::class.java.name
    }
}