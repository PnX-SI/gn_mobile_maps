package fr.geonature.maps.settings

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Default settings for a given geographical layer source.
 *
 * @author S. Grimault
 */
@Parcelize
data class LayerSettings(
    /**
     * An human short description of this [LayerSettings]. Should be unique.
     */
    val label: String,
    val source: List<String>,
    val properties: LayerPropertiesSettings = LayerPropertiesSettings()
) : Parcelable, Comparable<LayerSettings> {

    private constructor(builder: Builder) : this(
        builder.label!!,
        builder.source,
        builder.properties!!
    )

    override fun compareTo(other: LayerSettings): Int {
        return when {
            this == other -> 0
            this.getType() != other.getType() -> this.getType().ordinal - other.getType().ordinal
            this.getType() == other.getType() && this.isOnline() != other.isOnline() -> if (this.isOnline()) -1 else 1
            this.getType() == other.getType() && this.source != other.source -> this.getPrimarySource()
                .compareTo(other.getPrimarySource())

            this.getType() == other.getType() && this.source == other.source && this.label != other.label -> this.label.compareTo(other.label)
            else -> -1
        }
    }

    fun getType(): LayerType {
        return Builder.layerType(source.firstOrNull())
    }

    fun getPrimarySource(): String {
        return source.first()
    }

    fun isOnline(): Boolean {
        return Builder.isOnline(source.firstOrNull())
    }

    /**
     * Gets all sources defined as valid URIs. May returns an empty list if none is eligible as
     * valid URI.
     */
    fun getSourcesAsUri() = source.mapNotNull { path ->
        Uri.parse(path)
            ?.takeIf { !it.scheme.isNullOrBlank() && (if (isOnline()) Builder.isOnline(path) else true) && getType() == Builder.layerType(path) }
    }

    class Builder {

        internal var label: String? = null
            private set

        internal var source: List<String> = emptyList()
            private set

        internal var properties: LayerPropertiesSettings? = null
            private set

        fun from(layerSettings: LayerSettings?) = apply {
            if (layerSettings == null) return@apply

            label(layerSettings.label)
            sources(layerSettings.source)
            properties(layerSettings.properties)
        }

        fun label(label: String) = apply { this.label = label }

        fun addSource(source: String) = apply {
            if (this.source.contains(source)) return@apply
            sources(this.source + listOf(source))
        }

        fun sources(source: List<String>) = apply {
            this.source = source.map { if (isOnline(it)) it.removeSuffix("/") else it }
                .filter { layerType(it) == layerType(source.firstOrNull()) }
                .filter { it.isNotBlank() }
                .distinct()
            properties(this.properties)
        }

        fun properties(properties: LayerPropertiesSettings? = null) = apply {
            // set default properties
            this.properties = LayerPropertiesSettings.Builder.newInstance()
                .from(properties)
                .build()

            // set default properties for online source if none was given
            if (isOnline(source.firstOrNull()) && this.properties?.let {
                    it.minZoomLevel < 0 || it.maxZoomLevel < 0 || it.tileSizePixels < 0 || it.tileMimeType.isNullOrBlank()
                } != false) {
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
            if (layerType(source.firstOrNull()) == LayerType.VECTOR && this.properties?.style == null) {
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
            if (source.isEmpty()) throw IllegalArgumentException("layer attribute source is required")

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

            internal val isOnline: (source: String?) -> Boolean = { it?.startsWith("http") == true }
        }
    }
}
