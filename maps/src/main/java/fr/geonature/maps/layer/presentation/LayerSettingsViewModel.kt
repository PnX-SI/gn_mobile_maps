package fr.geonature.maps.layer.presentation

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.maps.jts.geojson.io.WKTReader
import fr.geonature.maps.layer.repository.ILayerRepository
import fr.geonature.maps.layer.tilesource.TileSourceFactory
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.settings.LayerType
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.overlay.feature.FeatureCollectionOverlay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.tileprovider.MapTileProviderArray
import org.osmdroid.tileprovider.MapTileProviderBase
import org.osmdroid.tileprovider.modules.ArchiveFileFactory
import org.osmdroid.tileprovider.modules.MapTileApproximater
import org.osmdroid.tileprovider.modules.MapTileDownloader
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase
import org.osmdroid.tileprovider.modules.MapTileSqlCacheProvider
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck
import org.osmdroid.tileprovider.modules.OfflineTileProvider
import org.osmdroid.tileprovider.modules.SqlTileWriter
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.overlay.Overlay
import org.tinylog.Logger
import java.io.FileReader
import javax.inject.Inject

/**
 * [LayerSettings] view model.
 *
 * @author S. Grimault
 */
@HiltViewModel
class LayerSettingsViewModel @Inject constructor(
    application: Application,
    private val layerRepository: ILayerRepository
) : AndroidViewModel(application) {

    private val _tileProvider = MutableLiveData<MapTileProviderBase?>()
    val tileProvider: LiveData<MapTileProviderBase?> = _tileProvider

    private val _vectorOverlays = MutableLiveData<List<Overlay>>()
    val vectorOverlays: LiveData<List<Overlay>> = _vectorOverlays

    private val _zoomToBoundingBox = MutableLiveData<BoundingBox?>()
    val zoomToBoundingBox: LiveData<BoundingBox?> = _zoomToBoundingBox

    /**
     * Whether we want to center and zoom according to this layer bounds.
     */
    private var centerAndZoomOnSelectedLayer: LayerSettings? = null

    /**
     * Loads and prepare all layers defined in [MapSettings].
     */
    fun init(mapSettings: MapSettings) {
        Logger.info {
            "preparing all layers:\n${
                mapSettings.layersSettings.joinToString(
                    separator = "\n",
                    postfix = ",\nusing online layers: ${mapSettings.useOnlineLayers}"
                ) { "\t'${it.label}': ${it.source}' (active: ${it.properties.active})" }
            }..."
        }

        viewModelScope.launch {
            // loads and prepare all layers
            val allLayers = layerRepository.prepareLayers(
                mapSettings.layersSettings,
                mapSettings.baseTilesPath
            )
                .getOrDefault(emptyList())

            // loads selected layers to show on the map or use the first eligible layer
            layerRepository.getSelectedLayers()
                .getOrDefault(emptyList())
                .also {
                    if (it.isEmpty() && mapSettings.useOnlineLayers) {
                        listOfNotNull((allLayers.firstOrNull { layer -> layer.isOnline() }
                            ?: allLayers.firstOrNull { layer -> !layer.isOnline() })?.also { layer ->
                            layerRepository.setSelectedLayers(listOf(layer))
                        })
                    }
                }
        }
    }

    /**
     * Load and show selected layers on the map.
     */
    fun load(selectedLayersSettings: List<LayerSettings>) {
        Logger.info {
            "loading selected layers:\n${
                selectedLayersSettings.joinToString("\n") { "\t'${it.label}': ${it.source} (active: ${it.properties.active})" }
            }"
        }

        viewModelScope.launch {
            val validLayersSettings =
                // only one online layer can be selected at a time
                listOfNotNull(selectedLayersSettings.firstOrNull { it.isOnline() }) +
                    // and load all valid local layers
                    selectedLayersSettings.filter {
                        !it.isOnline() && it.getSourcesAsUri()
                            .isNotEmpty()
                    }

            // load only active layers from selection
            val activeLayerSettings = validLayersSettings.filter { it.properties.active }

            val tileProvider = buildTileProvider(activeLayerSettings)
            val vectorOverlays = buildVectorOverlays(activeLayerSettings)

            layerRepository.setSelectedLayers(selectedLayersSettings)

            _tileProvider.postValue(tileProvider)
            _vectorOverlays.postValue(vectorOverlays)
        }
    }

    /**
     * Adds new layer to show on the map.
     */
    fun addLayer(uri: Uri) {
        viewModelScope.launch {
            val selectedLayers = layerRepository.getSelectedLayers()
                .getOrDefault(emptyList())

            layerRepository.addLayerFromURI(uri)
                .getOrNull()
                ?.also {
                    centerAndZoomOnSelectedLayer = it
                    load((selectedLayers + listOf(it)).distinctBy { layer -> layer.getPrimarySource() })
                }
        }
    }

    suspend fun getAllLayers(): List<LayerSettings> {
        return layerRepository.getAllLayers()
            .getOrDefault(emptyList())
    }

    suspend fun getSelectedLayers(): List<LayerSettings> {
        return layerRepository.getSelectedLayers()
            .getOrDefault(emptyList())
    }

    suspend fun getActiveLayersOnZoomLevel(zoomLevel: Double): List<LayerSettings> {
        val selectedLayers = layerRepository.getSelectedLayers()
            .getOrDefault(emptyList())

        return selectedLayers.filter {
            it.properties.active && it.properties.minZoomLevel.toDouble()
                .coerceAtLeast(0.0)
                .rangeTo(it.properties.maxZoomLevel.toDouble()
                    .takeIf { d -> d >= 0.0 } ?: Double.MAX_VALUE)
                .contains(zoomLevel)
        }
    }

    private suspend fun buildTileProvider(layersSettings: List<LayerSettings>): MapTileProviderBase? =
        withContext(Dispatchers.IO) {
            val registerReceiver = SimpleRegisterReceiver(getApplication())

            val offlineTileSources = layersSettings.asFlow()
                .filter { it.getType() == LayerType.TILES }
                .filter { !it.isOnline() }
                .map {
                    Logger.info { "loading local tiles layer '${it.label}'..." }

                    Pair(
                        it,
                        it.getSourcesAsUri()
                            .mapNotNull { uri -> runCatching { uri.toFile() }.getOrNull() },
                    )
                }
                .map {
                    Logger.info { "local tiles layer '${it.first.label}' loaded" }

                    it.second
                }
                .toList()
                .flatten()

            val onlineTileSource = layersSettings.find { it.isOnline() }
                ?.let { layersSettings ->
                    val onlineTileSource = runCatching {
                        TileSourceFactory.getOnlineTileSource(
                            getApplication(),
                            layersSettings
                        )
                    }.onFailure {
                        Logger.warn {
                            it.message
                                ?: "failed to find the corresponding online tile source from online layer '${layersSettings.label}'"
                        }
                    }
                        .getOrNull() ?: return@let null

                    Logger.info { "loading online layer '${layersSettings.label}'..." }

                    onlineTileSource
                }
                ?: return@withContext if (offlineTileSources.isEmpty()) null else OfflineTileProvider(
                    registerReceiver,
                    offlineTileSources.toTypedArray()
                )

            val cacheProvider = MapTileSqlCacheProvider(
                registerReceiver,
                onlineTileSource
            )

            val offlineTileProvider = MapTileFileArchiveProvider(registerReceiver,
                onlineTileSource,
                offlineTileSources.map { ArchiveFileFactory.getArchiveFile(it) }
                    .toTypedArray())

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
                (if (offlineTileSources.isEmpty()) arrayOf<MapTileModuleProviderBase>(cacheProvider)
                else emptyArray()) + arrayOf(
                    offlineTileProvider,
                    approximationProvider,
                    onlineTileProvider,
                )
            )
        }

    private suspend fun buildVectorOverlays(layersSettings: List<LayerSettings>): List<Overlay> =
        withContext(Dispatchers.IO) {
            layersSettings.asFlow()
                .filter { it.getType() == LayerType.VECTOR }
                .map {
                    Logger.info { "loading vector layer '${it.label}'..." }

                    Pair(
                        it,
                        it.getSourcesAsUri()
                            .mapNotNull { uri -> runCatching { uri.toFile() }.getOrNull() },
                    )
                }
                .filter {
                    if (it.second.isEmpty()) {
                        Logger.warn { "cannot read vector layer '${it.first.label}': no source defined..." }
                    }

                    it.second.isNotEmpty()
                }
                .map {
                    it.first to it.second.map { file ->
                        when (file.extension) {
                            "geojson", "json" -> runCatching {
                                GeoJsonReader().read(FileReader(file))
                            }.onFailure {
                                Logger.warn { "failed to load vector layer from file '${file.name}'" }
                            }
                                .getOrDefault(emptyList())

                            "wkt" -> runCatching {
                                WKTReader().readFeatures(FileReader(file))
                            }.onFailure {
                                Logger.warn { "failed to load vector layer from file '${file.name}'" }
                            }
                                .getOrDefault(emptyList())

                            else -> {
                                Logger.warn { "unsupported vector layer '${file.name}'" }

                                emptyList()
                            }
                        }
                    }
                        .filter { features -> features.isNotEmpty() }
                }
                .filter {
                    if (it.second.isEmpty()) {
                        Logger.warn { "cannot read vector layer '${it.first.label}': no feature loaded" }
                    }

                    it.second.isNotEmpty()
                }
                .map {
                    Logger.info { "vector layer '${it.first.label}' loaded" }

                    FeatureCollectionOverlay(it.first.label).apply {
                        setFeatures(
                            it.second.flatten(),
                            it.first.properties.style ?: LayerStyleSettings()
                        )
                    }
                        .also { overlay ->
                            if (it.first == centerAndZoomOnSelectedLayer) {
                                centerAndZoomOnSelectedLayer = null
                                _zoomToBoundingBox.postValue(overlay.bounds)
                            }
                        }
                }
                .toList()
        }
}