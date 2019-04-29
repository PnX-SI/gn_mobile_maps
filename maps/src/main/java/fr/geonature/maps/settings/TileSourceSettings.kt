package fr.geonature.maps.settings

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils.isEmpty

/**
 * Default settings for a given tile source.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class TileSourceSettings(
    var name: String,
    var label: String,
    val minZoomLevel: Double = 0.0,
    val maxZoomLevel: Double = 0.0,
    val tileSizePixels: Int = DEFAULT_TILE_SIZE,
    val imageExtension: String? = DEFAULT_IMAGE_EXTENSION
) : Parcelable {

    private constructor(builder: Builder) : this(
        builder.name!!,
        builder.label!!,
        builder.minZoomLevel,
        builder.maxZoomLevel,
        builder.tileSizePixels,
        builder.imageExtension
    )

    private constructor(source: Parcel) : this(
        source.readString() ?: "",
        source.readString() ?: "",
        source.readDouble(),
        source.readDouble(),
        source.readInt().let { if (it == 0) DEFAULT_TILE_SIZE else it },
        source.readString() ?: DEFAULT_IMAGE_EXTENSION
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.writeString(name)
        dest?.writeString(label)
        dest?.writeDouble(minZoomLevel)
        dest?.writeDouble(maxZoomLevel)
        dest?.writeInt(tileSizePixels)
        dest?.writeString(imageExtension)
    }

    data class Builder(
        var name: String? = null,
        var label: String? = null,
        var minZoomLevel: Double = 0.0,
        var maxZoomLevel: Double = 0.0,
        var tileSizePixels: Int = DEFAULT_TILE_SIZE,
        var imageExtension: String? = DEFAULT_IMAGE_EXTENSION
    ) {

        fun from(tileSourceSettings: TileSourceSettings) = apply {
            name(tileSourceSettings.name)
            label(tileSourceSettings.label)
            minZoomLevel(tileSourceSettings.minZoomLevel)
            maxZoomLevel(tileSourceSettings.maxZoomLevel)
            tileSizePixels(tileSourceSettings.tileSizePixels)
            imageExtension(tileSourceSettings.imageExtension)
        }

        fun name(name: String) = apply { this.name = name }

        fun label(label: String) = apply { this.label = label }
        fun minZoomLevel(minZoomLevel: Double) = apply { this.minZoomLevel = minZoomLevel }
        fun maxZoomLevel(maxZoomLevel: Double) = apply { this.maxZoomLevel = maxZoomLevel }
        fun tileSizePixels(tileSizePixels: Int) = apply {
            this.tileSizePixels = if (tileSizePixels == 0) DEFAULT_TILE_SIZE else tileSizePixels
        }

        fun imageExtension(imageExtension: String?) = apply {
            this.imageExtension =
                if (isEmpty(imageExtension)) DEFAULT_IMAGE_EXTENSION else imageExtension
        }

        @Throws(java.lang.IllegalArgumentException::class)
        fun build(): TileSourceSettings {
            if (isEmpty(name)) throw IllegalArgumentException("tile source attribute name is required")
            // set default label from name if not defined
            if (isEmpty(label)) label = name

            return TileSourceSettings(this)
        }

        companion object {
            fun newInstance(): Builder = Builder()
        }
    }

    companion object {

        const val DEFAULT_TILE_SIZE = 256
        const val DEFAULT_IMAGE_EXTENSION = "png"

        @JvmField
        val CREATOR: Parcelable.Creator<TileSourceSettings> =
            object : Parcelable.Creator<TileSourceSettings> {
                override fun createFromParcel(source: Parcel): TileSourceSettings {
                    return TileSourceSettings(source)
                }

                override fun newArray(size: Int): Array<TileSourceSettings?> {
                    return arrayOfNulls(size)
                }
            }
    }
}