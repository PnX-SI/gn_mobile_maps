package fr.geonature.maps.layer.tilesource

import android.content.Context
import fr.geonature.maps.layer.error.LayerException
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
     * @throws LayerException.NotSupportedException if no implementation was found from given [LayerSettings]
     */
    fun getOnlineTileSource(
        context: Context,
        layerSettings: LayerSettings
    ): AbstractOnlineLayerTileSource {
        if (!layerSettings.isOnline()) throw LayerException.InvalidOnlineLayerException(layerSettings)

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
            .onFailure { throw LayerException.NotSupportedException(layerSettings) }
            .getOrThrow()
    }
}