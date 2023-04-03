package fr.geonature.maps.layer.tilesource

import android.content.Context
import fr.geonature.maps.R
import fr.geonature.maps.layer.LayerException
import fr.geonature.maps.settings.LayerSettings
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy

/**
 * Describes online tile source for [Wikimedia Maps](https://maps.wikimedia.org) services.
 *
 * @author S. Grimault
 */
class WikimediaOnlineLayerTileSource(
    context: Context,
    layerSettings: LayerSettings
) : OnlineLayerZXYTileSource(
    layerSettings.let {
        if (BASE_URLS.none { url -> layerSettings.source.any { source -> source.startsWith(url) } }) {
            throw LayerException.InvalidLayerException(
                layerSettings.source,
                WikimediaOnlineLayerTileSource::class.java.simpleName
            )
        }

        it.copy(
            source = BASE_URLS.asList(),
            properties = it.properties.copy(
                minZoomLevel = it.properties.minZoomLevel.coerceIn(
                    IntRange(
                        1,
                        19
                    )
                ),
                maxZoomLevel = it.properties.maxZoomLevel.takeIf { zoomLevel -> zoomLevel >= 0 }
                    ?.coerceIn(
                        IntRange(
                            1,
                            19
                        )
                    ) ?: 19,
                tileSizePixels = 256,
                tileMimeType = "image/png",
                attribution = it.properties.attribution?.takeIf { attribution ->
                    attribution.isNotBlank()
                } ?: context.getString(R.string.layer_wikimedia_attribution),
            ),
        )
    },
    TileSourcePolicy(
        1,
        TileSourcePolicy.FLAG_NO_BULK or TileSourcePolicy.FLAG_NO_PREVENTIVE or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL or TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED
    )
) {

    companion object {
        private val BASE_URLS = arrayOf(
            "https://maps.wikimedia.org/osm-intl"
        )
    }
}