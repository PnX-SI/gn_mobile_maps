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

    abstract val layerSettings: LayerSettings

    /**
     * Thrown if the given [LayerSettings] was not eligible to use with given tile source.
     */
    data class InvalidLayerException(
        override val layerSettings: LayerSettings,
        val tileSource: String
    ) : LayerException("invalid layer '${layerSettings.label}' from URLs '${layerSettings.source.joinToString(", ")}' to use as source '$tileSource'")

    /**
     * Thrown if the given [LayerSettings] was not eligible to use as local file layer.
     */
    data class InvalidFileLayerException(override val layerSettings: LayerSettings) :
        LayerException("invalid layer '${layerSettings.label}' from URIs '${layerSettings.source.joinToString(", ")}' to use as local source")

    /**
     * Thrown if the given [LayerSettings] was not eligible to use as online tile source.
     */
    data class InvalidOnlineLayerException(
        override val layerSettings: LayerSettings,
        override val cause: Throwable? = null
    ) : LayerException("invalid layer '${layerSettings.label}' from URLs '${layerSettings.source.joinToString(", ")}' to use as online source")

    /**
     * Thrown if the given [LayerSettings] cannot be loaded.
     */
    data class IOException(
        override val layerSettings: LayerSettings,
        override val cause: Throwable? = null
    ) : LayerException(
        "failed to load layer '${layerSettings.label}' from URIs '${layerSettings.source.joinToString(", ")}'${cause?.message?.let { ", cause: $it" } ?: ""}",
        cause,
    )

    /**
     * Thrown if no implementation was found from given sources of this [LayerSettings].
     */
    data class NotSupportedException(override val layerSettings: LayerSettings) :
        LayerException("no implementation found for layer '${layerSettings.label}' with URIs '${layerSettings.source.joinToString(", ")}'")

    /**
     * Thrown if no layer file was found locally matching the given sources. of this [LayerSettings]
     */
    data class NotFoundException(override val layerSettings: LayerSettings) :
        LayerException("no layer '${layerSettings.label}' found with URIs '${layerSettings.source.joinToString(", ")}'")
}
