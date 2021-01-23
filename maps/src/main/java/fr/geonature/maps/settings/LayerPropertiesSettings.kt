package fr.geonature.maps.settings

import android.os.Parcel
import android.os.Parcelable

/**
 * Layer additional properties.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class LayerPropertiesSettings(
    val minZoomLevel: Int = 0,
    val maxZoomLevel: Int = 0,
    val tileSizePixels: Int = 0,
    val tileMimeType: String? = null,
    val attribution: String? = null,
    val style: LayerStyleSettings? = null
) : Parcelable {

    private constructor(builder: Builder) : this(
        builder.minZoomLevel,
        builder.maxZoomLevel,
        builder.tileSizePixels,
        builder.tileMimeType,
        builder.attribution,
        builder.style
    )

    private constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(LayerStyleSettings::class.java.classLoader)
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.also {
            it.writeInt(minZoomLevel)
            it.writeInt(maxZoomLevel)
            it.writeInt(tileSizePixels)
            it.writeString(tileMimeType)
            it.writeString(attribution)
            it.writeParcelable(
                style,
                0
            )
        }
    }

    class Builder {
        internal var minZoomLevel: Int = 0
            private set

        internal var maxZoomLevel: Int = 0
            private set

        internal var tileSizePixels: Int = 0
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
                
                minZoomLevel(layerPropertiesSettings.minZoomLevel)
                maxZoomLevel(layerPropertiesSettings.maxZoomLevel)
                tileSizePixels(layerPropertiesSettings.tileSizePixels)
                tileMimeType(layerPropertiesSettings.tileMimeType)
                attribution(layerPropertiesSettings.attribution)
                style(layerPropertiesSettings.style)
            }

        fun minZoomLevel(minZoomLevel: Int = 0) =
            apply {
                this.minZoomLevel = minZoomLevel.coerceIn(
                    0,
                    19
                )
                maxZoomLevel(if (maxZoomLevel == 0) 19 else maxZoomLevel)
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

    companion object CREATOR : Parcelable.Creator<LayerPropertiesSettings> {
        override fun createFromParcel(parcel: Parcel): LayerPropertiesSettings {
            return LayerPropertiesSettings(parcel)
        }

        override fun newArray(size: Int): Array<LayerPropertiesSettings?> {
            return arrayOfNulls(size)
        }
    }
}
