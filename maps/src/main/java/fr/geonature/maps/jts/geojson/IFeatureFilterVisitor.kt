package fr.geonature.maps.jts.geojson

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
     */
    fun filter(feature: Feature)
}
