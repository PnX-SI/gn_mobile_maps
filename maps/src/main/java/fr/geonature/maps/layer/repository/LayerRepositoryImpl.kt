package fr.geonature.maps.layer.repository

import android.net.Uri
import androidx.core.net.toFile
import fr.geonature.maps.layer.data.ILayerLocalDataSource
import fr.geonature.maps.layer.data.ISelectedLayersLocalDataSource
import fr.geonature.maps.settings.LayerPropertiesSettings
import fr.geonature.maps.settings.LayerSettings

/**
 * Default implementation of [ILayerRepository].
 *
 * @author S. Grimault
 */
class LayerRepositoryImpl(
    private val localLayerDataSource: ILayerLocalDataSource,
    private val selectedLayersLocalDataSource: ISelectedLayersLocalDataSource
) : ILayerRepository {

    private val layers = mutableSetOf<LayerSettings>()

    override suspend fun prepareLayers(
        layersSettings: List<LayerSettings>,
        basePath: String?
    ): Result<List<LayerSettings>> {
        return runCatching {
            layersSettings.filter { it.isOnline() } + layersSettings.filterNot { it.isOnline() }
                .map { layerSettings ->
                    // if local sources are already resolved, returns the current layer settings
                    if (layerSettings.getSourcesAsUri()
                            .mapNotNull { runCatching { it.toFile() }.getOrNull() }
                            .isNotEmpty()
                    ) return@map LayerSettings.Builder()
                        .from(layerSettings)
                        .properties(
                            LayerPropertiesSettings.Builder.newInstance()
                                .from(layerSettings.properties)
                                .active(true)
                                .build()
                        )
                        .build()

                    runCatching {
                        localLayerDataSource.resolvesLocalLayerFromLayerSettings(
                            layerSettings,
                            basePath
                        )
                    }.getOrNull() ?: LayerSettings.Builder()
                        .from(layerSettings)
                        .properties(
                            LayerPropertiesSettings.Builder.newInstance()
                                .from(layerSettings.properties)
                                // something goes wrong: deactivate this layer settings
                                .active(false)
                                .build()
                        )
                        .build()
                }
        }.onSuccess {
            with(layers) {
                clear()
                addAll(it)
            }
        }
    }

    override suspend fun getAllLayers(): Result<List<LayerSettings>> {
        return runCatching { layers.toList() }
    }

    override suspend fun addLayerFromURI(uri: Uri): Result<LayerSettings> {
        return runCatching { localLayerDataSource.buildLocalLayerFromUri(uri) }.onSuccess {
            layers.add(it)
        }
    }

    override suspend fun getSelectedLayers(): Result<List<LayerSettings>> {
        return runCatching {
            selectedLayersLocalDataSource.getSelectedLayers()
                .mapNotNull { uri ->
                    layers.firstOrNull { it.getPrimarySource() == uri.toString() }
                }
        }
    }

    override suspend fun setSelectedLayers(selectedLayers: List<LayerSettings>) {
        selectedLayersLocalDataSource.setSelectedLayers(
            selectedLayers.mapNotNull { layer ->
                layer.getSourcesAsUri()
                    .firstOrNull { it.toString() == layer.getPrimarySource() }
            }
                .toSet(),
        )
    }
}
