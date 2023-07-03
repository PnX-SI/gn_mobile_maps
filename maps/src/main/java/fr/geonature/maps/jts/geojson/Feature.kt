package fr.geonature.maps.jts.geojson

import android.os.Bundle
import android.os.Parcelable
import fr.geonature.maps.jts.geojson.filter.IFeatureFilterVisitor
import kotlinx.parcelize.Parcelize
import org.locationtech.jts.geom.Geometry

/**
 * `GeoJSON` [Feature] object.
 *
 * @author S. Grimault
 */
@Parcelize
data class Feature(
    val id: String?,
    val geometry: Geometry,
    val properties: Bundle = Bundle()
) : AbstractGeoJson(), Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Feature

        if (id != other.id) return false
        if (geometry != other.geometry) return false
        if (properties.size() != other.properties.size()) return false

        for (key in properties.keySet()) {
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
}
