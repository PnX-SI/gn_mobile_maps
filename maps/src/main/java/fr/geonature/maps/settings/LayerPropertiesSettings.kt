package fr.geonature.maps.settings

import android.os.Parcel
import android.os.Parcelable
import androidx.core.os.ParcelCompat

/**
 * Layer additional properties.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class LayerPropertiesSettings(
    var active: Boolean = Builder.newInstance().active,
    val minZoomLevel: Int = Builder.newInstance().minZoomLevel,
    val maxZoomLevel: Int = Builder.newInstance().maxZoomLevel,
    val tileSizePixels: Int = Builder.newInstance().tileSizePixels,
    val tileMimeType: String? = Builder.newInstance().tileMimeType,
    val attribution: String? = Builder.newInstance().attribution,
    val style: LayerStyleSettings? = Builder.newInstance().style
) : Parcelable {

    private constructor(builder: Builder) : this(
        builder.active,
        builder.minZoomLevel,
        builder.maxZoomLevel,
        builder.tileSizePixels,
        builder.tileMimeType,
        builder.attribution,
        builder.style
    )

    private constructor(parcel: Parcel) : this(
        ParcelCompat.readBoolean(parcel),
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
            ParcelCompat.writeBoolean(dest, active)
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
        internal var active: Boolean = true
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

    companion object CREATOR : Parcelable.Creator<LayerPropertiesSettings> {
        override fun createFromParcel(parcel: Parcel): LayerPropertiesSettings {
            return LayerPropertiesSettings(parcel)
        }

        override fun newArray(size: Int): Array<LayerPropertiesSettings?> {
            return arrayOfNulls(size)
        }
    }
}
