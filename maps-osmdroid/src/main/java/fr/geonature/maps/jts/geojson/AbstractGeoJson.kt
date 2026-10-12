package fr.geonature.maps.jts.geojson

/**
 * Base `GeoJSON` object.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
abstract class AbstractGeoJson {

    val type: String = javaClass.simpleName
}
