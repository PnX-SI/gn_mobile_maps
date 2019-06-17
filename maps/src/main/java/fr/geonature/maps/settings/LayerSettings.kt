package fr.geonature.maps.settings

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils

/**
 * Default settings for a given layer source.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class LayerSettings(
    var label: String,
    var source: String
) : Parcelable {

    private constructor(builder: Builder) : this(
        builder.label!!,
        builder.source!!
    )

    private constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
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
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LayerSettings

        if (label != other.label) return false
        if (source != other.source) return false

        return true
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + source.hashCode()

        return result
    }

    data class Builder(
        var label: String? = null,
        var source: String? = null
    ) {
        fun label(label: String) =
            apply { this.label = label }

        fun source(source: String) =
            apply { this.source = source }

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