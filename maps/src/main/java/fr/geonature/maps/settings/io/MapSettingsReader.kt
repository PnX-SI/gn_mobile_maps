package fr.geonature.maps.settings.io

import android.util.JsonReader
import android.util.JsonToken.BEGIN_OBJECT
import android.util.JsonToken.NULL
import android.util.Log
import fr.geonature.maps.settings.LayerPropertiesSettings
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.settings.MapSettings
import org.osmdroid.util.GeoPoint
import java.io.IOException
import java.io.Reader

/**
 * Default [JsonReader] about reading a `JSON` stream and build the corresponding [MapSettings] metadata.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class MapSettingsReader {

    /**
     * parse a `JSON` string to convert as [MapSettings].
     *
     * @param json the `JSON` string to parse
     *
     * @return a [MapSettings] instance from the `JSON` string or `null` if something goes wrong
     */
    fun read(json: String?): MapSettings? {
        if (json.isNullOrBlank()) {
            return null
        }

        try {
            return read(json.reader())
        } catch (ioe: Exception) {
            Log.w(
                TAG,
                ioe
            )
        }

        return null
    }

    /**
     * parse a `JSON` reader to convert as [MapSettings].
     *
     * @param reader the [Reader] to parse
     *
     * @return a [MapSettings] instance from the `JSON` reader
     *
     * @throws IOException if something goes wrong
     */
    @Throws(Exception::class)
    fun read(reader: Reader): MapSettings {
        val jsonReader = JsonReader(reader)
        val pager = read(jsonReader)
        jsonReader.close()

        return pager
    }

    /**
     * Use a [JsonReader] instance to convert as [MapSettings].
     *
     * @param reader the [JsonReader] to use
     *
     * @return a [MapSettings] instance from [JsonReader]
     *
     * @throws Exception if something goes wrong
     */
    @Throws(Exception::class)
    fun read(reader: JsonReader): MapSettings {
        val builder = MapSettings.Builder.newInstance()

        reader.beginObject()

        while (reader.hasNext()) {

            when (reader.nextName()) {
                "base_path" -> builder.baseTilesPath(reader.nextString())
                "use_default_online_tile_source" -> builder.useDefaultOnlineTileSource(reader.nextBoolean())
                "show_attribution" -> builder.showAttribution(reader.nextBoolean())
                "show_compass" -> builder.showCompass(reader.nextBoolean())
                "show_scale" -> builder.showScale(reader.nextBoolean())
                "show_zoom" -> builder.showZoom(reader.nextBoolean())
                "max_bounds" -> {
                    val maxBounds = mutableListOf<GeoPoint>()

                    reader.beginArray()

                    while (reader.hasNext()) {
                        val tokens = mutableListOf<Double>()
                        reader.beginArray()

                        while (reader.hasNext()) {
                            tokens.add(reader.nextDouble())
                        }

                        reader.endArray()

                        if (tokens.size == 2) {
                            maxBounds.add(
                                GeoPoint(
                                    tokens[0],
                                    tokens[1]
                                )
                            )
                        }
                    }

                    reader.endArray()

                    builder.maxBounds(maxBounds)
                }
                "center" -> {
                    val tokens = mutableListOf<Double>()
                    reader.beginArray()

                    while (reader.hasNext()) {
                        tokens.add(reader.nextDouble())
                    }

                    reader.endArray()

                    if (tokens.size == 2) {
                        builder.center(
                            GeoPoint(
                                tokens[0],
                                tokens[1]
                            )
                        )
                    }
                }
                "start_zoom", "zoom" -> builder.zoom(reader.nextDouble())
                "min_zoom" -> builder.minZoomLevel(reader.nextDouble())
                "max_zoom" -> builder.maxZoomLevel(reader.nextDouble())
                "min_zoom_editing" -> builder.minZoomEditing(reader.nextDouble())
                "layers" -> readLayerSettingsAsList(
                    reader,
                    builder
                )
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return builder.build()
    }

    @Throws(IOException::class)
    private fun readLayerSettingsAsList(
        reader: JsonReader,
        builder: MapSettings.Builder
    ) {
        reader.beginArray()

        while (reader.hasNext()) {
            val layerSettings = readLayerSettings(reader)

            if (layerSettings != null) {
                builder.addLayer(layerSettings)
            }
        }

        reader.endArray()
    }

    @Throws(IOException::class)
    private fun readLayerSettings(reader: JsonReader): LayerSettings? {
        reader.beginObject()

        val builder = LayerSettings.Builder.newInstance()

        while (reader.hasNext()) {

            when (reader.nextName()) {
                "label" -> builder.label(reader.nextString())
                "source" -> builder.source(reader.nextString())
                "properties" -> builder.properties(readLayerPropertiesSettings(reader))
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return try {
            builder.build()
        } catch (iae: IllegalArgumentException) {
            Log.w(
                TAG,
                iae
            )

            null
        }
    }

    private fun readLayerPropertiesSettings(reader: JsonReader): LayerPropertiesSettings? {
        return when (val jsonToken = reader.peek()) {
            NULL -> {
                reader.nextNull()
                null
            }
            BEGIN_OBJECT -> {
                reader.beginObject()

                val builder = LayerPropertiesSettings.Builder.newInstance()

                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "min_zoom" -> builder.minZoomLevel(reader.nextInt())
                        "max_zoom" -> builder.maxZoomLevel(reader.nextInt())
                        "tile_size" -> builder.tileSizePixels(reader.nextInt())
                        "tile_mime_type" -> builder.tileMimeType(reader.nextString())
                        "attribution" -> builder.attribution(reader.nextString())
                        "style" -> builder.style(readLayerStyleSettings(reader))
                        else -> reader.skipValue()
                    }
                }

                reader.endObject()

                builder.build()
            }
            else -> throw IOException("Invalid object properties JSON token $jsonToken")
        }
    }

    private fun readLayerStyleSettings(reader: JsonReader): LayerStyleSettings? {
        return when (val jsonToken = reader.peek()) {
            NULL -> {
                reader.nextNull()
                null
            }
            BEGIN_OBJECT -> {
                reader.beginObject()

                val builder = LayerStyleSettings.Builder.newInstance()

                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "stroke" -> builder.stroke(reader.nextBoolean())
                        "color" -> builder.color(reader.nextString())
                        "weight" -> builder.weight(reader.nextInt())
                        "opacity" -> builder.opacity(
                            reader.nextDouble()
                                .toFloat()
                        )
                        "fill" -> builder.fill(reader.nextBoolean())
                        "fillColor" -> builder.fillColor(reader.nextString())
                        "fillOpacity" -> builder.fillOpacity(
                            reader.nextDouble()
                                .toFloat()
                        )
                        else -> reader.skipValue()
                    }
                }

                reader.endObject()

                builder.build()
            }
            else -> throw IOException("Invalid object properties JSON token $jsonToken")
        }
    }

    companion object {

        private val TAG = MapSettingsReader::class.java.name
    }
}
