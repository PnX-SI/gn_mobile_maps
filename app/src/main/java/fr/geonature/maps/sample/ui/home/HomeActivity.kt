package fr.geonature.maps.sample.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.maps.sample.R
import fr.geonature.maps.sample.ui.map.MapActivity
import fr.geonature.maps.settings.io.MapSettingsReader
import fr.geonature.mountpoint.util.FileUtils
import fr.geonature.mountpoint.util.getFile
import org.tinylog.kotlin.Logger
import java.io.InputStreamReader

/**
 * Home screen Activity.
 *
 * @author S. Grimault
 * @see HomeListFragment
 */
class HomeActivity : AppCompatActivity(), HomeListFragment.OnHomeListFragmentListener {

    private lateinit var mapSettingsResultLauncher: ActivityResultLauncher<Intent>

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

        val osmdroidFile = FileUtils.getExternalStorageDirectory(application)
            .getFile("osmdroid")

        Logger.debug {
            "${osmdroidFile.absolutePath}: (exists: ${osmdroidFile.exists()}, ${
                if (osmdroidFile.canRead()) "r" else ""
            }${
                if (osmdroidFile.canWrite()) "w" else ""
            }${
                if (osmdroidFile.canExecute()) "x" else ""
            })"
        }

        mapSettingsResultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            when (result.resultCode) {
                RESULT_CANCELED -> {
                    Toast.makeText(
                        this,
                        R.string.toast_settings_not_selected,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }

                RESULT_OK -> {
                    val uri = result.data?.data

                    if (uri == null) {
                        Toast.makeText(
                            this,
                            R.string.toast_settings_not_selected,
                            Toast.LENGTH_LONG
                        )
                            .show()

                        return@registerForActivityResult
                    }

                    Logger.info { "loading settings from '$uri'..." }

                    try {
                        val mapSettings =
                            MapSettingsReader().read(InputStreamReader(contentResolver.openInputStream(uri)))

                        startActivity(
                            MapActivity.newIntent(
                                this,
                                mapSettings,
                                getString(
                                    R.string.map_settings_loaded_from,
                                    uri.path
                                )
                            )
                        )
                    } catch (e: Exception) {
                        Logger.warn(e) { "unable to load settings from $uri" }

                        Toast.makeText(
                            this,
                            getString(
                                R.string.toast_settings_not_found,
                                uri.path
                            ),
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                }
            }
        }
    }

    override fun onSelectedMenuItem(menuItem: MenuItem) {
        if (menuItem.mapSettings == null) {
            mapSettingsResultLauncher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            })

            return
        }

        startActivity(
            MapActivity.newIntent(
                this,
                menuItem.mapSettings,
                menuItem.label
            )
        )
    }
}