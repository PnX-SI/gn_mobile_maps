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
data class MapSettings(
    val tileSourceSettings: List<TileSourceSettings>,
    val displayScale: Boolean = true,
    val zoom: Double = 0.0,
    val minimumZoomEditing: Double = 0.0,
    val maxBounds: BoundingBox?,
    val center: GeoPoint?
) : Parcelable {

    private constructor(builder: MapSettings.Builder) : this(
        builder.tileSourceSettings,
        builder.displayScale,
        builder.zoom,
        builder.minimumZoomEditing,
        builder.maxBounds,
        builder.center
    )

    private constructor(source: Parcel) : this(
        mutableListOf(),
        source.readByte() == Integer.valueOf(1).toByte(),
        source.readDouble(),
        source.readDouble(),
        source.readParcelable(BoundingBox::class.java.classLoader) as BoundingBox,
        source.readParcelable(GeoPoint::class.java.classLoader) as GeoPoint
    ) {
        source.readTypedList(
            tileSourceSettings,
            TileSourceSettings.CREATOR
        )
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeByte((if (displayScale) 1 else 0).toByte()) // as boolean value
        dest?.writeDouble(zoom)
        dest?.writeDouble(minimumZoomEditing)
        dest?.writeParcelable(
            maxBounds,
            0
        )
        dest?.writeParcelable(
            center,
            0
        )
        dest?.writeTypedList(tileSourceSettings)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MapSettings

        if (tileSourceSettings != other.tileSourceSettings) return false
        if (displayScale != other.displayScale) return false
        if (zoom != other.zoom) return false
        if (minimumZoomEditing != other.minimumZoomEditing) return false

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
        var result = tileSourceSettings.hashCode()
        result = 31 * result + displayScale.hashCode()
        result = 31 * result + zoom.hashCode()
        result = 31 * result + minimumZoomEditing.hashCode()
        result = 31 * result + (maxBounds?.hashCode() ?: 0)
        result = 31 * result + (center?.hashCode() ?: 0)

        return result
    }

    data class Builder(
        val tileSourceSettings: MutableList<TileSourceSettings> = mutableListOf(),
        var displayScale: Boolean = true,
        var zoom: Double = 0.0,
        var minimumZoomEditing: Double = 0.0,
        var maxBounds: BoundingBox? = null,
        var center: GeoPoint? = null
    ) {
        fun displayScale(displayScale: Boolean) = apply { this.displayScale = displayScale }
        fun zoom(zoom: Double) = apply { this.zoom = zoom }
        fun minimumZoomEditing(minimumZoomEditing: Double) =
            apply { this.minimumZoomEditing = minimumZoomEditing }

        fun maxBounds(geoPoints: List<GeoPoint>) =
            apply { this.maxBounds = BoundingBox.fromGeoPoints(geoPoints) }

        fun center(center: GeoPoint?) = apply { this.center = center }
        fun addTileSource(
            name: String, label: String
        ) = apply {
            this.tileSourceSettings.add(
                TileSourceSettings(
                    name,
                    label
                )
            )
        }

        fun build() = MapSettings(this)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<MapSettings> = object : Parcelable.Creator<MapSettings> {
            override fun createFromParcel(source: Parcel): MapSettings {
                return MapSettings(source)
            }

            override fun newArray(size: Int): Array<MapSettings?> {
                return arrayOfNulls(size)
            }
        }
    }
}