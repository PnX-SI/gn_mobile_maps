package fr.geonature.maps.jts.geojson.repository

import fr.geonature.maps.jts.geojson.Feature
import java.io.File

/**
 * [Feature] repository.
 *
 * @author S. Grimault
 */
interface IFeatureRepository {

    /**
     * Loads all [Feature]s from given file. If several files are given , all features are combined
     * in a single list.
     *
     * Supported file format:
     * * `.geojson`
     * * `.json`
     * * `.wkt`
     *
     * @return a list of loaded [Feature]s
     */
    fun loadFeatures(vararg file: File): Result<List<Feature>>
}