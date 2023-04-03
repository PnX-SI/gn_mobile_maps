package fr.geonature.maps.layer.tilesource

import android.net.Uri
import fr.geonature.maps.settings.LayerSettings
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.util.MapTileIndex

/**
 * Base online layer `ZXY` tile source.
 *
 * The `ZXY` tile source is used for tile data that is accessed through URLs that include a zoom
 * level and tile grid x/y coordinates.
 *
 * @author S. Grimault
 */
open class OnlineLayerZXYTileSource(
    layerSettings: LayerSettings,
    tileSourcePolicy: TileSourcePolicy = TileSourcePolicy()
) : AbstractOnlineLayerTileSource(
    layerSettings,
    tileSourcePolicy
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        return Uri.parse(baseUrl)
            .buildUpon()
            .appendPath(
                MapTileIndex.getZoom(pMapTileIndex)
                    .toString()
            )
            .appendPath(
                MapTileIndex.getX(pMapTileIndex)
                    .toString()
            )
            .appendPath(
                "${MapTileIndex.getY(pMapTileIndex)}${imageFilenameEnding()}"
            )
            .build()
            .toString()
    }
}