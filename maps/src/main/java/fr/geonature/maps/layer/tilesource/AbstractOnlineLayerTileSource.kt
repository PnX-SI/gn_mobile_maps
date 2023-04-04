package fr.geonature.maps.layer.tilesource

import fr.geonature.maps.settings.LayerSettings
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.tinylog.Logger

/**
 * Base online layer tile source.
 *
 * @author S. Grimault
 */
abstract class AbstractOnlineLayerTileSource(
    layerSettings: LayerSettings,
    tileSourcePolicy: TileSourcePolicy = TileSourcePolicy()
) : OnlineTileSourceBase(
    layerSettings.label,
    layerSettings.properties.minZoomLevel.takeIf { it >= 0 } ?: 0,
    layerSettings.properties.maxZoomLevel.takeIf { it >= 0 } ?: 20,
    layerSettings.properties.tileSizePixels.takeIf { it >= 0 } ?: 256,
    ".${
        layerSettings.properties.tileMimeType?.substringAfterLast(
            "image/",
            "png"
        ) ?: run {
            Logger.warn {
                "unable to determine tile source file extension from properties, use 'png' as default"
            }
            "png"
        }
    }",
    layerSettings.source.toTypedArray(),
    layerSettings.properties.attribution,
    tileSourcePolicy,
)