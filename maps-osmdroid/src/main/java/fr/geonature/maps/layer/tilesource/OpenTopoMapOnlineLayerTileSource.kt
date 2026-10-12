package fr.geonature.maps.layer.tilesource

import android.content.Context
import fr.geonature.maps.R
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.settings.LayerSettings

/**
 * Describes online tile source for [OpenTopoMap](https://www.opentopomap.org) services.
 *
 * @author S. Grimault
 */
class OpenTopoMapOnlineLayerTileSource(
    context: Context,
    layerSettings: LayerSettings
) : OnlineLayerZXYTileSource(
    layerSettings.let {
        if (BASE_URLS.none { url -> layerSettings.source.any { source -> source.startsWith(url) } }) {
            throw LayerException.InvalidLayerException(
                layerSettings,
                OpenTopoMapOnlineLayerTileSource::class.java.simpleName
            )
        }

        it.copy(
            source = BASE_URLS.asList(),
            properties = it.properties.copy(
                minZoomLevel = it.properties.minZoomLevel.coerceIn(
                    IntRange(
                        0,
                        17
                    )
                ),
                maxZoomLevel = it.properties.maxZoomLevel.takeIf { zoomLevel -> zoomLevel >= 0 }
                    ?.coerceIn(
                        IntRange(
                            0,
                            17
                        )
                    ) ?: 17,
                tileSizePixels = 256,
                tileMimeType = "image/png",
                attribution = it.properties.attribution?.takeIf { attribution ->
                    attribution.isNotBlank()
                } ?: context.getString(R.string.layer_otm_attribution),
            ),
        )
    },
) {

    companion object {
        private val BASE_URLS = arrayOf(
            "https://a.tile.opentopomap.org",
            "https://b.tile.opentopomap.org",
            "https://c.tile.opentopomap.org"
        )
    }
}