package fr.geonature.maps.layer

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
     * Thrown if the given layer URL was not eligible to use as online tile source.
     */
    data class InvalidOnlineLayerException(val baseUrls: List<String>) :
        LayerException("invalid layer from URLs '${baseUrls.joinToString(", ")}' to use as online source")

    /**
     * Thrown if no implementation was found from given tile source.
     */
    data class NotFoundException(val baseUrls: List<String>) :
        LayerException("no implementation found for layer with URLs '${baseUrls.joinToString(", ")}'")
}
