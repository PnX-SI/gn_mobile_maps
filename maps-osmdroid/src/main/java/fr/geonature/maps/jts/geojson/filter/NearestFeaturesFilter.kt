package fr.geonature.maps.jts.geojson.filter

import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.FeatureCollection
import fr.geonature.maps.jts.geojson.GeometryUtils
import java.util.TreeMap
import org.locationtech.jts.geom.Point

/**
 * Gets an ordered `List` of nearest [Feature]s located at a given distance (in meters)
 * of a given [Point].
 *
 * @author S. Grimault
 */
class NearestFeaturesFilter(
    private val point: Point,
    private val maxDistance: Double
) : IFeatureFilterVisitor {

    private val features: MutableMap<Double, Feature> = TreeMap()

    val filteredFeatures: List<Feature>
        get() = this.features.values.toList()

    override fun filter(feature: Feature): Boolean {
        val distanceFromFeature = GeometryUtils.distanceTo(
            point,
            feature.geometry
        )

        val matches = this.maxDistance >= distanceFromFeature

        if (matches) {
            features[distanceFromFeature] = feature
        }

        return matches
    }

    companion object {

        /**
         * Gets an ordered `List` of nearest [Feature]s located at a given distance (in meters)
         *
         * @param point the current [Point] to use.
         * @param maxDistance the max distance in meters as filter
         * @param features a `List` of [Feature]s on which to apply the filter
         *
         * @return an ordered `List` of nearest filtered [Feature]s found
         */
        fun getFilteredFeatures(
            point: Point,
            maxDistance: Double,
            features: List<Feature>
        ): List<Feature> {
            val nearestFeaturesFilter = NearestFeaturesFilter(
                point,
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
         * @param point the current [Point] to use.
         * @param maxDistance the max distance in meters as filter
         * @param featureCollection the [FeatureCollection] on which to apply the filter
         *
         * @return an ordered `List` of nearest filtered [Feature]s found
         */
        fun getFilteredFeatures(
            point: Point,
            maxDistance: Double,
            featureCollection: FeatureCollection
        ): List<Feature> {
            val nearestFeaturesFilter = NearestFeaturesFilter(
                point,
                maxDistance
            )
            featureCollection.apply(nearestFeaturesFilter)

            return nearestFeaturesFilter.filteredFeatures
        }
    }
}
