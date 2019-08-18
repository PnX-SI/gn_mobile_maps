package fr.geonature.maps.jts.geojson.filter

import fr.geonature.maps.jts.geojson.Feature

/**
 * Visitor pattern to apply a concrete filter to a given [Feature].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
interface IFeatureFilterVisitor {

    /**
     * Performs an operation on a given [Feature].
     *
     * @param feature a [Feature] instance to which the filter is applied
     *
     * @return `true` if the given [Feature] matches the filter
     */
    fun filter(feature: Feature): Boolean
}
