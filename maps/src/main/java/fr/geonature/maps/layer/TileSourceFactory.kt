package fr.geonature.maps.layer

import android.content.Context
import fr.geonature.maps.layer.tilesource.AbstractOnlineLayerTileSource
import fr.geonature.maps.layer.tilesource.GeoportailWMTSOnlineLayerTileSource
import fr.geonature.maps.layer.tilesource.OSMOnlineLayerTileSource
import fr.geonature.maps.layer.tilesource.OpenTopoMapOnlineLayerTileSource
import fr.geonature.maps.layer.tilesource.WikimediaOnlineLayerTileSource
import fr.geonature.maps.settings.LayerSettings
import org.osmdroid.tileprovider.tilesource.ITileSource

/**
 * Tile source factory to get the corresponding [ITileSource] from given [LayerSettings].
 *
 * @author S. Grimault
 */
object TileSourceFactory {

    /**
     * Gets the corresponding online tile source from given [LayerSettings].
     *
     * @throws LayerException.InvalidOnlineLayerException if the given layer settings cannot be used as online tile source
     * @throws LayerException.NotFoundException if no implementation was found from given [LayerSettings]
     */
    fun getOnlineTileSource(
        context: Context,
        layerSettings: LayerSettings
    ): AbstractOnlineLayerTileSource {
        if (!layerSettings.isOnline()) throw LayerException.InvalidOnlineLayerException(layerSettings.source)

        return runCatching {
            GeoportailWMTSOnlineLayerTileSource(
                context,
                layerSettings
            )
        }.recoverCatching {
            OpenTopoMapOnlineLayerTileSource(
                context,
                layerSettings
            )
        }
            .recoverCatching {
                OSMOnlineLayerTileSource(
                    context,
                    layerSettings
                )
            }
            .recoverCatching {
                WikimediaOnlineLayerTileSource(
                    context,
                    layerSettings
                )
            }
            .onFailure { throw LayerException.NotFoundException(layerSettings.source) }
            .getOrThrow()
    }
}