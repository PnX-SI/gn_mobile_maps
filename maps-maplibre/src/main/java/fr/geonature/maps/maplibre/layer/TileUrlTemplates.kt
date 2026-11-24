package fr.geonature.maps.maplibre.layer

import fr.geonature.maps.settings.LayerSettings

/**
 * Builds MapLibre tile URL templates (with `{z}`, `{x}` and `{y}` placeholders) from online
 * [LayerSettings].
 *
 * Supported sources:
 * - OGC WMTS (KVP encoding) such as the IGN geoportail (`REQUEST=GetTile`)
 * - ZXY tile servers such as OpenStreetMap (`<base url>/{z}/{x}/{y}.png`)
 * - sources already defining the `{z}`, `{x}` and `{y}` placeholders
 *
 * @author S. Grimault
 */
object TileUrlTemplates {
    private val WMTS_PARAMETERS = listOf(
        "TILEMATRIX",
        "TILECOL",
        "TILEROW"
    )

    fun fromLayerSettings(layerSettings: LayerSettings): List<String> {
        return layerSettings.source.filter { it.startsWith("http") }
            .map { source ->
                when {
                    source.contains("{z}") -> source
                    isWmts(source) -> wmtsTemplate(source)
                    else -> zxyTemplate(
                        source,
                        layerSettings.properties.tileMimeType
                    )
                }
            }
            .distinct()
    }

    private fun isWmts(source: String): Boolean {
        return source.substringAfter(
            '?',
            ""
        )
            .split('&')
            .any {
                it.equals(
                    "REQUEST=GetTile",
                    ignoreCase = true
                )
            }
    }

    private fun wmtsTemplate(source: String): String {
        val parameters = source.substringAfter(
            '?',
            ""
        )
            .split('&')
            .filter {
                it.isNotBlank() && WMTS_PARAMETERS.none { name ->
                    it.startsWith(
                        "$name=",
                        ignoreCase = true
                    )
                }
            } + listOf(
            "TILEMATRIX={z}",
            "TILECOL={x}",
            "TILEROW={y}"
        )

        return "${source.substringBefore('?')}?${parameters.joinToString("&")}"
    }

    private fun zxyTemplate(
        source: String,
        tileMimeType: String?
    ): String {
        val extension = tileMimeType?.substringAfterLast(
            "image/",
            "png"
        )
            ?.takeIf { it.isNotBlank() } ?: "png"

        return "${source.removeSuffix("/")}/{z}/{x}/{y}.$extension"
    }

    /**
     * Gets a default attribution from the given tile URL if none was defined.
     */
    fun defaultAttribution(tileUrl: String): String? {
        return when {
            tileUrl.contains("openstreetmap.org") -> "\u00A9 OpenStreetMap contributors"
            tileUrl.contains("opentopomap.org") -> "\u00A9 OpenTopoMap (CC-BY-SA), \u00A9 OpenStreetMap contributors"
            tileUrl.contains("data.geopf.fr") -> "\u00A9 IGN"
            else -> null
        }
    }
}
