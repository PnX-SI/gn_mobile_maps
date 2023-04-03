package fr.geonature.maps.settings

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

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
    val showAttribution: Boolean = Builder.newInstance().showAttribution,
    val showCompass: Boolean = Builder.newInstance().showCompass,
    val showScale: Boolean = Builder.newInstance().showScale,
    val showZoom: Boolean = Builder.newInstance().showZoom,
    val rotationGesture: Boolean = Builder.newInstance().rotationGesture,
    val zoom: Double = 0.0,
    val minZoomLevel: Double = 0.0,
    val maxZoomLevel: Double = 0.0,
    val minZoomEditing: Double = 0.0,
    val maxBounds: BoundingBox?,
    val center: GeoPoint?
) : Parcelable {

    private constructor(builder: Builder) : this(
        builder.layersSettings,
        builder.baseTilesPath,
        builder.useOnlineLayers,
        builder.showAttribution,
        builder.showCompass,
        builder.showScale,
        builder.showZoom,
        builder.rotationGesture,
        builder.zoom,
        builder.minZoomLevel,
        builder.maxZoomLevel,
        builder.minZoomEditing,
        builder.maxBounds,
        builder.center
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapSettings

        if (_layersSettings != other._layersSettings) return false
        if (baseTilesPath != other.baseTilesPath) return false
        if (useOnlineLayers != other.useOnlineLayers) return false
        if (showAttribution != other.showAttribution) return false
        if (showCompass != other.showCompass) return false
        if (showScale != other.showScale) return false
        if (showZoom != other.showZoom) return false
        if (rotationGesture != other.rotationGesture) return false
        if (zoom != other.zoom) return false
        if (minZoomLevel != other.minZoomLevel) return false
        if (maxZoomLevel != other.maxZoomLevel) return false
        if (minZoomEditing != other.minZoomEditing) return false

        if (maxBounds != null && other.maxBounds != null) {
            if (maxBounds.latNorth != other.maxBounds.latNorth) return false
            if (maxBounds.latSouth != other.maxBounds.latSouth) return false
            if (maxBounds.lonWest != other.maxBounds.lonWest) return false
            if (maxBounds.lonEast != other.maxBounds.lonEast) return false
        } else {
            if (maxBounds != other.maxBounds) return false
        }

        if (center != other.center) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _layersSettings.hashCode()
        result = 31 * result + baseTilesPath.hashCode()
        result = 31 * result + useOnlineLayers.hashCode()
        result = 31 * result + showAttribution.hashCode()
        result = 31 * result + showCompass.hashCode()
        result = 31 * result + showScale.hashCode()
        result = 31 * result + showZoom.hashCode()
        result = 31 * result + rotationGesture.hashCode()
        result = 31 * result + zoom.hashCode()
        result = 31 * result + minZoomLevel.hashCode()
        result = 31 * result + maxZoomLevel.hashCode()
        result = 31 * result + minZoomEditing.hashCode()
        result = 31 * result + (maxBounds?.hashCode() ?: 0)
        result = 31 * result + (center?.hashCode() ?: 0)

        return result
    }

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

        internal val layersSettings: MutableList<LayerSettings> = mutableListOf()

        internal var baseTilesPath: String? = null
            private set

        /**
         * Whether to use online layers (default: `true`).
         */
        var useOnlineLayers: Boolean = true
            private set

        /**
         * Whether to show the layer attribution control (default: `true`).
         */
        var showAttribution: Boolean = true
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

        internal var zoom: Double = 0.0
            private set

        internal var minZoomLevel: Double = 0.0
            private set

        internal var maxZoomLevel: Double = 0.0
            private set

        internal var minZoomEditing: Double = 0.0
            private set

        internal var maxBounds: BoundingBox? = null
            private set

        internal var center: GeoPoint? = null
            private set

        fun from(mapSettings: MapSettings?) =
            apply {
                if (mapSettings == null) return@apply

                this.layersSettings.addAll(mapSettings._layersSettings)
                this.baseTilesPath = mapSettings.baseTilesPath
                this.useOnlineLayers = mapSettings.useOnlineLayers
                this.showAttribution = mapSettings.showAttribution
                this.showCompass = mapSettings.showCompass
                this.showScale = mapSettings.showScale
                this.showZoom = mapSettings.showZoom
                this.rotationGesture = mapSettings.rotationGesture
                this.zoom = mapSettings.zoom
                this.minZoomLevel = mapSettings.minZoomLevel
                this.maxZoomLevel = mapSettings.maxZoomLevel
                this.minZoomEditing = mapSettings.minZoomEditing
                this.maxBounds = mapSettings.maxBounds
                this.center = mapSettings.center
            }

        fun baseTilesPath(baseTilesPath: String) =
            apply { this.baseTilesPath = baseTilesPath }

        fun useOnlineLayers(useOnlineLayers: Boolean) =
            apply { this.useOnlineLayers = useOnlineLayers }

        fun showAttribution(showAttribution: Boolean) =
            apply { this.showAttribution = showAttribution }

        fun showCompass(showCompass: Boolean) =
            apply { this.showCompass = showCompass }

        fun showScale(showScale: Boolean) =
            apply { this.showScale = showScale }

        fun showZoom(showZoom: Boolean) =
            apply { this.showZoom = showZoom }

        fun rotationGesture(rotateGesture: Boolean) =
            apply { this.rotationGesture = rotateGesture }

        fun zoom(zoom: Double) =
            apply { this.zoom = zoom }

        fun minZoomLevel(minZoomLevel: Double) =
            apply { this.minZoomLevel = minZoomLevel }

        fun maxZoomLevel(maxZoomLevel: Double) =
            apply { this.maxZoomLevel = maxZoomLevel }

        fun minZoomEditing(minZoomEditing: Double) =
            apply { this.minZoomEditing = minZoomEditing }

        fun maxBounds(geoPoints: List<GeoPoint>) =
            apply { this.maxBounds = BoundingBox.fromGeoPoints(geoPoints) }

        fun center(center: GeoPoint?) =
            apply { this.center = center }

        fun addLayer(
            label: String,
            vararg source: String
        ) =
            apply {
                addLayer(
                    LayerSettings.Builder.newInstance()
                        .label(label)
                        .sources(source.toList())
                        .build()
                )
            }

        fun addLayer(layerSettings: LayerSettings) =
            apply {
                if (!this.layersSettings.any { it.source == layerSettings.source }) this.layersSettings.add(
                    layerSettings
                )
            }

        fun build() =
            MapSettings(this)

        companion object {
            fun newInstance(): Builder = Builder()
        }
    }
}
