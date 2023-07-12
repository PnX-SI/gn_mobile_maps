package fr.geonature.maps.layer.error

import fr.geonature.maps.settings.LayerSettings

/**
 * Base exception about [LayerSettings].
 *
 * @author S. Grimault
 */
sealed class LayerException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
) {

    /**
     * Thrown if the given layer URL was not eligible to use with given tile source.
     */
    data class InvalidLayerException(
        val baseUrls: List<String>,
        val tileSource: String
    ) : LayerException("invalid layer from URLs '${baseUrls.joinToString(", ")}' to use as source '$tileSource'")

    /**
     * Thrown if the given layer URI was not eligible to use as local file layer.
     */
    data class InvalidFileLayerException(val baseURIs: List<String>) :
        LayerException("invalid layer from URIs '${baseURIs.joinToString(", ")}' to use as local source")

    /**
     * Thrown if the given layer URL was not eligible to use as online tile source.
     */
    data class InvalidOnlineLayerException(val baseUrls: List<String>) :
        LayerException("invalid layer from URLs '${baseUrls.joinToString(", ")}' to use as online source")

    /**
     * Thrown if no implementation was found from given sources.
     */
    data class NotSupportedException(val baseURIs: List<String>) :
        LayerException("no implementation found for layer with URIs '${baseURIs.joinToString(", ")}'")

    /**
     * Thrown if no layer file was found locally matching the given sources.
     */
    data class NotFoundException(val baseURIs: List<String>) :
        LayerException("no layer found with URIs '${baseURIs.joinToString(", ")}'")
}
