package fr.geonature.maps.jts.geojson

import android.os.Parcel
import android.os.Parcelable
import fr.geonature.compat.os.readSerializableCompat
import fr.geonature.maps.jts.geojson.filter.IFeatureFilterVisitor
import org.locationtech.jts.geom.Geometry

/**
 * `GeoJSON` [Feature] object.
 *
 * @author S. Grimault
 */
data class Feature(
    val id: String?,
    val geometry: Geometry,
    val properties: HashMap<String, Any> = hashMapOf()
) : AbstractGeoJson(), Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readSerializableCompat<Geometry>()!!,
        parcel.readSerializableCompat<HashMap<String, Any>>()?: hashMapOf()
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.writeString(id)
        dest.writeSerializable(geometry)
        dest.writeSerializable(properties)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Feature

        if (id != other.id) return false
        if (geometry != other.geometry) return false
        if (properties.size != other.properties.size) return false

        for (key in properties.keys) {
            if (properties[key] != other.properties[key]) return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + geometry.hashCode()
        result = 31 * result + properties.hashCode()

        return result
    }

    /**
     * Performs an operation on a given [Feature].
     *
     * @param filter the filter to apply to this [Feature]
     */
    fun apply(filter: IFeatureFilterVisitor) {
        filter.filter(this)
    }

    companion object CREATOR : Parcelable.Creator<Feature> {
        override fun createFromParcel(parcel: Parcel): Feature {
            return Feature(parcel)
        }

        override fun newArray(size: Int): Array<Feature?> {
            return arrayOfNulls(size)
        }
    }
}
