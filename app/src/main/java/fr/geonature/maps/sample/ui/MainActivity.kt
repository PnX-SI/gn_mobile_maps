package fr.geonature.maps.sample.ui

import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import fr.geonature.maps.sample.R
import fr.geonature.maps.sample.settings.AppSettingsManager
import fr.geonature.maps.sample.settings.io.OnAppSettingsJsonReaderListenerImpl
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.MapFragment
import fr.geonature.maps.util.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Main Activity.
 *
 * @see MapFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class MainActivity : AppCompatActivity(), MapFragment.OnMapFragmentListener {

    private lateinit var mapSettings: MapSettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val granted = PermissionUtils.checkSelfPermissions(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (granted) {
            loadAppSettings()
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSIONS -> {
                val requestPermissionsResult = PermissionUtils.checkPermissions(grantResults)

                if (requestPermissionsResult) {
                    loadAppSettings()
                }
                else {
                    Toast.makeText(
                        this,
                        fr.geonature.maps.R.string.snackbar_permissions_not_granted,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
            else -> super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        }
    }

    override fun getMapSettings(): MapSettings {
        return mapSettings
    }

    private fun loadAppSettings() {
        val appSettingsManager = AppSettingsManager(
            application,
            OnAppSettingsJsonReaderListenerImpl()
        )

        GlobalScope.launch(Dispatchers.Main) {
            val appSettings = appSettingsManager.loadAppSettings()

            if (appSettings?.mapSettings == null) {
                Toast.makeText(
                    this@MainActivity,
                    getString(
                        R.string.toast_settings_not_found,
                        appSettingsManager.getAppSettingsFilename()
                    ),
                    Toast.LENGTH_LONG
                )
                    .show()
            }
            else {
                this@MainActivity.mapSettings = appSettings.mapSettings!!

                // Display the fragment as the main content.
                supportFragmentManager.beginTransaction()
                    .replace(
                        android.R.id.content,
                        MapFragment.newInstance()
                    )
                    .commit()
            }
        }
    }

    companion object {
        private const val REQUEST_STORAGE_PERMISSIONS = 0
    }
}
