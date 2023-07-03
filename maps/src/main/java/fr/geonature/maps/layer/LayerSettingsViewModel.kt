package fr.geonature.maps.layer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.maps.jts.geojson.io.WKTReader
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.settings.LayerType
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.overlay.feature.FeatureCollectionOverlay
import fr.geonature.maps.util.MapSettingsPreferencesUtils.getSelectedLayers
import fr.geonature.maps.util.MapSettingsPreferencesUtils.setSelectedLayers
import fr.geonature.maps.util.MapSettingsPreferencesUtils.setUseOnlineLayers
import fr.geonature.maps.util.MapSettingsPreferencesUtils.useOnlineLayers
import fr.geonature.mountpoint.util.FileUtils.getExternalStorageDirectory
import fr.geonature.mountpoint.util.FileUtils.getFile
import fr.geonature.mountpoint.util.MountPointUtils.getInternalStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
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
import org.osmdroid.views.overlay.Overlay
import org.tinylog.Logger
import java.io.File
import java.io.FileReader

/**
 * [LayerSettings] view model.
 *
 * @author S. Grimault
 */
class LayerSettingsViewModel(
    application: Application,
    private val baseTilesPath: String? = null
) : AndroidViewModel(application) {

    private var rootPath: File? = null
    private val selectedLayers: MutableMap<String, LayerSettings> = mutableMapOf()

    private val _tileProvider = MutableLiveData<MapTileProviderBase?>()
    val tileProvider: LiveData<MapTileProviderBase?> = _tileProvider

    private val _vectorOverlays = MutableLiveData<List<Overlay>>()
    val vectorOverlays: LiveData<List<Overlay>> = _vectorOverlays

    /**
     * Load and show selected layers on the map.
     */
    fun load(selectedLayersSettings: List<LayerSettings>) {
        Logger.info {
            "loading selected layers:\n${
                selectedLayersSettings.joinToString("\n") { "\t'${it.source}' (active: ${it.properties.active})" }
            }"
        }

        viewModelScope.launch {
            resolveRootPath()

            // only one online layer can be selected at a time
            val validLayersSettings =
                listOfNotNull(selectedLayersSettings.firstOrNull { it.isOnline() }) + selectedLayersSettings.filter { !it.isOnline() }

            // add first all inactive layers
            with(selectedLayers) {
                clear()
                putAll(validLayersSettings.filter { !it.properties.active }
                    .map {
                        Pair(
                            it.getPrimarySource(),
                            it
                        )
                    })
            }

            // load only active layers from selection
            val activeLayerSettings = validLayersSettings.filter { it.properties.active }

            val tileProvider = buildTileProvider(activeLayerSettings)
            val vectorOverlays = buildVectorOverlays(activeLayerSettings)

            // save current selection
            setUseOnlineLayers(getApplication(),
                selectedLayers.values.any { layer -> layer.isOnline() && layer.properties.active })
            setSelectedLayers(
                getApplication(),
                selectedLayers.values.toList()
            )

            _tileProvider.postValue(tileProvider)
            _vectorOverlays.postValue(vectorOverlays)
        }
    }

    /**
     * Gets selected layers.
     */
    fun getSelectedLayers(
        mapSettings: MapSettings,
        filter: (layerSettings: LayerSettings) -> Boolean = DEFAULT_LAYER_SETTINGS_FILTER
    ): List<LayerSettings> {
        val useOnlineLayers = useOnlineLayers(getApplication())

        val selectedLayers = (selectedLayers.values.takeIf { it.isNotEmpty() } ?: getSelectedLayers(
            getApplication(),
            mapSettings
        ).takeIf { it.isNotEmpty() } ?: mapSettings.layersSettings.filter { layerSettings ->
            (layerSettings.isOnline() && useOnlineLayers) || !layerSettings.isOnline()
        }).map { layerSettings ->
            if (layerSettings.isOnline()) {
                layerSettings.copy(
                    properties = layerSettings.properties.copy(
                        active = useOnlineLayers
                    )
                )
            } else {
                layerSettings.copy()
            }
        }

        if (filter === DEFAULT_LAYER_SETTINGS_FILTER) {
            return selectedLayers
        }

        return selectedLayers.filter(filter)
    }

    fun getActiveLayersOnZoomLevel(zoomLevel: Double): List<LayerSettings> {
        return selectedLayers.values.filter {
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
                        resolveLayerSettingsPath(it)
                    )
                }
                .onEach {
                    selectedLayers[it.first.getPrimarySource()] = it.first.copy(
                        properties = it.first.properties.copy(
                            active = it.second.isNotEmpty()
                        )
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

                    selectedLayers[layersSettings.getPrimarySource()] = layersSettings.copy()

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
                        resolveLayerSettingsPath(it)
                    )
                }
                .onEach {
                    selectedLayers[it.first.getPrimarySource()] = it.first.copy(
                        properties = it.first.properties.copy(
                            active = it.second.isNotEmpty()
                        )
                    )
                }
                .filter {
                    if (it.second.isEmpty()) {
                        Logger.warn { "cannot read vector layer '${it.first.label}'..." }
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
                .onEach {
                    selectedLayers[it.first.getPrimarySource()] =
                        it.first.copy(properties = it.first.properties.copy(active = it.second.isNotEmpty()))
                }
                .filter {
                    if (it.second.isEmpty()) {
                        Logger.warn { "cannot read vector layer '${it.first.label}'" }
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
                }
                .toList()
        }

    private suspend fun resolveRootPath(): File = withContext(Dispatchers.IO) {
        rootPath ?: (baseTilesPath?.split(File.separator)
            ?.filter { it.isNotBlank() } ?: emptyList()).let { segments ->
            if (segments.isEmpty()) {
                getExternalStorageDirectory(getApplication()).let {
                    if (it.exists() && it.canRead()) it
                    else getInternalStorage(getApplication()).mountPath
                }
            }
            // absolute path
            else if (baseTilesPath?.startsWith(File.separator) == true) {
                File(
                    segments.joinToString(
                        separator = File.separator,
                        prefix = File.separator
                    )
                )
            }
            // relative path
            else {
                // first: try to find tiles relative path from external storage
                (getFile(getExternalStorageDirectory(getApplication())).walkTopDown()
                    .filter { it.isDirectory }
                    .filter { segments.first() == it.name }
                    .find {
                        if (segments.size > 1) it.resolve(
                            segments.drop(1)
                                .joinToString(File.separator)
                        )
                            .let { relativeFile ->
                                relativeFile.exists() && relativeFile.canRead()
                            } else segments.first() == it.name
                    }
                // if not found, try to find tiles relative path from internal storage
                    ?: getInternalStorage(getApplication()).mountPath.walkTopDown()
                        .filter { it.isDirectory }
                        .filter { segments.first() == it.name }
                        .find {
                            if (segments.size > 1) it.resolve(
                                segments.drop(1)
                                    .joinToString(File.separator)
                            )
                                .let { relativeFile ->
                                    relativeFile.exists() && relativeFile.canRead()
                                } else segments.first() == it.name
                        })?.let {
                    if (segments.size > 1) it.resolve(
                        segments.drop(1)
                            .joinToString(File.separator)
                    ) else it
                }
                // use external storage as fallback
                    ?: getFile(getExternalStorageDirectory(getApplication()))
            }
        }
            .also {
                rootPath = it

                Logger.info {
                    "root path: '$it'"
                }
            }
    }

    private suspend fun resolveLayerSettingsPath(layerSettings: LayerSettings): List<File> =
        withContext(Dispatchers.IO) {
            if (layerSettings.isOnline()) {
                return@withContext emptyList()
            }

            val rootPath = resolveRootPath()

            rootPath.walkTopDown()
                .filter { f ->
                    f.isFile && f.canRead() && layerSettings.source.any { it == f.name }
                }
                .toList()
                .takeIf { it.isNotEmpty() } ?: getExternalStorageDirectory(getApplication()).also {
                Logger.warn {
                    "no layer '${layerSettings.label}' found from root path: '$rootPath', try to perform a deep scan from ${
                        if (it.absolutePath == getInternalStorage(getApplication()).mountPath.absolutePath) "internal"
                        else "external"
                    } storage '${it}'..."
                }
            }
                .walkTopDown()
                .filter { f ->
                    f.isFile && f.canRead() && layerSettings.source.any { it == f.name }
                }
                .toList()
                .takeIf { it.isNotEmpty() } ?: getInternalStorage(getApplication()).mountPath.let {
                if (it.absolutePath == getExternalStorageDirectory(getApplication()).absolutePath) null else it
            }
                ?.also {
                    Logger.warn {
                        "no layer '${layerSettings.label}' found from root path: '$rootPath', try to perform a deep scan from internal storage '${it}'..."
                    }
                }
                ?.walkTopDown()
                ?.filter { f ->
                    f.isFile && f.canRead() && layerSettings.source.any { it == f.name }
                }
                ?.toList() ?: run {
                Logger.warn {
                    "no layer '${layerSettings.label}' found from storage"
                }
                emptyList()
            }
        }

    companion object {
        private val DEFAULT_LAYER_SETTINGS_FILTER: (layerSettings: LayerSettings) -> Boolean =
            { true }
    }

    /**
     * Default Factory to use for [LayerSettingsViewModel].
     *
     * @author S. Grimault
     */
    class Factory(val creator: () -> LayerSettingsViewModel) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST") return creator() as T
        }
    }
}