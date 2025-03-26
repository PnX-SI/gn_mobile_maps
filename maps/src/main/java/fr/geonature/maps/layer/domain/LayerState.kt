package fr.geonature.maps.layer.domain

import android.net.Uri
import android.os.Parcelable
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.settings.LayerSettings
import kotlinx.parcelize.Parcelize

/**
 * Describes the different statuses of a geographical layer source.
 * @see LayerSettings
 * @author S. Grimault
 */
sealed class LayerState : Parcelable, Comparable<LayerState> {

    override fun compareTo(other: LayerState): Int {
        return getLayerSettings().compareTo(other.getLayerSettings())
    }

    /**
     * Gets the underlying [LayerSettings].
     */
    fun getLayerSettings(): LayerSettings {
        return when (this) {
            is Layer -> settings
            is SelectedLayer -> settings
            is Error -> error.layerSettings
        }
    }

    /**
     * Whether this given [LayerState] should be considered as the same as this one by comparing
     * declared URIs.
     */
    fun isSame(layerState: LayerState): Boolean {
        return getLayerSettings().source.any { layerState.getLayerSettings().source.contains(it) }
    }

    /**
     * Describes a valid layer with its properties and layer sources as valid URIs.
     * This layer is ready to be shown to the map.
     */
    @Parcelize
    data class Layer(
        val settings: LayerSettings,
        val source: List<Uri>,

        /**
         * Whether this layer is active or not (default: `true`).
         */
        val active: Boolean = true
    ) : LayerState() {

        /**
         * Marks this [Layer] as [SelectedLayer].
         */
        fun select(): SelectedLayer {
            return SelectedLayer(
                settings = settings,
                source = source
            )
        }
    }

    /**
     * Describes a selected layer with its properties and layer sources as valid URIs to show on the
     * map.
     */
    @Parcelize
    data class SelectedLayer(
        val settings: LayerSettings,
        val source: List<Uri>,

        /**
         * Whether this layer is active or not (default: `true`).
         */
        val active: Boolean = true
    ) : LayerState() {

        /**
         * Converts this [SelectedLayer] to [Layer].
         */
        fun toLayer(): Layer {
            return Layer(
                settings = settings,
                source = source
            )
        }
    }

    /**
     * In case of errors, embeds original [LayerException] with its URIs.
     */
    @Parcelize
    data class Error(val error: LayerException) : LayerState()
}