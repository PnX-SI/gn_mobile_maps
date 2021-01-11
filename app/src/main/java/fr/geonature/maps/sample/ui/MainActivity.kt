package fr.geonature.maps.sample.ui

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.lifecycle.ViewModelProvider
import fr.geonature.commons.util.observeOnce
import fr.geonature.maps.sample.R
import fr.geonature.maps.sample.settings.AppSettings
import fr.geonature.maps.sample.settings.AppSettingsViewModel
import fr.geonature.maps.sample.ui.settings.PreferencesActivity
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.MapFragment
import fr.geonature.maps.ui.overlay.feature.FeatureCollectionOverlay
import fr.geonature.maps.ui.overlay.feature.filter.ContainsFeaturesFilter
import fr.geonature.maps.ui.widget.EditFeatureButton
import fr.geonature.maps.util.PermissionUtils

/**
 * Main Activity.
 *
 * @see MapFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class MainActivity : AppCompatActivity() {

    private var appSettings: AppSettings? = null
    private lateinit var appSettingsViewModel: AppSettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        appSettingsViewModel = ViewModelProvider(this,
            fr.geonature.commons.settings.AppSettingsViewModel.Factory {
                AppSettingsViewModel(
                    this.application
                )
            }).get(AppSettingsViewModel::class.java)

        checkPermissions()
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

    private fun checkPermissions() {
        PermissionUtils.requestPermissions(
            this,
            listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            { result ->
                if (result.values.all { it }) {
                    loadAppSettings()
                } else {
                    Toast.makeText(
                        this,
                        R.string.snackbar_permissions_not_granted,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            })
    }

    private fun loadAppSettings() {
        appSettingsViewModel.loadAppSettings()
            .observeOnce(this) {
                val mapSettings = it?.mapSettings

                if (mapSettings == null) {
                    Toast.makeText(
                        this@MainActivity,
                        getString(
                            R.string.toast_settings_not_found,
                            appSettingsViewModel.getAppSettingsFilename()
                        ),
                        Toast.LENGTH_LONG
                    )
                        .show()
                } else {
                    appSettings = it
                    loadMapFragment(mapSettings)
                }
            }
    }

    /**
     * Display the MapFragment as main content.
     */
    private fun loadMapFragment(mapSettings: MapSettings) {
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content,
                MapFragment.newInstance(
                    mapSettings,
                    EditFeatureButton.EditMode.SINGLE
                )
                    .apply {
                        onSelectedPOIsListener = { pois ->
                            Log.i(
                                TAG,
                                "selected POIs: $pois"
                            )

                            getOverlays { overlay -> overlay is FeatureCollectionOverlay }.asSequence()
                                .map { it as FeatureCollectionOverlay }
                                .map { it.also { it.setStyle(it.layerStyle) } }
                                .map {
                                    pois.asSequence()
                                        .filterNotNull()
                                        .map { geoPoint ->
                                            val filter = ContainsFeaturesFilter(
                                                geoPoint,
                                                it.layerStyle,
                                                LayerStyleSettings.Builder.newInstance()
                                                    .from(
                                                        it.layerStyle
                                                    )
                                                    .color("red")
                                                    .build()
                                            )
                                            it.apply(filter)
                                            filter.getSelectedFeatures()
                                        }
                                        .flatMap { list -> list.asSequence() }
                                        .toList()
                                }
                                .flatMap { it.asSequence() }
                                .forEach {
                                    Log.i(
                                        TAG,
                                        "selected zone: ${it.id}"
                                    )
                                }
                        }
                        onVectorLayersChangedListener = { activeVectorLayers ->
                            activeVectorLayers.asSequence()
                                .map { it as FeatureCollectionOverlay }
                                .map { it.also { it.setStyle(it.layerStyle) } }
                                .map {
                                    getSelectedPOIs().asSequence()
                                        .filterNotNull()
                                        .map { geoPoint ->
                                            val filter = ContainsFeaturesFilter(
                                                geoPoint,
                                                it.layerStyle,
                                                LayerStyleSettings.Builder.newInstance()
                                                    .from(
                                                        it.layerStyle
                                                    )
                                                    .color("red")
                                                    .build()
                                            )
                                            it.apply(filter)
                                            filter.getSelectedFeatures()
                                        }
                                        .flatMap { list -> list.asSequence() }
                                        .toList()
                                }
                                .flatMap { it.asSequence() }
                                .forEach {
                                    Log.i(
                                        TAG,
                                        "selected zone: ${it.id}"
                                    )
                                }
                        }
                    })
            .commit()
    }

    companion object {
        private val TAG = MainActivity::class.java.name
    }
}
