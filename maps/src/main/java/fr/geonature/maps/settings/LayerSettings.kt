package fr.geonature.maps.settings

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils

/**
 * Default settings for a given layer source.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class LayerSettings(
    var label: String,
    var source: String,
    var layerStyle: LayerStyleSettings = LayerStyleSettings()
) : Parcelable {

    private constructor(builder: Builder) : this(
        builder.label!!,
        builder.source!!,
        builder.layerStyle
    )

    private constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(LayerStyleSettings::class.java.classLoader) as LayerStyleSettings
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.writeString(label)
        dest?.writeString(source)
        dest?.writeParcelable(
            layerStyle,
            0
        )
    }

    class Builder {

        internal var label: String? = null
            private set

        internal var source: String? = null
            private set

        internal var layerStyle: LayerStyleSettings = LayerStyleSettings()
            private set

        fun label(label: String) =
            apply { this.label = label }

        fun source(source: String) =
            apply { this.source = source }

        fun style(layerStyle: LayerStyleSettings?) =
            apply { this.layerStyle = layerStyle ?: LayerStyleSettings() }

        @Throws(java.lang.IllegalArgumentException::class)
        fun build(): LayerSettings {
            if (TextUtils.isEmpty(label)) throw IllegalArgumentException("layer attribute label is required")
            if (TextUtils.isEmpty(source)) throw IllegalArgumentException("layer attribute source is required")

            return LayerSettings(this)
        }

        companion object {
            fun newInstance(): Builder =
                Builder()
        }
    }

    companion object CREATOR : Parcelable.Creator<LayerSettings> {
        override fun createFromParcel(parcel: Parcel): LayerSettings {
            return LayerSettings(parcel)
        }

        override fun newArray(size: Int): Array<LayerSettings?> {
            return arrayOfNulls(size)
        }
    }
}
