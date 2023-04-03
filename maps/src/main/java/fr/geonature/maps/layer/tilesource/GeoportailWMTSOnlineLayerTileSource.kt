package fr.geonature.maps.layer.tilesource

import android.content.Context
import android.net.Uri
import fr.geonature.maps.R
import fr.geonature.maps.layer.LayerException
import fr.geonature.maps.settings.LayerSettings
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.util.MapTileIndex

/**
 * Describes online tile source for [Geoportail](https://www.geoportail.gouv.fr) services. See:
 * [Tiled Images - WMTS (OGC)](https://geoservices.ign.fr/documentation/services/api-et-services-ogc/images-tuilees-wmts-ogc).
 *
 * @author S. Grimault
 */
class GeoportailWMTSOnlineLayerTileSource(
    context: Context,
    layerSettings: LayerSettings
) : AbstractOnlineLayerTileSource(
    layerSettings.let {
        if (BASE_URLS.none { url -> layerSettings.source.any { source -> source.startsWith(url) } }) {
            throw LayerException.InvalidLayerException(
                layerSettings.source,
                GeoportailWMTSOnlineLayerTileSource::class.java.simpleName
            )
        }

        it.copy(
            properties = it.properties.copy(
                minZoomLevel = it.properties.minZoomLevel.coerceIn(
                    IntRange(
                        0,
                        21
                    )
                ),
                maxZoomLevel = it.properties.maxZoomLevel.takeIf { zoomLevel -> zoomLevel >= 0 }
                    ?.coerceIn(
                        IntRange(
                            0,
                            21
                        )
                    ) ?: 21,
                tileSizePixels = 256,
                tileMimeType = layerSettings.properties.tileMimeType ?: runCatching {
                    Uri.parse(layerSettings.source.firstOrNull())
                        .getQueryParameter("FORMAT")
                }.getOrNull(),
                attribution = it.properties.attribution?.takeIf { attribution ->
                    attribution.isNotBlank()
                } ?: context.getString(R.string.layer_geoportail_attribution),
            ),
        )
    },
    TileSourcePolicy(
        2,
        TileSourcePolicy.FLAG_USER_AGENT_NORMALIZED or TileSourcePolicy.FLAG_USER_AGENT_MEANINGFUL or TileSourcePolicy.FLAG_NO_PREVENTIVE or TileSourcePolicy.FLAG_NO_BULK
    )
) {

    override fun getTileURLString(pMapTileIndex: Long): String {
        return Uri.parse(baseUrl)
            .let {
                val queryParameters = (it.queryParameterNames.mapNotNull { parameterName ->
                    it.getQueryParameter(parameterName)
                        ?.let { parameterValue -> parameterName to parameterValue }
                } + listOf(
                    "TILEMATRIX" to MapTileIndex.getZoom(pMapTileIndex)
                        .toString(),
                    "TILECOL" to MapTileIndex.getX(pMapTileIndex)
                        .toString(),
                    "TILEROW" to MapTileIndex.getY(pMapTileIndex)
                        .toString()
                )).toMap()

                it.buildUpon()
                    .clearQuery()
                    .apply {
                        queryParameters.forEach { queryParameter ->
                            appendQueryParameter(
                                queryParameter.key,
                                queryParameter.value
                            )
                        }
                    }
                    .build()
            }
            .toString()
    }

    companion object {
        private val BASE_URLS = arrayOf("https://wxs.ign.fr")
    }
}