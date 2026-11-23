package fr.geonature.maps.maplibre.layer
import fr.geonature.maps.settings.LayerSettings
import java.io.File

/**
 * The base layer to show on the map.
 *
 * @author S. Grimault
 */
sealed class BaseLayer {
    abstract val settings: LayerSettings

    /**
     * An online raster tiles layer, described by one or more `{z}/{x}/{y}` tile URL templates.
     */
    data class Online(
        override val settings: LayerSettings,
        val tileUrls: List<String>
    ) : BaseLayer()

    /**
     * An embedded tiles layer (`mbtiles` or `pmtiles`) read from the local storage.
     */
    data class Local(
        override val settings: LayerSettings,
        val file: File
    ) : BaseLayer()
}
