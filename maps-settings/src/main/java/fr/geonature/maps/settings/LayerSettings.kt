package fr.geonature.maps.settings

import android.os.Parcelable
import androidx.core.net.toUri
import kotlinx.parcelize.Parcelize
import java.io.Serializable

/**
 * Default settings for a given geographical layer source.
 *
 * @author S. Grimault
 */
@Parcelize
data class LayerSettings(
    /**
     * A human short description of this [LayerSettings]. Should be unique.
     */
    val label: String,
    val source: List<String>,
    val order: Int = 0,
    val properties: LayerPropertiesSettings = LayerPropertiesSettings()
) : Parcelable, Comparable<LayerSettings>, Serializable {

    private constructor(builder: Builder) : this(
        builder.label!!,
        builder.source,
        builder.order ?: 0,
        builder.properties!!
    )

    override fun compareTo(other: LayerSettings): Int {
        return when {
            this == other -> 0
            this.getType() != other.getType() -> this.getType().ordinal - other.getType().ordinal
            this.getType() == other.getType() && this.isOnline() != other.isOnline() -> if (this.isOnline()) -1 else 1
            else -> this.order - other.order
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
        path.toUri()
            .takeIf { !it.scheme.isNullOrBlank() && (if (isOnline()) Builder.isOnline(path) else true) && getType() == Builder.layerType(path) }
    }

    class Builder {

        internal var label: String? = null
            private set

        internal var source: List<String> = emptyList()
            private set

        internal var order: Int? = null
            private set

        internal var properties: LayerPropertiesSettings? = null
            private set

        fun from(layerSettings: LayerSettings?) = apply {
            if (layerSettings == null) return@apply

            label(layerSettings.label)
            sources(layerSettings.source)
            order(layerSettings.order)
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

        fun order(order: Int) = apply { this.order = order }

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
