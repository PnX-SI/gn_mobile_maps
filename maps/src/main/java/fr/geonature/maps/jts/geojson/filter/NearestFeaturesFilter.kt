package fr.geonature.maps.jts.geojson.filter

import org.osmdroid.util.GeoPoint

import java.util.ArrayList
import java.util.TreeMap
import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.FeatureCollection
import fr.geonature.maps.jts.geojson.GeometryUtils
import fr.geonature.maps.jts.geojson.IFeatureFilterVisitor

/**
 * Gets an ordered `List` of nearest [Feature]s located at a given distance (in meters)
 * of a given [GeoPoint].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class NearestFeaturesFilter(
    private val geoPoint: GeoPoint,
    private val maxDistance: Double
) : IFeatureFilterVisitor {

    private val features: MutableMap<Double, Feature> = TreeMap()

    val filteredFeatures: List<Feature>
        get() = ArrayList(this.features.values)

    override fun filter(feature: Feature) {
        val distanceFromFeature = GeometryUtils.distanceTo(
            GeometryUtils.toPoint(geoPoint),
            feature.geometry
        )

        if (this.maxDistance >= distanceFromFeature) {
            features[distanceFromFeature] = feature
        }
    }

    companion object {

        /**
         * Gets an ordered `List` of nearest [Feature]s located at a given distance (in meters)
         *
         * @param geoPoint    the current [GeoPoint] to use.
         * @param maxDistance the max distance in meters as filter
         * @param features    a `List` of [Feature]s on which to apply the filter
         *
         * @return an ordered `List` of nearest filtered [Feature]s found
         */
        fun getFilteredFeatures(
            geoPoint: GeoPoint,
            maxDistance: Double,
            features: List<Feature>
        ): List<Feature> {
            val nearestFeaturesFilter = NearestFeaturesFilter(
                geoPoint,
                maxDistance
            )

            for (feature in features) {
                feature.apply(nearestFeaturesFilter)
            }

            return nearestFeaturesFilter.filteredFeatures
        }

        /**
         * Gets an ordered `List` of nearest [Feature]s located at a given distance (in meters)
         *
         * @param geoPoint          the current [GeoPoint] to use.
         * @param maxDistance       the max distance in meters as filter
         * @param featureCollection the [FeatureCollection] on which to apply the filter
         *
         * @return an ordered `List` of nearest filtered [Feature]s found
         */
        fun getFilteredFeatures(
            geoPoint: GeoPoint,
            maxDistance: Double,
            featureCollection: FeatureCollection
        ): List<Feature> {
            val nearestFeaturesFilter = NearestFeaturesFilter(
                geoPoint,
                maxDistance
            )
            featureCollection.apply(nearestFeaturesFilter)

            return nearestFeaturesFilter.filteredFeatures
        }
    }
}
