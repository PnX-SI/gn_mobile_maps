package fr.geonature.maps.maplibre.layer

import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.LayerType
import fr.geonature.maps.settings.MapSettings
import org.tinylog.kotlin.Logger
import java.io.File

/**
 * Selects the base layer to show from [MapSettings].
 *
 * Rules:
 * 1. always use the first embedded tiles layer (`mbtiles` or `pmtiles`) which can be resolved from
 * the storage, preferring layers shown by default
 * 2. otherwise, use the first online layer (preferring layers shown by default) if online layers are
 * allowed
 *
 * @author S. Grimault
 */
object BaseLayerSelector {

    /**
     * @param mapSettings the map settings
     * @param resolveLocalFiles resolves the existing files of the given local layer
     *
     * @return the selected base layer or `null` if none can be shown
     */
    fun select(
        mapSettings: MapSettings,
        resolveLocalFiles: (LayerSettings) -> List<File>
    ): BaseLayer? {
        val tilesLayers = mapSettings.layersSettings.filter { it.getType() == LayerType.TILES }
        val localLayer = tilesLayers.filter { !it.isOnline() }
            .let { layers -> layers.filter { it.properties.shownByDefault } + layers.filterNot { it.properties.shownByDefault } }
            .firstNotNullOfOrNull { layerSettings ->
                resolveLocalFiles(layerSettings).firstOrNull { it.isFile && it.canRead() }
                    ?.let { BaseLayer.Local(layerSettings, it) }
            }

        if (localLayer != null) return localLayer

        if (!mapSettings.useOnlineLayers) {
            Logger.warn { "no local tiles layer found and online layers are disabled" }
            return null
        }

        val onlineLayers = tilesLayers.filter { it.isOnline() }

        return (onlineLayers.firstOrNull { it.properties.shownByDefault } ?: onlineLayers.firstOrNull())
            ?.let { layerSettings ->
                TileUrlTemplates.fromLayerSettings(layerSettings)
                    .takeIf { it.isNotEmpty() }
                    ?.let { BaseLayer.Online(layerSettings, it) }
            }
    }
}
