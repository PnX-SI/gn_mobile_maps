package fr.geonature.maps.maplibre.layer
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.RasterLayer
import org.maplibre.android.style.sources.RasterSource
import org.maplibre.android.style.sources.TileSet

/**
 * Builds a MapLibre [Style.Builder] from a [BaseLayer].
 *
 * @author S. Grimault
 */
object BaseLayerStyleFactory {
    private const val SOURCE_ID = "base-layer-source"
    private const val LAYER_ID = "base-layer"
    private const val DEFAULT_TILE_SIZE = 256
    private const val DEFAULT_MAX_ZOOM = 22

    fun build(baseLayer: BaseLayer?): Style.Builder {
        val builder = Style.Builder()

        if (baseLayer == null) return builder.fromJson(EMPTY_STYLE)

        val tileSize = baseLayer.settings.properties.tileSizePixels.takeIf { it > 0 } ?: DEFAULT_TILE_SIZE
        val source = when (baseLayer) {
            is BaseLayer.Online -> RasterSource(
                SOURCE_ID,
                TileSet(
                    "2.1.0",
                    *baseLayer.tileUrls.toTypedArray()
                ).apply {
                    minZoom = baseLayer.settings.properties.minZoomLevel.coerceAtLeast(0)
                        .toFloat()
                    maxZoom = baseLayer.settings.properties.maxZoomLevel.takeIf { it >= 0 }
                        ?.toFloat() ?: DEFAULT_MAX_ZOOM.toFloat()
                    attribution = baseLayer.settings.properties.attribution?.takeIf { it.isNotBlank() }
                        ?: baseLayer.tileUrls.firstNotNullOfOrNull { TileUrlTemplates.defaultAttribution(it) }
                },
                tileSize
            )
            is BaseLayer.Local -> RasterSource(
                SOURCE_ID,
                localSourceUri(baseLayer),
                tileSize
            )
        }

        return builder.withSource(source)
            .withLayer(RasterLayer(LAYER_ID, SOURCE_ID))
    }

    internal fun localSourceUri(baseLayer: BaseLayer.Local): String {
        return when {
            baseLayer.file.name.endsWith(".pmtiles") -> "pmtiles://file://${baseLayer.file.absolutePath}"
            else -> "mbtiles://${baseLayer.file.absolutePath}"
        }
    }

    private const val EMPTY_STYLE = """{"version":8,"sources":{},"layers":[]}"""
}
