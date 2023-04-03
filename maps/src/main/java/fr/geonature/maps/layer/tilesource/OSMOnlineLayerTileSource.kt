package fr.geonature.maps.layer.tilesource

import android.content.Context
import fr.geonature.maps.R
import fr.geonature.maps.layer.LayerException
import fr.geonature.maps.settings.LayerSettings
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy

/**
 * Describes online tile source for [OSM](https://www.openstreetmap.org) services.
 *
 * @author S. Grimault
 */
class OSMOnlineLayerTileSource(
    context: Context,
    layerSettings: LayerSettings
) : OnlineLayerZXYTileSource(
    layerSettings.let {
        if (BASE_URLS.none { url -> layerSettings.source.any { source -> source.startsWith(url) } }) {
            throw LayerException.InvalidLayerException(
                layerSettings.source,
                OSMOnlineLayerTileSource::class.java.simpleName
            )
        }

        it.copy(
            source = BASE_URLS.asList(),
            properties = it.properties.copy(
                minZoomLevel = it.properties.minZoomLevel.coerceIn(
                    IntRange(
                        0,
                        19
                    )
                ),
                maxZoomLevel = it.properties.maxZoomLevel.takeIf { zoomLevel -> zoomLevel >= 0 }
                    ?.coerceIn(
                        IntRange(
                            0,
                            19
                        )
                    ) ?: 19,
                tileSizePixels = 256,
                tileMimeType = "image/png",
                attribution = it.properties.attribution?.takeIf { attribution ->
                    attribution.isNotBlank()
                } ?: context.getString(R.string.layer_osm_attribution),
            ),
        )
    },
    TileSourcePolicy(
        2,
        TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL or TileSourcePolicy.FLAG_NO_PREVENTIVE or TileSourcePolicy.FLAG_NO_BULK
    )
) {

    companion object {
        private val BASE_URLS = arrayOf(
            "https://a.tile.openstreetmap.org",
            "https://b.tile.openstreetmap.org",
            "https://c.tile.openstreetmap.org"
        )
    }
}