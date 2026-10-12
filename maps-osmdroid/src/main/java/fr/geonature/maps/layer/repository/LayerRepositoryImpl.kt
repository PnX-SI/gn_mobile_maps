package fr.geonature.maps.layer.repository

import android.content.Context
import android.net.Uri
import fr.geonature.maps.layer.data.ILayerLocalDataSource
import fr.geonature.maps.layer.data.ISelectedLayersLocalDataSource
import fr.geonature.maps.layer.domain.LayerState
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.layer.tilesource.TileSourceFactory
import fr.geonature.maps.settings.LayerSettings
import org.tinylog.Logger

/**
 * Default implementation of [ILayerRepository].
 *
 * @author S. Grimault
 */
class LayerRepositoryImpl(
    private val context: Context,
    private val localLayerDataSource: ILayerLocalDataSource,
    private val selectedLayersLocalDataSource: ISelectedLayersLocalDataSource
) : ILayerRepository {

    private val layers = mutableSetOf<LayerState>()

    override suspend fun prepareLayers(
        layersSettings: List<LayerSettings>,
        basePath: String?
    ): List<LayerState> {
        return (layersSettings.filter { it.isOnline() }
            .map {
                LayerState.Layer(
                    settings = it,
                    source = it.getSourcesAsUri(),
                )
            } + layersSettings.filterNot { it.isOnline() }
            .map { layerSettings ->
                val result = runCatching {
                    localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                        layerSettings,
                        basePath
                    )
                }

                result.getOrNull()
                    ?.let {
                        LayerState.Layer(
                            layerSettings,
                            it
                        )
                    } ?: LayerState.Error(
                    result.exceptionOrNull()
                    ?.takeIf { it is LayerException }
                    ?.let { it as LayerException }
                    ?: LayerException.NotSupportedException(layerSettings))
            }).also { results ->
            with(layers) {
                clear()
                addAll(results)
            }
        }
    }

    override suspend fun prepareLayerFromSettings(
        layerSettings: LayerSettings,
        basePath: String?
    ): LayerState {
        val result = if (layerSettings.isOnline()) {
            // check if we have a valid URI...
            runCatching {
                TileSourceFactory.getOnlineTileSource(
                    context,
                    layerSettings
                )
            }.map {
                LayerState.Layer(
                    settings = layerSettings,
                    source = layerSettings.getSourcesAsUri(),
                )
            }
                .getOrElse {
                    LayerState.Error(
                        it as? LayerException ?: LayerException.InvalidOnlineLayerException(
                            layerSettings,
                            it
                        )
                    )
                }
        } else {
            val resolved = runCatching {
                localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                    layerSettings,
                    basePath
                )
            }
            resolved.getOrNull()
                ?.let {
                    LayerState.Layer(
                        layerSettings,
                        it
                    )
                } ?: LayerState.Error(
                resolved.exceptionOrNull()
                ?.takeIf { it is LayerException }
                ?.let { it as LayerException }
                ?: LayerException.NotSupportedException(layerSettings))
        }

        with(layers) {
            removeAll { it.isSame(result) }
            add(result)
        }

        return result
    }

    override suspend fun getAllLayers(): List<LayerState> {
        return layers.toList()
    }

    override suspend fun addLayerFromURI(uri: Uri): LayerState {
        Logger.info { "loading layer from URI '${uri}'..." }

        return localLayerDataSource.buildLocalLayerFromUri(uri)
            .also {
                when (it) {
                    is LayerState.Error -> Logger.error {
                        it.error.message ?: "failed to load local layer from URI '${uri}'"
                    }

                    else -> layers.add(it)
                }
            }
    }

    override suspend fun getSelectedLayers(): List<LayerState.SelectedLayer> {
        return selectedLayersLocalDataSource.getSelectedLayers()
            .mapNotNull { uri ->
                layers.filterIsInstance<LayerState.Layer>()
                    .firstOrNull { it.source.contains(uri) }
                    ?.select()
            }
    }

    override suspend fun setSelectedLayers(selectedLayers: List<LayerState.SelectedLayer>) {
        selectedLayersLocalDataSource.setSelectedLayers(
            selectedLayers.mapNotNull { layer ->
                layer.source.firstOrNull()
            }
                .toSet(),
        )
    }
}
