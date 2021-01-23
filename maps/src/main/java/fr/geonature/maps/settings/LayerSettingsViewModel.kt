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
import org.osmdroid.tileprovider.MapTileProviderArray
import org.osmdroid.tileprovider.MapTileProviderBase
import org.osmdroid.tileprovider.modules.ArchiveFileFactory
import org.osmdroid.tileprovider.modules.MapTileApproximater
import org.osmdroid.tileprovider.modules.MapTileDownloader
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider
import org.osmdroid.tileprovider.modules.MapTileSqlCacheProvider
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.modules.SqlTileWriter
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
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

    private val layers: MutableList<LayerSettings> = mutableListOf()
    private val activeLayers: MutableList<LayerSettings> = mutableListOf()

    private val _tileProvider = MutableLiveData<MapTileProviderBase>()
    val tileProvider: LiveData<MapTileProviderBase> = _tileProvider

    private val _vectorOverlays = MutableLiveData<List<Overlay>>()
    val vectorOverlays: LiveData<List<Overlay>> = _vectorOverlays

    /**
     * Sets available layers.
     */
    fun setLayersSettings(
        layersSettings: List<LayerSettings>,
        useDefaultOnlineTileSource: Boolean = true
    ) {
        with(layers) {
            clear()
            addAll((if (useDefaultOnlineTileSource) mutableListOf<OnlineTileSourceBase>(TileSourceFactory.DEFAULT_TILE_SOURCE) else emptyList()).map {
                LayerSettings.Builder()
                    .label(it.name())
                    .source(it.baseUrl)
                    .properties(
                        LayerPropertiesSettings.Builder.newInstance()
                            .minZoomLevel(0)
                            .maxZoomLevel(19)
                            .tileSizePixels(256)
                            .tileMimeType("image/png")
                            .attribution(it.copyrightNotice)
                            .build()
                    )
                    .build()
            } + layersSettings)
        }
    }

    /**
     * Gets available layers.
     */
    fun getLayerSettings(): List<LayerSettings> {
        return this.layers
    }

    /**
     * Load and show selected layers on the map.
     */
    fun load(selectedLayersSettings: List<LayerSettings>) {
        viewModelScope.launch {
            var rootPath = if (baseTilesPath?.startsWith("/") == true) {
                File(baseTilesPath)
            } else {
                getFile(
                    getExternalStorageDirectory(getApplication()),
                    baseTilesPath ?: ""
                )
            }

            if (!rootPath.canRead()) {
                Log.w(
                    TAG,
                    "Cannot access to '$rootPath'..."
                )
            }

            rootPath =
                if (rootPath.canRead()) rootPath else getExternalStorageDirectory(getApplication())

            activeLayers.clear()

            val tileProvider = buildTileProvider(
                rootPath,
                selectedLayersSettings
            )
            val vectorOverlays = buildVectorOverlays(
                rootPath,
                selectedLayersSettings
            )

            _tileProvider.postValue(tileProvider)
            _vectorOverlays.postValue(vectorOverlays)
        }
    }

    /**
     * Gets active layers (i.e. visible on the map).
     */
    fun getActiveLayers(filter: (layerSettings: LayerSettings) -> Boolean = DEFAULT_LAYER_SETTINGS_FILTER): List<LayerSettings> {
        if (filter === DEFAULT_LAYER_SETTINGS_FILTER) {
            return activeLayers
        }

        return activeLayers.filter(filter)
    }

    private suspend fun buildTileProvider(
        rootPath: File,
        layersSettings: List<LayerSettings>
    ): MapTileProviderBase? = withContext(Dispatchers.IO) {
        val registerReceiver = SimpleRegisterReceiver(getApplication())

        val offlineTileSources = layersSettings.asSequence()
            .filter { it.getType() == LayerType.TILES }
            .filter { !it.isOnline() }
            .map {
                Log.i(
                    TAG,
                    "Loading local tiles layer '${it.label}'..."
                )

                Pair(it,
                    rootPath.walkTopDown()
                        .firstOrNull { f -> f.isFile && f.name == it.source })
            }
            .filter { it.second != null }
            .filter {
                val canRead = it.second!!.canRead()

                if (!canRead) {
                    Log.w(
                        TAG,
                        "Cannot access to '${it.second}'..."
                    )
                }

                canRead
            }
            .onEach {
                activeLayers.add(it.first)
            }
            .map { it.second }
            .toList()

        val onlineTileSource = layersSettings.find { it.isOnline() && it.properties != null }
            ?.let {
                activeLayers.add(
                    0,
                    it
                )

                it.properties!!

                XYTileSource(
                    it.label,
                    it.properties.minZoomLevel,
                    it.properties.maxZoomLevel,
                    it.properties.tileSizePixels,
                    ".${it.properties.tileMimeType?.substringAfter("image/")}",
                    arrayOf(it.source),
                    it.properties.attribution
                )
            } ?: return@withContext if (offlineTileSources.isEmpty()) null else OfflineTileProvider(
            registerReceiver,
            offlineTileSources.toTypedArray()
        )

        val cacheProvider = MapTileSqlCacheProvider(
            registerReceiver,
            onlineTileSource
        )

        val offlineTileProvider = MapTileFileArchiveProvider(
            registerReceiver,
            onlineTileSource,
            offlineTileSources.map { ArchiveFileFactory.getArchiveFile(it) }
                .toTypedArray()
        )

        val approximationProvider = MapTileApproximater()
        approximationProvider.addProvider(cacheProvider)
        approximationProvider.addProvider(offlineTileProvider)

        val onlineTileProvider = MapTileDownloader(
            onlineTileSource,
            SqlTileWriter(),
            NetworkAvailabliltyCheck(getApplication())
        )

        MapTileProviderArray(
            onlineTileSource,
            registerReceiver,
            arrayOf(
                cacheProvider,
                offlineTileProvider,
                approximationProvider,
                onlineTileProvider,
            )
        )
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
                        it.first.properties?.style ?: LayerStyleSettings()
                    )
                }
            }
            .toList()
    }

    companion object {
        private val TAG = LayerSettingsViewModel::class.java.name

        private val DEFAULT_LAYER_SETTINGS_FILTER: (layerSettings: LayerSettings) -> Boolean =
            { true }
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