package fr.geonature.maps.settings

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Layer additional properties.
 *
 * @author S. Grimault
 */
@Parcelize
data class LayerPropertiesSettings(

    /**
     * Whether this layer is ready to be displayed on the map (default: `true`).
     */
    val active: Boolean = Builder.newInstance().active,

    /**
     * Whether to show this layer by default (default: `true`, only applicable to vector layers).
     */
    val shownByDefault: Boolean = Builder.newInstance().shownByDefault,

    /**
     * The minimum zoom level where the layer is visible (default: -1, ie. no restriction).
     */
    val minZoomLevel: Int = Builder.newInstance().minZoomLevel,

    /**
     * The maximum zoom level where the layer is visible (default: -1, ie. no restriction).
     */
    val maxZoomLevel: Int = Builder.newInstance().maxZoomLevel,

    /**
     * The tile size in pixels (only applicable to tiles layers, default: 256).
     */
    val tileSizePixels: Int = Builder.newInstance().tileSizePixels,

    /**
     * The MIME type used for tiles (only applicable to tiles layers, default: `image/png`).
     */
    val tileMimeType: String? = Builder.newInstance().tileMimeType,

    /**
     * Describe the layer data and is often a legal obligation towards copyright holders and tile
     * providers (only applicable to tiles layers).
     */
    val attribution: String? = Builder.newInstance().attribution,

    /**
     * Define the layer style (only applicable to vector layers).
     */
    val style: LayerStyleSettings? = Builder.newInstance().style
) : Parcelable {

    private constructor(builder: Builder) : this(
        builder.active,
        builder.shownByDefault,
        builder.minZoomLevel,
        builder.maxZoomLevel,
        builder.tileSizePixels,
        builder.tileMimeType,
        builder.attribution,
        builder.style
    )

    class Builder {
        internal var active: Boolean = true
            private set

        internal var shownByDefault: Boolean = true
            private set

        internal var minZoomLevel: Int = -1
            private set

        internal var maxZoomLevel: Int = -1
            private set

        internal var tileSizePixels: Int = -1
            private set

        internal var tileMimeType: String? = null
            private set

        internal var attribution: String? = null
            private set

        internal var style: LayerStyleSettings? = null
            private set

        fun from(layerPropertiesSettings: LayerPropertiesSettings?) =
            apply {
                if (layerPropertiesSettings == null) return@apply

                active(layerPropertiesSettings.active)
                shownByDefault(layerPropertiesSettings.shownByDefault)
                if (layerPropertiesSettings.minZoomLevel >= 0) minZoomLevel(layerPropertiesSettings.minZoomLevel)
                if (layerPropertiesSettings.maxZoomLevel > 0) maxZoomLevel(layerPropertiesSettings.maxZoomLevel)
                if (layerPropertiesSettings.tileSizePixels > 0) tileSizePixels(layerPropertiesSettings.tileSizePixels)
                tileMimeType(layerPropertiesSettings.tileMimeType)
                attribution(layerPropertiesSettings.attribution)
                style(layerPropertiesSettings.style)
            }

        fun active(active: Boolean = true) = apply {
            this.active = active
        }

        fun shownByDefault(shownByDefault: Boolean = true) = apply {
            this.shownByDefault = shownByDefault
        }

        fun minZoomLevel(minZoomLevel: Int = 0) =
            apply {
                this.minZoomLevel = minZoomLevel.coerceIn(
                    0,
                    19
                )
                maxZoomLevel(if (maxZoomLevel < 0) 19 else maxZoomLevel)
            }

        fun maxZoomLevel(maxZoomLevel: Int = 19) = apply {
            this.maxZoomLevel =
                maxZoomLevel.coerceIn(
                    0,
                    19
                )
                    .takeIf { i -> i >= this.minZoomLevel }
                    ?: (this.minZoomLevel + 1).coerceAtMost(19)
        }

        fun tileSizePixels(tileSizePixels: Int = 256) =
            apply { this.tileSizePixels = tileSizePixels }

        fun tileMimeType(tileMimeType: String? = "image/png") =
            apply { this.tileMimeType = tileMimeType }

        fun attribution(attribution: String?) = apply { this.attribution = attribution }

        fun style(layerStyle: LayerStyleSettings?) =
            apply { this.style = layerStyle }

        fun build(): LayerPropertiesSettings {
            return LayerPropertiesSettings(this)
        }

        companion object {
            fun newInstance(): Builder = Builder()
        }
    }
}
