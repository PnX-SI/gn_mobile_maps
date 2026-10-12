package fr.geonature.maps.settings

import android.os.Parcelable
import fr.geonature.maps.jts.BoundingBox
import fr.geonature.maps.jts.LatLng
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

/**
 * Default settings for map configuration.
 *
 * @author S. Grimault
 */
@Parcelize
data class MapSettings(
    private val _layersSettings: List<LayerSettings>,
    val baseTilesPath: String?,
    val useOnlineLayers: Boolean = Builder.newInstance().useOnlineLayers,
    val showCompass: Boolean = Builder.newInstance().showCompass,
    val showScale: Boolean = Builder.newInstance().showScale,
    val showZoom: Boolean = Builder.newInstance().showZoom,
    val rotationGesture: Boolean = Builder.newInstance().rotationGesture,
    val zoom: Double = 0.0,
    val minZoomLevel: Double = 0.0,
    val maxZoomLevel: Double = 0.0,
    val maxBounds: BoundingBox?,
    val center: LatLng?,
    val edit: EditModeSettings? = null
) : Parcelable {

    private constructor(builder: Builder) : this(
        builder.layersSettings,
        builder.baseTilesPath,
        builder.useOnlineLayers,
        builder.showCompass,
        builder.showScale,
        builder.showZoom,
        builder.rotationGesture,
        builder.zoom,
        builder.minZoomLevel,
        builder.maxZoomLevel,
        builder.maxBounds,
        builder.center,
        builder.edit
    )

    fun builder(): Builder = Builder.newInstance().from(this)

    @IgnoredOnParcel
    val layersSettings: List<LayerSettings> = _layersSettings.sorted()

    fun getOnlineLayers(): List<LayerSettings> {
        return _layersSettings.filter { it.getType() == LayerType.TILES && it.isOnline() }
    }

    fun getTilesLayers(): List<LayerSettings> {
        return _layersSettings.filter { it.getType() == LayerType.TILES }
    }

    fun getVectorLayers(): List<LayerSettings> {
        return _layersSettings.filter { it.getType() == LayerType.VECTOR }
    }

    class Builder {

        val layersSettings: MutableList<LayerSettings> = mutableListOf()

        var baseTilesPath: String? = null
            private set

        /**
         * Whether to use online layers (default: `true`).
         */
        var useOnlineLayers: Boolean = true
            private set

        /**
         * Whether to show north compass during map rotation (default: `true`).
         */
        var showCompass: Boolean = true
            private set

        /**
         * Whether to show the map scale (default: `true`).
         */
        var showScale: Boolean = true
            private set

        /**
         * Whether to show the zoom control (default: `false`).
         */
        var showZoom: Boolean = false
            private set

        /**
         * Whether to activate rotation gesture (default: `false`).
         */
        var rotationGesture: Boolean = false
            private set

        var zoom: Double = 0.0
            private set

        var minZoomLevel: Double = 0.0
            private set

        var maxZoomLevel: Double = 0.0
            private set

        var maxBounds: BoundingBox? = null
            private set

        var center: LatLng? = null
            private set
        /**
         * The edit mode settings (default: `null`, meaning that the edit feature is disabled).
         */
        var edit: EditModeSettings? = null
            private set

        /**
         * Makes a copy of given [MapSettings].
         */
        fun from(mapSettings: MapSettings?) = apply {
            if (mapSettings == null) return@apply

            this.layersSettings.addAll(mapSettings._layersSettings)
            this.baseTilesPath = mapSettings.baseTilesPath
            this.useOnlineLayers = mapSettings.useOnlineLayers
            this.showCompass = mapSettings.showCompass
            this.showScale = mapSettings.showScale
            this.showZoom = mapSettings.showZoom
            this.rotationGesture = mapSettings.rotationGesture
            this.zoom = mapSettings.zoom
            this.minZoomLevel = mapSettings.minZoomLevel
            this.maxZoomLevel = mapSettings.maxZoomLevel
            this.maxBounds = mapSettings.maxBounds
            this.center = mapSettings.center
            this.edit = mapSettings.edit
        }

        fun baseTilesPath(baseTilesPath: String) = apply { this.baseTilesPath = baseTilesPath }

        fun useOnlineLayers(useOnlineLayers: Boolean) =
            apply { this.useOnlineLayers = useOnlineLayers }

        fun showCompass(showCompass: Boolean) = apply { this.showCompass = showCompass }

        fun showScale(showScale: Boolean) = apply { this.showScale = showScale }

        fun showZoom(showZoom: Boolean) = apply { this.showZoom = showZoom }

        fun rotationGesture(rotateGesture: Boolean) = apply { this.rotationGesture = rotateGesture }

        fun zoom(zoom: Double) = apply { this.zoom = zoom }

        fun minZoomLevel(minZoomLevel: Double) = apply { this.minZoomLevel = minZoomLevel }

        fun maxZoomLevel(maxZoomLevel: Double) = apply { this.maxZoomLevel = maxZoomLevel }

        fun maxBounds(latLngs: List<LatLng>) =
            apply { this.maxBounds = if (latLngs.isEmpty()) null else BoundingBox.fromLatLng(*latLngs.toTypedArray()) }

        fun maxBounds(maxBounds: BoundingBox?) = apply { this.maxBounds = maxBounds }

        fun center(center: LatLng?) = apply { this.center = center }
        fun edit(edit: EditModeSettings?) = apply { this.edit = edit }

        fun addLayer(
            label: String,
            vararg source: String
        ) = apply {
            addLayer(
                LayerSettings.Builder.newInstance()
                    .label(label)
                    .sources(source.toList())
                    .build()
            )
        }

        fun addLayer(layerSettings: LayerSettings) = apply {
            val layerIndex = this.layersSettings.maxOfOrNull { it.order } ?: -1
            if (!this.layersSettings.any { it.source == layerSettings.source }) this.layersSettings.add(
                layerSettings.copy(order = layerIndex + 1)
            )
        }

        fun build() = MapSettings(this)

        companion object {
            fun newInstance(): Builder = Builder()
        }
    }
}
