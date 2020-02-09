package fr.geonature.maps.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.maps.jts.geojson.io.WKTReader
import fr.geonature.maps.ui.overlay.feature.FeatureCollectionOverlay
import fr.geonature.mountpoint.util.FileUtils.getExternalStorageDirectory
import fr.geonature.mountpoint.util.FileUtils.getFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.tileprovider.MapTileProviderBase
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.views.overlay.Overlay
import java.io.File
import java.io.FileReader

/**
 * [LayerSettings] view model.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class LayerSettingsViewModel(application: Application, private val baseTilesPath: String? = null) :
    AndroidViewModel(application) {

    private val activeLayers: MutableList<LayerSettings> = mutableListOf()
    private val _tileProvider = MutableLiveData<MapTileProviderBase>()
    val tileProvider: LiveData<MapTileProviderBase> = _tileProvider

    private val _vectorOverlays = MutableLiveData<List<Overlay>>()
    val vectorOverlays: LiveData<List<Overlay>> = _vectorOverlays

    fun load(layersSettings: List<LayerSettings>) {
        activeLayers.clear()

        viewModelScope.launch {
            var rootPath = if (baseTilesPath?.startsWith("/") == true) {
                File(baseTilesPath)
            } else {
                getFile(getExternalStorageDirectory(getApplication()), baseTilesPath ?: "")
            }

            rootPath =
                if (rootPath.canRead()) rootPath else getExternalStorageDirectory(getApplication())

            _tileProvider.value = buildTileProvider(rootPath, layersSettings)
            _vectorOverlays.value = buildVectorOverlays(rootPath, layersSettings)
        }
    }

    fun getActiveLayers(): List<LayerSettings> {
        return activeLayers
    }

    private suspend fun buildTileProvider(
        rootPath: File,
        layersSettings: List<LayerSettings>
    ): MapTileProviderBase? = withContext(Dispatchers.IO) {
        val tileSources = layersSettings.asSequence()
            .filter { it.getType() == LayerType.TILES }
            .map {
                Log.i(
                    TAG,
                    "Loading tiles layer '${it.label}'..."
                )

                Pair(it, rootPath.walkTopDown()
                    .firstOrNull { f -> f.isFile && f.name == it.source && f.canRead() })
            }
            .filter { it.second != null }
            .onEach {
                activeLayers.add(it.first)
            }
            .map { it.second }
            .toList()

        if (tileSources.isNotEmpty()) {
            return@withContext OfflineTileProvider(
                SimpleRegisterReceiver(getApplication()),
                tileSources.toTypedArray()
            )
        }

        return@withContext null
    }

    private suspend fun buildVectorOverlays(
        rootPath: File,
        layersSettings: List<LayerSettings>
    ): List<Overlay> = withContext(Dispatchers.IO) {
        layersSettings.asSequence()
            .filter { it.getType() == LayerType.VECTOR }
            .map {
                Log.i(
                    TAG,
                    "Loading vector layer '${it.label}'..."
                )

                Pair(
                    it,
                    rootPath.walkTopDown()
                        .firstOrNull { f -> f.isFile && f.name == it.source && f.canRead() }
                )
            }
            .filter { it.second != null }
            .map {
                when (it.second?.extension) {
                    "geojson", "json" -> Pair(
                        it.first,
                        GeoJsonReader().read(FileReader(it.second))
                    )
                    "wkt" -> Pair(
                        it.first,
                        WKTReader().readFeatures(FileReader(it.second))
                    )
                    else -> Pair(
                        it.first,
                        emptyList()
                    )
                }
            }
            .filter {
                val isLoaded = it.second.isNotEmpty()

                if (isLoaded) {
                    Log.i(
                        TAG,
                        "Vector layer '${it.first.label}' loaded"
                    )
                } else {
                    Log.w(
                        TAG,
                        "Failed to load vector layer '${it.first.label}'"
                    )
                }

                isLoaded
            }
            .onEach {
                activeLayers.add(it.first)
            }
            .map {
                FeatureCollectionOverlay().apply {
                    setFeatures(
                        it.second,
                        it.first.layerStyle
                    )
                }
            }
            .toList()
    }

    companion object {
        private val TAG = LayerSettingsViewModel::class.java.name
    }

    /**
     * Default Factory to use for [LayerSettingsViewModel].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    class Factory(val creator: () -> LayerSettingsViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST") return creator() as T
        }
    }
}