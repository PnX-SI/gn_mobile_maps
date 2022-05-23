package fr.geonature.maps.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.maps.jts.geojson.io.WKTReader
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
import org.osmdroid.tileprovider.tilesource.XYTileSource
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
class LayerSettingsViewModel(application: Application, private val baseTilesPath: String? = null) :
    AndroidViewModel(application) {

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
                            it.source,
                            it
                        )
                    })
            }

            // load only active layers from selection
            val activeLayerSettings = validLayersSettings.filter { it.properties.active }

            val tileProvider = buildTileProvider(activeLayerSettings)
            val vectorOverlays = buildVectorOverlays(activeLayerSettings)

            // save current selection
            setUseOnlineLayers(
                getApplication(),
                selectedLayers.values.any { layer -> layer.isOnline() && layer.properties.active }
            )
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
                layerSettings.copy(properties = layerSettings.properties.copy(active = useOnlineLayers))
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
            it.properties.active &&
                it.properties.minZoomLevel.toDouble()
                    .coerceAtLeast(0.0)
                    .rangeTo(
                        it.properties.maxZoomLevel.toDouble()
                            .takeIf { d -> d >= 0.0 } ?: Double.MAX_VALUE
                    )
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
                    Logger.info { "loading local tiles layer '${it.source}'..." }

                    Pair(
                        it,
                        resolveLayerSettingsPath(it)
                    )
                }
                .onEach {
                    selectedLayers[it.first.source] =
                        it.first.copy(properties = it.first.properties.copy(active = it.second != null))
                }
                .filter {
                    val canRead = it.second?.canRead() == true

                    if (!canRead) {
                        Logger.warn { "cannot read local tiles layer '${it.first.source}'${if (it.second == null) "" else " (${it.second})"}..." }
                    }

                    canRead
                }
                .map {
                    Logger.info { "local tiles layer '${it.first.source}' loaded" }

                    it.second!!
                }
                .toList()

            val onlineTileSource = layersSettings.find { it.isOnline() }
                ?.let {
                    selectedLayers[it.source] = it.copy()

                    Logger.info { "loading online layer '${it.source}'..." }

                    XYTileSource(
                        it.label,
                        it.properties.minZoomLevel,
                        it.properties.maxZoomLevel,
                        it.properties.tileSizePixels,
                        ".${it.properties.tileMimeType?.substringAfter("image/")}",
                        arrayOf(
                            it.source.removeSuffix("/")
                                .plus("/")
                        ),
                        it.properties.attribution
                    )
                }
                ?: return@withContext if (offlineTileSources.isEmpty()) null else OfflineTileProvider(
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
                (if (offlineTileSources.isEmpty()) arrayOf<MapTileModuleProviderBase>(cacheProvider)
                else emptyArray()) +
                    arrayOf(
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
                    Logger.info { "loading vector layer '${it.source}'..." }

                    Pair(
                        it,
                        resolveLayerSettingsPath(it)
                    )
                }
                .onEach {
                    selectedLayers[it.first.source] =
                        it.first.copy(properties = it.first.properties.copy(active = it.second != null))
                }
                .filter {
                    if (it.second == null) {
                        Logger.warn { "cannot read vector layer '${it.first.source}'..." }
                    }

                    it.second != null
                }
                .map {
                    when (it.second?.extension) {
                        "geojson", "json" -> Pair(
                            it.first,
                            runCatching { GeoJsonReader().read(FileReader(it.second)) }.getOrNull()
                        )
                        "wkt" -> Pair(
                            it.first,
                            runCatching { WKTReader().readFeatures(FileReader(it.second)) }.getOrNull()
                        )
                        else -> {
                            Logger.warn { "unsupported vector layer '${it.first.source}'" }

                            Pair(
                                it.first,
                                null
                            )
                        }
                    }
                }
                .onEach {
                    selectedLayers[it.first.source] =
                        it.first.copy(properties = it.first.properties.copy(active = !it.second.isNullOrEmpty()))
                }
                .filter {
                    if (it.second == null) {
                        Logger.warn { "cannot read vector layer '${it.first.source}'" }
                    }

                    it.second != null
                }
                .filter {
                    val isLoaded = it.second!!.isNotEmpty()

                    if (!isLoaded) {
                        Logger.warn { "empty vector layer '${it.first.source}'" }
                    }

                    isLoaded
                }
                .map {
                    Logger.info { "vector layer '${it.first.source}' loaded" }

                    FeatureCollectionOverlay().apply {
                        name = it.first.label
                        setFeatures(
                            it.second!!,
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
            else if (segments.isNotEmpty() && baseTilesPath?.startsWith(File.separator) == true) {
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
                (getFile(getExternalStorageDirectory(getApplication()))
                    .walkTopDown()
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
                    ?: getInternalStorage(getApplication()).mountPath
                        .walkTopDown()
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

    private suspend fun resolveLayerSettingsPath(layerSettings: LayerSettings): File? =
        withContext(Dispatchers.IO) {
            if (layerSettings.isOnline()) {
                return@withContext null
            }

            val rootPath = resolveRootPath()

            rootPath.walkTopDown()
                .firstOrNull { f -> f.isFile && f.name == layerSettings.source && f.canRead() }
                ?: getExternalStorageDirectory(getApplication())
                    .also {
                        Logger.warn {
                            "no layer '${layerSettings.source}' found from root path: '$rootPath', try to perform a deep scan from ${if (it.absolutePath == getInternalStorage(getApplication()).mountPath.absolutePath) "internal" else "external"} storage '${it}'..."
                        }
                    }
                    .walkTopDown()
                    .firstOrNull { f -> f.isFile && f.name == layerSettings.source && f.canRead() }
                ?: getInternalStorage(getApplication()).mountPath.let {
                    if (it.absolutePath == getExternalStorageDirectory(getApplication()).absolutePath) null else it
                }
                    ?.also {
                        Logger.warn {
                            "no layer '${layerSettings.source}' found from root path: '$rootPath', try to perform a deep scan from internal storage '${it}'..."
                        }
                    }
                    ?.walkTopDown()
                    ?.firstOrNull { f -> f.isFile && f.name == layerSettings.source && f.canRead() }
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