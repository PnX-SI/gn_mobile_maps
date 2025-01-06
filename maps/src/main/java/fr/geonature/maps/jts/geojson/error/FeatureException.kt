package fr.geonature.maps.jts.geojson.error

import fr.geonature.maps.jts.geojson.Feature
import java.io.File

/**
 * Base exception about [Feature].
 *
 * @author S. Grimault
 */
sealed class FeatureException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
) {

    /**
     * Thrown if no layer file was found locally matching the given sources.
     */
    data class NotFoundException(val file: File) :
        FeatureException("no layer found from file '${file.absolutePath}'")

    /**
     * Thrown if no vector layer implementation was found from given file.
     */
    data class NotSupportedException(val file: File) :
        FeatureException("no implementation found for file '${file.absolutePath}' as vector layer")

    /**
     * Thrown if the given file cannot be parsed as vector layer.
     */
    data class ParseException(
        val file: File,
        override val cause: Throwable? = null
    ) : FeatureException(
        "invalid file '${file.absolutePath}' to use as local source${cause?.message?.let { ", cause: $it" } ?: ""}",
        cause,
    )

    /**
     * Thrown if no feature was loaded or found from given file.
     */
    data class NoFeatureFoundException(val file: File) :
        FeatureException("no feature found from file '${file.absolutePath}'")
}