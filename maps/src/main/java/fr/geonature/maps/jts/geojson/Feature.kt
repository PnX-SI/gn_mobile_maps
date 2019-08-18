package fr.geonature.maps.jts.geojson

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import fr.geonature.maps.jts.geojson.filter.IFeatureFilterVisitor
import org.locationtech.jts.geom.Geometry

/**
 * `GeoJSON` [Feature] object.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class Feature : AbstractGeoJson, Parcelable {

    val id: String?
    val geometry: Geometry
    var properties: Bundle = Bundle()

    constructor(id: String,
                geometry: Geometry) {
        this.id = id
        this.geometry = geometry
    }

    private constructor(source: Parcel) {
        id = source.readString()
        geometry = source.readSerializable() as Geometry
        properties.putAll(source.readBundle(Bundle::class.java.classLoader))
    }

    /**
     * Performs an operation on a given [Feature].
     *
     * @param filter the filter to apply to this [Feature]
     */
    fun apply(filter: IFeatureFilterVisitor) {
        filter.filter(this)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel,
                               flags: Int) {
        dest.writeString(id)
        dest.writeSerializable(geometry)
        dest.writeBundle(properties)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Feature

        if (id != other.id) return false
        if (geometry != other.geometry) return false
        if (properties.keySet() != other.properties.keySet()) return false
        if (!properties.keySet().all { key -> properties[key] == other.properties[key] }) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + geometry.hashCode()
        result = 31 * result + properties.hashCode()

        return result
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
