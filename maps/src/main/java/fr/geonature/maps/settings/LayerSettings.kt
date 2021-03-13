package fr.geonature.maps.settings

import android.os.Parcel
import android.os.Parcelable

/**
 * Default settings for a given layer source.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class LayerSettings(
    val label: String,
    val source: String, // TODO: manage source as list
    val properties: LayerPropertiesSettings = LayerPropertiesSettings()
) : Parcelable, Comparable<LayerSettings> {

    private constructor(builder: Builder) : this(
        builder.label!!,
        builder.source!!,
        builder.properties!!
    )

    private constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(LayerPropertiesSettings::class.java.classLoader)!!
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel?,
        flags: Int
    ) {
        dest?.also {
            it.writeString(label)
            it.writeString(source)
            it.writeParcelable(
                properties,
                0
            )
        }
    }

    override fun compareTo(other: LayerSettings): Int {
        return when {
            this == other -> 0
            this.getType() != other.getType() -> this.getType().ordinal - other.getType().ordinal
            this.getType() == other.getType() && this.isOnline() != other.isOnline() -> if (this.isOnline()) -1 else 1
            this.getType() == other.getType() && this.source != other.source -> this.source.compareTo(other.source)
            this.getType() == other.getType() && this.source == other.source && this.label != other.label -> this.label.compareTo(other.label)
            this.getType() == other.getType() && this.source == other.source && this.label == other.label -> this.properties.active.compareTo(other.properties.active)
            else -> -1
        }
    }

    fun getType(): LayerType {
        return Builder.layerType(source)
    }

    fun isOnline(): Boolean {
        return getType() == LayerType.TILES && Builder.isOnline(source)
    }

    class Builder {

        internal var label: String? = null
            private set

        internal var source: String? = null
            private set

        internal var properties: LayerPropertiesSettings? = null
            private set

        fun label(label: String) =
            apply { this.label = label }

        fun source(source: String) =
            apply {
                this.source = if (isOnline(source)) source.removeSuffix("/") else source
                properties(this.properties)
            }

        fun properties(properties: LayerPropertiesSettings? = null) =
            apply {
                // set default properties
                this.properties = LayerPropertiesSettings.Builder.newInstance()
                    .from(properties)
                    .build()

                // set default properties for online source if none was given
                if (isOnline(source) &&
                    this.properties?.let {
                        it.minZoomLevel < 0 ||
                            it.maxZoomLevel < 0 ||
                            it.tileSizePixels < 0 ||
                            it.tileMimeType.isNullOrBlank()
                    } != false
                ) {
                    this.properties = LayerPropertiesSettings.Builder.newInstance()
                        .from(this.properties)
                        .minZoomLevel()
                        .maxZoomLevel()
                        .tileSizePixels()
                        .tileMimeType()
                        .build()

                    return@apply
                }

                // set default style for vector source if none was given
                if (layerType(source) == LayerType.VECTOR && this.properties?.style == null) {
                    this.properties = LayerPropertiesSettings.Builder.newInstance()
                        .from(this.properties)
                        .style(
                            LayerStyleSettings.Builder.newInstance()
                                .build()
                        )
                        .build()

                    return@apply
                }
            }

        @Throws(java.lang.IllegalArgumentException::class)
        fun build(): LayerSettings {
            if (label.isNullOrBlank()) throw IllegalArgumentException("layer attribute label is required")
            if (source.isNullOrBlank()) throw IllegalArgumentException("layer attribute source is required")

            return LayerSettings(this)
        }

        companion object {
            fun newInstance(): Builder = Builder()

            internal val layerType: (source: String?) -> LayerType = { source ->
                when {
                    isOnline(source) || source?.endsWith("mbtiles") == true -> LayerType.TILES
                    arrayOf(
                        ".geojson",
                        ".json",
                        ".wkt"
                    ).any { source?.endsWith(it) == true } -> LayerType.VECTOR
                    else -> LayerType.NOT_IMPLEMENTED
                }
            }

            internal val isOnline: (source: String?) -> Boolean =
                { it?.startsWith("http") == true }
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
