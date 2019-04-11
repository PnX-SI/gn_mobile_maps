package fr.geonature.maps.settings.io

import android.text.TextUtils.isEmpty
import android.util.JsonReader
import android.util.Log
import fr.geonature.maps.settings.MapSettings
import fr.geonature.maps.settings.TileSourceSettings
import org.osmdroid.util.GeoPoint
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.lang.IllegalArgumentException

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
        if (isEmpty(json)) {
            return null
        }

        try {
            return read(StringReader(json))
        }
        catch (ioe: IOException) {
            Log.w(
                TAG,
                ioe.message)
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
    @Throws(IOException::class)
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
     * @throws IOException if something goes wrong
     */
    @Throws(IOException::class)
    private fun read(reader: JsonReader): MapSettings {
        val builder = MapSettings.Builder.newInstance()

        reader.beginObject()

        while (reader.hasNext()) {
            val keyName = reader.nextName()

            when (keyName) {
                "show_scale" -> builder.showScale(reader.nextBoolean())
                "show_compass" -> builder.showCompass(reader.nextBoolean())
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
                                    tokens[1]))
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
                                tokens[1]))
                    }
                }
                "start_zoom", "zoom" -> builder.zoom(reader.nextDouble())
                "min_zoom" -> builder.minZoomLevel(reader.nextDouble())
                "max_zoom" -> builder.maxZoomLevel(reader.nextDouble())
                "min_zoom_editing" -> builder.minZoomEditing(reader.nextDouble())
                "layers" -> readTileSourceSettingsAsList(
                    reader,
                    builder)
            }
        }

        reader.endObject()

        return builder.build()
    }

    @Throws(IOException::class)
    private fun readTileSourceSettingsAsList(reader: JsonReader,
                                             builder: MapSettings.Builder) {
        reader.beginArray()

        while (reader.hasNext()) {
            val tileSourceSettings = readTileSourceSettings(reader)

            if (tileSourceSettings != null) {
                builder.addTileSource(tileSourceSettings)
            }
        }

        reader.endArray()
    }

    @Throws(IOException::class)
    private fun readTileSourceSettings(reader: JsonReader): TileSourceSettings? {
        reader.beginObject()

        val builder = TileSourceSettings.Builder.newInstance()

        while (reader.hasNext()) {
            val keyName = reader.nextName()

            when (keyName) {
                "name" -> builder.name(reader.nextString())
                "label" -> builder.label(reader.nextString())
                "min_zoom" -> builder.minZoomLevel(reader.nextDouble())
                "max_zoom" -> builder.maxZoomLevel(reader.nextDouble())
                "tile_size" -> builder.tileSizePixels(reader.nextInt())
                "image_extension" -> builder.imageExtension(reader.nextString())
            }
        }

        reader.endObject()

        return try {
            builder.build()
        }
        catch (iae: IllegalArgumentException) {
            Log.w(TAG, iae.message)

            null
        }
    }

    companion object {

        private val TAG = MapSettingsReader::class.java.name
    }
}