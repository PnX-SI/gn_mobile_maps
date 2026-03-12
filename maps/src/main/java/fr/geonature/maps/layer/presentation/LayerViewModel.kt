package fr.geonature.maps.layer.presentation

import android.app.Application
import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.geonature.maps.jts.geojson.repository.IFeatureRepository
import fr.geonature.maps.layer.domain.LayerState
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.layer.repository.ILayerRepository
import fr.geonature.maps.layer.tilesource.TileSourceFactory
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.settings.LayerType
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.ui.overlay.feature.FeatureCollectionOverlay
import kotlinx.coroutines.Dispatchers
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
import java.util.Date
import javax.inject.Inject
import kotlin.collections.any
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * [LayerState] view model.
 *
 * @author S. Grimault
 */
@HiltViewModel
class LayerViewModel @Inject constructor(
    application: Application,
    private val featureRepository: IFeatureRepository,
    private val layerRepository: ILayerRepository
) : AndroidViewModel(application) {

    private val _allLayers = MutableLiveData<List<LayerState>>()
    val allLayers: LiveData<List<LayerState>> = _allLayers

    private val _selectedLayers = MutableLiveData<Set<LayerState.SelectedLayer>>()
    val selectedLayers: LiveData<Set<LayerState.SelectedLayer>> = _selectedLayers

    private val _tileProvider = MutableLiveData<MapTileProviderBase?>()
    val tileProvider: LiveData<MapTileProviderBase?> = _tileProvider

    private val _vectorOverlays = MutableLiveData<List<Overlay>>()
    val vectorOverlays: LiveData<List<Overlay>> = _vectorOverlays

    private val _zoomToBoundingBox = MutableLiveData<BoundingBox?>()
    val zoomToBoundingBox: LiveData<BoundingBox?> = _zoomToBoundingBox

    private val layers = mutableSetOf<LayerState>()

    /**
     * Whether we want to center and zoom according to this layer bounds.
     */
    private var centerAndZoomOnSelectedLayer: LayerState.SelectedLayer? = null

    /**
     * Loads and prepare all layers defined in [MapSettings].
     */
    fun init(mapSettings: MapSettings) {
        Logger.info {
            "preparing all layers:\n${
                mapSettings.layersSettings.joinToString(
                    separator = "\n",
                    postfix = ",\nusing online layers: ${mapSettings.useOnlineLayers}"
                ) { "\t'${it.label}': ${it.source}" }
            }..."
        }

        // step 1: initialize every layer as LayerState.Loading and publish immediately
        val loadingStates = mapSettings.layersSettings.map { LayerState.Loading(it) }
        with(layers) {
            clear()
            addAll(loadingStates)
        }
        _allLayers.postValue(layers.toList())

        viewModelScope.launch {
            // step 2: resolve the previously-persisted selection so we can promote layers later
            val persistedSelectedSources = layerRepository.getSelectedLayers()
                .flatMap { it.getLayerSettings().source }
                .toSet()

            // step 3: resolve each LayerSettings one by one, posting updates as we go
            mapSettings.layersSettings.forEach { layerSettings ->
                val resolved = layerRepository.prepareLayerFromSettings(
                    layerSettings,
                    mapSettings.baseTilesPath
                )

                with(layers) {
                    removeAll { it.isSame(resolved) }
                    add(resolved)
                }

                _allLayers.postValue(layers.toList())
            }

            // step 4: now that all layers are resolved, restore or pick the default selection
            val allValidLayers = layers.filterIsInstance<LayerState.Layer>()

            val existingSelectedLayers = allValidLayers
                .filter { layer -> layer.getLayerSettings().source.any { it in persistedSelectedSources } }
                .map { layer ->
                    layer.select()
                        .copy(active = if (mapSettings.useOnlineLayers) true else if (layer.settings.isOnline()) false else true)
                }

            val selectedLayers = if (existingSelectedLayers.isNotEmpty()) {
                Logger.info {
                    "existing selected layers:\n${
                        existingSelectedLayers.joinToString(separator = "\n") { "\t'${it.settings.label}': ${it.source}, (active: ${it.active})" }
                    }"
                }
                existingSelectedLayers
            } else {
                val onlineCandidates: List<LayerState.Layer> =
                    if (mapSettings.useOnlineLayers) listOfNotNull(
                        allValidLayers.firstOrNull { it.settings.isOnline() && it.settings.properties.shownByDefault }
                            ?: allValidLayers.firstOrNull { it.settings.isOnline() })
                    else emptyList()
                val localCandidates: List<LayerState.Layer> =
                    allValidLayers.filter { !it.settings.isOnline() && it.settings.properties.shownByDefault }
                        .takeIf { it.isNotEmpty() }
                        ?: listOfNotNull(allValidLayers.firstOrNull { !it.settings.isOnline() })
                (onlineCandidates + localCandidates).map { it.select() }
            }

            // replace resolved layers with their selected counterparts
            with(layers) {
                retainAll { layer -> selectedLayers.none { it.isSame(layer) } }
                addAll(selectedLayers)
            }
            layerRepository.setSelectedLayers(selectedLayers)

            _allLayers.postValue(layers.toList())
            _selectedLayers.postValue(layers.filterIsInstance<LayerState.SelectedLayer>().toSet())
        }
    }

    /**
     * Whether we want to use online layers.
     */
    fun useOnlineLayers(useOnlineLayers: Boolean) {
        layers.map {
            when (it) {
                is LayerState.Loading -> it
                is LayerState.Layer -> it.copy(active = if (it.settings.isOnline()) useOnlineLayers else true)
                is LayerState.SelectedLayer -> if (it.settings.isOnline() && !useOnlineLayers) it.toLayer().copy(active = false) else it
                is LayerState.Error -> it
            }
        }
            .also {
                with(this@LayerViewModel.layers) {
                    clear()
                    addAll(it)
                }
            }
    }

    /**
     * Loads and show selected layers on the map.
     */
    suspend fun load(
        selectedLayers: List<LayerState.SelectedLayer>,
        forceReload: Boolean = false
    ): List<LayerState> {
        if (selectedLayers.isNotEmpty()) {
            Logger.info {
                "loading selected layers:\n${
                    selectedLayers.joinToString("\n") { "\t'${it.settings.label}': ${it.source}" }
                }"
            }
        }

        layers.map {
            when (it) {
                is LayerState.Loading -> it
                is LayerState.Layer -> it
                is LayerState.SelectedLayer -> if (selectedLayers.any { selectedLayer -> selectedLayer.isSame(it) }) it else it.toLayer()
                is LayerState.Error -> it
            }
        }
            .also {
                with(this@LayerViewModel.layers) {
                    clear()
                    addAll(it)
                }
            }

        val validLayers =
            // only one online layer can be selected at a time
            listOfNotNull(selectedLayers.firstOrNull { it.settings.isOnline() && it.active }) +
                // and load all valid local layers
                selectedLayers.filter { !it.settings.isOnline() && it.source.isNotEmpty() && it.active }

        val tileProvider = buildTileProvider(validLayers)
        val vectorOverlays = buildVectorOverlays(
            validLayers,
            forceReload
        )

        layerRepository.setSelectedLayers(layers.filterIsInstance<LayerState.SelectedLayer>())

        _allLayers.postValue(layers.toList())
        _selectedLayers.postValue(layers.filterIsInstance<LayerState.SelectedLayer>().toSet())

        _tileProvider.postValue(tileProvider)
        _vectorOverlays.postValue(vectorOverlays)

        return layers.toList()
    }

    /**
     * Adds new layer to show on the map.
     */
    fun addLayer(uri: Uri): LiveData<LayerState> = liveData {
        val selectedLayers = layerRepository.getSelectedLayers()
        val newLayer = layerRepository.addLayerFromURI(uri)

        if (newLayer is LayerState.Error) {
            emit(newLayer)
            return@liveData
        }

        (newLayer as? LayerState.SelectedLayer ?: (newLayer as LayerState.Layer).select()).also {
            centerAndZoomOnSelectedLayer = it

            with(layers) {
                retainAll { layer -> !layer.isSame(it) }
                add(it)
            }

            val loadedLayer = load(
                (selectedLayers.filterNot { layer -> layer.isSame(it) } + listOf(it)),
                forceReload = true,
            ).firstOrNull { layer -> layer.isSame(it) } ?: newLayer

            if (loadedLayer is LayerState.Error) {
                layers.retainAll { layer -> !layer.isSame(loadedLayer) }
            }

            _allLayers.postValue(layers.toList())
            _selectedLayers.postValue(layers.filterIsInstance<LayerState.SelectedLayer>().toSet())

            emit(loadedLayer)
        }
    }

    fun getAllLayers(): List<LayerState> {
        return layers.toList()
    }

    fun getSelectedLayers(): List<LayerState.SelectedLayer> {
        return layers.filterIsInstance<LayerState.SelectedLayer>()
    }

    fun getActiveLayersOnZoomLevel(zoomLevel: Double): List<LayerState.SelectedLayer> {
        val selectedLayers = getSelectedLayers()

        return selectedLayers.filter {
            it.settings.properties.minZoomLevel.toDouble()
                .coerceAtLeast(0.0)
                .rangeTo(it.settings.properties.maxZoomLevel.toDouble()
                    .takeIf { d -> d >= 0.0 } ?: Double.MAX_VALUE)
                .contains(zoomLevel)
        }
    }

    private suspend fun buildTileProvider(layers: List<LayerState.SelectedLayer>): MapTileProviderBase? =
        withContext(Dispatchers.IO) {
            val registerReceiver = SimpleRegisterReceiver(getApplication())

            val offlineTileSources = layers.asSequence()
                .filter { it.settings.getType() == LayerType.TILES }
                .filter { !it.settings.isOnline() }
                .map { layer ->
                    Logger.info { "loading local tiles layer '${layer.settings.label}'..." }

                    val asFiles = layer.source.map { uri ->
                        runCatching { uri.toFile() }
                    }

                    val result =
                        if (asFiles.none { it.isSuccess }) LayerState.Error(LayerException.IOException(layer.settings,
                            asFiles.firstOrNull { it.isFailure }
                                ?.exceptionOrNull()))
                        else layer

                    with(this@LayerViewModel.layers) {
                        retainAll { layer -> !layer.isSame(result) }
                        add(result)
                    }

                    Pair(
                        layer,
                        asFiles.mapNotNull { it.getOrNull() },
                    )
                }
                .mapNotNull { pair ->
                    pair.second.firstOrNull()
                        ?.let { pair.first to it }
                }
                .onEach { Logger.info { "local tiles layer '${it.first.settings.label}' loaded" } }
                .toList()

            val onlineTileSource = layers.find { it.settings.isOnline() }
                ?.let { layer ->
                    val onlineTileSource = runCatching {
                        TileSourceFactory.getOnlineTileSource(
                            getApplication(),
                            layer.settings
                        )
                    }.onFailure { e ->
                        Logger.warn {
                            e.message
                                ?: "failed to find the corresponding online tile source from online layer '${layer.settings.label}'"
                        }

                        val error = LayerState.Error(
                            if (e is LayerException) e
                            else LayerException.InvalidOnlineLayerException(
                                layer.settings,
                                e
                            )
                        )

                        with(this@LayerViewModel.layers) {
                            retainAll { layer -> !layer.isSame(error) }
                            add(error)
                        }
                    }
                        .getOrNull() ?: return@let null

                    Logger.info { "loading online layer '${layer.settings.label}'..." }

                    this@LayerViewModel.layers.map {
                        when (it) {
                            is LayerState.Loading -> it
                            is LayerState.Layer -> if (it.isSame(layer)) layer else it
                            is LayerState.SelectedLayer -> if (it.isSame(layer)) layer else if (it.getLayerSettings()
                                    .isOnline()
                            ) it.toLayer() else it

                            is LayerState.Error -> if (it.isSame(layer)) layer else it
                        }
                    }
                        .also {
                            with(this@LayerViewModel.layers) {
                                clear()
                                addAll(it)
                            }
                        }

                    Pair(
                        layer,
                        onlineTileSource
                    )
                } ?: return@withContext if (offlineTileSources.isEmpty()) null
            else OfflineTileProvider(registerReceiver,
                offlineTileSources.map { it.second }
                    .toTypedArray())

            val cacheProvider = MapTileSqlCacheProvider(
                registerReceiver,
                onlineTileSource.second
            )

            val offlineTileProvider = MapTileFileArchiveProvider(registerReceiver,
                onlineTileSource.second,
                offlineTileSources.map { ArchiveFileFactory.getArchiveFile(it.second) }
                    .toTypedArray())

            val approximationProvider = MapTileApproximater()
            approximationProvider.addProvider(cacheProvider)
            approximationProvider.addProvider(offlineTileProvider)

            val onlineTileProvider = MapTileDownloader(
                onlineTileSource.second,
                SqlTileWriter(),
                NetworkAvailabliltyCheck(getApplication())
            )

            MapTileProviderArray(
                onlineTileSource.second,
                registerReceiver,
                (if (offlineTileSources.isEmpty()) arrayOf<MapTileModuleProviderBase>(
                    cacheProvider
                )
                else emptyArray()) + arrayOf(
                    offlineTileProvider,
                    approximationProvider,
                    onlineTileProvider,
                )
            )
        }

    private suspend fun buildVectorOverlays(
        layers: List<LayerState.SelectedLayer>,
        forceReload: Boolean = false
    ): List<Overlay> = withContext(Dispatchers.IO) {
        // adds already loaded layers
        (_vectorOverlays.value
            ?: emptyList()).filter { overlay -> overlay is FeatureCollectionOverlay && layers.any { it.settings.label == overlay.name } && !forceReload } +

            layers.asSequence()
                // keep only layers type as vector
                .filter { it.settings.getType() == LayerType.VECTOR }
                // keep only layers not already loaded on the map
                .filter {
                    (_vectorOverlays.value
                        ?: emptyList()).none { overlay -> overlay is FeatureCollectionOverlay && it.settings.label == overlay.name } || forceReload
                }
                // build a triple with layer, sources as files and the start time for metrics
                .map { layer ->
                    val startTime = Date()
                    Logger.info { "loading vector layer '${layer.settings.label}'..." }

                    val asFiles = layer.source.map { uri ->
                        runCatching { uri.toFile() }
                    }

                    val result =
                        if (asFiles.none { it.isSuccess }) LayerState.Error(LayerException.IOException(layer.settings,
                            asFiles.firstOrNull { it.isFailure }
                                ?.exceptionOrNull()))
                        else layer

                    with(this@LayerViewModel.layers) {
                        retainAll { layer -> !layer.isSame(result) }
                        add(result)
                    }

                    Triple(
                        layer,
                        asFiles.mapNotNull { it.getOrNull() },
                        startTime
                    )
                }
                // keep only triples with valid files (at least one)
                .filter {
                    if (it.second.isEmpty()) {
                        Logger.warn { "cannot read vector layer '${it.first.settings.label}': no source defined..." }
                    }

                    it.second.isNotEmpty()
                }
                // load features from files
                .map { triple ->
                    val featuresResult =
                        featureRepository.loadFeatures(*triple.second.toTypedArray())

                    with(this@LayerViewModel.layers) {
                        retainAll { layer -> !layer.isSame(triple.first) }
                        add(
                            if (featuresResult.isSuccess) triple.first else LayerState.Error(
                                LayerException.IOException(
                                    triple.first.settings,
                                    featuresResult.exceptionOrNull()
                                )
                            )
                        )
                    }

                    Triple(
                        triple.first,
                        featuresResult.getOrElse { emptyList() },
                        triple.third
                    )
                }
                // keep only triples with valid features (at least one)
                .filter {
                    if (it.second.isEmpty()) {
                        Logger.warn { "cannot read vector layer '${it.first.settings.label}': no feature loaded" }
                    }

                    it.second.isNotEmpty()
                }
                .map {
                    Logger.info {
                        "vector layer '${it.first.settings.label}' loaded (took ${
                            (Date().time - it.third.time).toDuration(
                                DurationUnit.MILLISECONDS
                            )
                        })"
                    }

                    FeatureCollectionOverlay(it.first.settings.label).apply {
                        setFeatures(
                            it.second,
                            it.first.settings.properties.style ?: LayerStyleSettings()
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