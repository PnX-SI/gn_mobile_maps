package fr.geonature.maps.settings

import android.os.Parcel
import android.os.Parcelable
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

/**
 * Default settings for map configuration.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class MapSettings(val layersSettings: List<LayerSettings>,
                       val baseTilesPath: String?,
                       val showScale: Boolean = true,
                       val showCompass: Boolean = true,
                       val zoom: Double = 0.0,
                       val minZoomLevel: Double = 0.0,
                       val maxZoomLevel: Double = 0.0,
                       val minZoomEditing: Double = 0.0,
                       val maxBounds: BoundingBox?,
                       val center: GeoPoint?) : Parcelable {

    private constructor(builder: Builder) : this(builder.layersSettings,
                                                 builder.baseTilesPath,
                                                 builder.showScale,
                                                 builder.showCompass,
                                                 builder.zoom,
                                                 builder.minZoomLevel,
                                                 builder.maxZoomLevel,
                                                 builder.minZoomEditing,
                                                 builder.maxBounds,
                                                 builder.center)

    private constructor(source: Parcel) : this(mutableListOf(),
                                               source.readString(),
                                               source.readByte() == Integer.valueOf(1).toByte(), // as boolean value
                                               source.readByte() == Integer.valueOf(1).toByte(), // as boolean value
                                               source.readDouble(),
                                               source.readDouble(),
                                               source.readDouble(),
                                               source.readDouble(),
                                               source.readParcelable(BoundingBox::class.java.classLoader) as BoundingBox,
                                               source.readParcelable(GeoPoint::class.java.classLoader) as GeoPoint) {
        source.readTypedList(layersSettings,
                             LayerSettings.CREATOR)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.writeString(baseTilesPath)
        dest?.writeByte((if (showScale) 1 else 0).toByte()) // as boolean value
        dest?.writeByte((if (showCompass) 1 else 0).toByte()) // as boolean value
        dest?.writeDouble(zoom)
        dest?.writeDouble(minZoomLevel)
        dest?.writeDouble(maxZoomLevel)
        dest?.writeDouble(minZoomEditing)
        dest?.writeParcelable(maxBounds,
                              0)
        dest?.writeParcelable(center,
                              0)
        dest?.writeTypedList(layersSettings)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapSettings

        if (layersSettings != other.layersSettings) return false
        if (baseTilesPath != other.baseTilesPath) return false
        if (showScale != other.showScale) return false
        if (zoom != other.zoom) return false
        if (minZoomLevel != other.minZoomLevel) return false
        if (maxZoomLevel != other.maxZoomLevel) return false
        if (minZoomEditing != other.minZoomEditing) return false

        if (maxBounds != null && other.maxBounds != null) {
            if (maxBounds.latNorth != other.maxBounds.latNorth) return false
            if (maxBounds.latSouth != other.maxBounds.latSouth) return false
            if (maxBounds.lonWest != other.maxBounds.lonWest) return false
            if (maxBounds.lonEast != other.maxBounds.lonEast) return false
        }
        else {
            if (maxBounds != other.maxBounds) return false
        }

        if (center != other.center) return false

        return true
    }

    override fun hashCode(): Int {
        var result = layersSettings.hashCode()
        result = 31 * result + baseTilesPath.hashCode()
        result = 31 * result + showScale.hashCode()
        result = 31 * result + zoom.hashCode()
        result = 31 * result + minZoomLevel.hashCode()
        result = 31 * result + maxZoomLevel.hashCode()
        result = 31 * result + minZoomEditing.hashCode()
        result = 31 * result + (maxBounds?.hashCode() ?: 0)
        result = 31 * result + (center?.hashCode() ?: 0)

        return result
    }

    fun getLayersAsTileSources(): List<String> {
        return layersSettings.filter { it.source.endsWith(".mbtiles") }
                .map { it.source }
    }

    fun getVectorLayers(): List<LayerSettings> {
        return layersSettings.filter { layerSettings ->
            arrayOf(".geojson",
                    ".json",
                    ".wkt").any { layerSettings.source.endsWith(it) }
        }
    }

    class Builder {

        internal val layersSettings: MutableList<LayerSettings> = mutableListOf()

        internal var baseTilesPath: String? = null
            private set

        internal var showScale: Boolean = true
            private set

        internal var showCompass: Boolean = true
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

        fun from(mapSettings: MapSettings) =
                apply {
                    this.layersSettings.addAll(mapSettings.layersSettings)
                    this.baseTilesPath = mapSettings.baseTilesPath
                    this.showScale = mapSettings.showScale
                    this.showCompass = mapSettings.showCompass
                    this.zoom = mapSettings.zoom
                    this.minZoomLevel = mapSettings.minZoomLevel
                    this.maxZoomLevel = mapSettings.maxZoomLevel
                    this.minZoomEditing = mapSettings.minZoomEditing
                    this.maxBounds = mapSettings.maxBounds
                    this.center = mapSettings.center
                }

        fun baseTilesPath(baseTilesPath: String) =
                apply { this.baseTilesPath = baseTilesPath }

        fun showScale(showScale: Boolean) =
                apply { this.showScale = showScale }

        fun showCompass(showCompass: Boolean) =
                apply { this.showCompass = showCompass }

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

        fun addLayer(label: String,
                     source: String) =
                apply {
                    addLayer(LayerSettings.Builder.newInstance().label(label).source(source).build())
                }

        fun addLayer(layerSettings: LayerSettings) =
                apply {
                    if (!this.layersSettings.any { it.source == layerSettings.source }) this.layersSettings.add(layerSettings)
                }

        fun build() =
                MapSettings(this)

        companion object {
            fun newInstance(): Builder =
                    Builder()
        }
    }

    companion object CREATOR : Parcelable.Creator<MapSettings> {
        override fun createFromParcel(parcel: Parcel): MapSettings {
            return MapSettings(parcel)
        }

        override fun newArray(size: Int): Array<MapSettings?> {
            return arrayOfNulls(size)
        }
    }
}