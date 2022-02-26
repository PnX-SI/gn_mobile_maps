package fr.geonature.maps.jts.geojson.io

import android.os.Bundle
import android.util.JsonReader
import android.util.JsonToken.BEGIN_ARRAY
import android.util.JsonToken.BEGIN_OBJECT
import android.util.JsonToken.BOOLEAN
import android.util.JsonToken.NAME
import android.util.JsonToken.NULL
import android.util.JsonToken.NUMBER
import android.util.JsonToken.STRING
import fr.geonature.maps.jts.geojson.AbstractGeoJson
import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.FeatureCollection
import fr.geonature.maps.util.nextName
import fr.geonature.maps.util.nextStringOrNull
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.LinearRing
import org.locationtech.jts.geom.MultiLineString
import org.locationtech.jts.geom.MultiPoint
import org.locationtech.jts.geom.MultiPolygon
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon
import org.tinylog.kotlin.Logger
import java.io.IOException
import java.io.Reader
import java.lang.Double.parseDouble
import java.lang.Integer.parseInt

/**
 * Default `JsonReader` about reading a `JSON` stream and build the corresponding
 * [AbstractGeoJson] implementation.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see [https://tools.ietf.org/html/rfc7946](https://tools.ietf.org/html/rfc7946)
 *
 * @see GeoJsonWriter
 */
class GeoJsonReader {

    private val gf: GeometryFactory = GeometryFactory()

    /**
     * parse a `JSON` string to convert as list of [Feature]s.
     *
     * @param json the `JSON` string to parse
     *
     * @return a list of [Feature]s from the `JSON` string or empty list if something goes wrong
     *
     * @see .read
     */
    fun read(json: String?): List<Feature> {
        if (json.isNullOrBlank()) {
            return emptyList()
        }

        try {
            return read(json.reader())
        } catch (e: Exception) {
            Logger.warn(e)
        }

        return emptyList()
    }

    /**
     * parse a `JSON` reader to convert as list of [Feature]s.
     *
     * @param in the `Reader` to parse
     *
     * @return a list of [Feature]s from the `JSON` reader
     *
     * @throws IOException if something goes wrong
     */
    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    fun read(`in`: Reader): List<Feature> {
        val jsonReader = JsonReader(`in`)
        val features = read(jsonReader)
        jsonReader.close()

        return features
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    fun read(reader: JsonReader): List<Feature> {
        @Suppress("RemoveExplicitTypeArguments") return when (reader.peek()) {
            BEGIN_OBJECT -> {
                val features = mutableListOf<Feature>()
                var id: String? = null
                var type: String? = null
                var geometry: Geometry? = null
                var bundle: Bundle? = null

                reader.beginObject()

                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "id" -> id = reader.nextString()
                        "type" -> type = reader.nextString()
                        "geometry" -> geometry = readGeometry(reader)
                        "properties" -> bundle = readProperties(reader)
                        "features" -> {
                            reader.beginArray()

                            while (reader.hasNext()) {
                                try {
                                    features.add(readFeature(reader))
                                } catch (e: Exception) {
                                    Logger.warn(e)
                                }
                            }

                            reader.endArray()
                        }
                        else -> reader.skipValue()
                    }
                }

                reader.endObject()

                if ("Feature" == type) {
                    // try to find ID value from properties
                    id = if (id.isNullOrBlank()) bundle?.get("id")
                        ?.toString() else id

                    if (id.isNullOrBlank()) {
                        throw IOException("No id found for feature")
                    }

                    if (geometry == null) {
                        throw IOException("No geometry found for feature $id")
                    }

                    val feature = Feature(
                        id,
                        geometry
                    )

                    if (bundle != null && !bundle.isEmpty) {
                        feature.properties.putAll(bundle)
                    }

                    features.add(feature)
                }

                return features
            }
            BEGIN_ARRAY -> {
                val features = mutableListOf<Feature>()

                reader.beginArray()

                while (reader.hasNext()) {
                    val feature = try {
                        readFeature(reader)
                    } catch (e: Exception) {
                        Logger.warn(e)

                        null
                    }

                    if (feature != null) features.add(feature)
                }

                reader.endArray()

                return features
            }
            else -> emptyList<Feature>()
        }
    }

    /**
     * parse a `JSON` string to convert as [Feature].
     *
     * @param json the `JSON` string to parse
     *
     * @return a [Feature] instance from the `JSON` string or `null` if something goes wrong
     *
     * @see .readFeature
     */
    fun readFeature(json: String?): Feature? {
        if (json.isNullOrBlank()) {
            return null
        }

        try {
            return readFeature(json.reader())
        } catch (e: Exception) {
            Logger.warn(e)
        }

        return null
    }

    /**
     * parse a `JSON` reader to convert as [Feature].
     *
     * @param in the `Reader` to parse
     *
     * @return a [Feature] instance from the `JSON` reader
     *
     * @throws IOException if something goes wrong
     */
    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    fun readFeature(`in`: Reader): Feature {
        val jsonReader = JsonReader(`in`)
        val feature = readFeature(jsonReader)
        jsonReader.close()

        return feature
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    fun readFeature(reader: JsonReader): Feature {
        var id: String? = null
        var type: String? = null
        var geometry: Geometry? = null
        var bundle: Bundle? = null

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextString()
                "type" -> type = reader.nextString()
                "geometry" -> geometry = readGeometry(reader)
                "properties" -> bundle = readProperties(reader)
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        // try to find ID value from properties
        id = if (id.isNullOrBlank()) bundle?.get("id")
            ?.toString() else id

        if (id.isNullOrBlank()) {
            throw IOException("No id found for feature")
        }

        if ("Feature" != type) {
            throw IOException("No such type found for feature $id")
        }

        if (geometry == null) {
            throw IOException("No geometry found for feature $id")
        }

        val feature = Feature(
            id,
            geometry
        )

        if (bundle != null && !bundle.isEmpty) {
            feature.properties.putAll(bundle)
        }

        return feature
    }

    /**
     * parse a `JSON` string to convert as [FeatureCollection].
     *
     * @param json the `JSON` string to parse
     *
     * @return a [FeatureCollection] instance from the `JSON` string or `null` if something goes wrong
     *
     * @see .readFeatureCollection
     */
    fun readFeatureCollection(json: String?): FeatureCollection? {
        if (json.isNullOrBlank()) {
            return null
        }

        try {
            return readFeatureCollection(json.reader())
        } catch (e: Exception) {
            Logger.warn(e)
        }

        return null
    }

    /**
     * parse a `JSON` reader to convert as [FeatureCollection].
     *
     * @param in the `Reader` to parse
     *
     * @return a [FeatureCollection] instance from the `JSON` reader
     *
     * @throws IOException if something goes wrong
     */
    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    fun readFeatureCollection(`in`: Reader): FeatureCollection {
        val jsonReader = JsonReader(`in`)
        val featureCollection = readFeatureCollection(jsonReader)
        jsonReader.close()

        return featureCollection
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    fun readFeatureCollection(reader: JsonReader): FeatureCollection {
        val featureCollection = FeatureCollection()

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "type" -> if ("FeatureCollection" != reader.nextString()) {
                    throw IOException("No such type found for FeatureCollection")
                }
                "features" -> {
                    reader.beginArray()

                    while (reader.hasNext()) {
                        try {
                            featureCollection.addFeature(readFeature(reader))
                        } catch (e: Exception) {
                            Logger.warn(e)
                        }
                    }

                    reader.endArray()
                }
            }
        }

        reader.endObject()

        return featureCollection
    }

    /**
     * parse a `JSON` string to convert as `Geometry`.
     *
     * @param json the `JSON` string to parse
     *
     * @return a `Geometry` instance from the `JSON` string or `null` if something goes wrong
     *
     * @see .readGeometry
     */
    fun readGeometry(json: String?): Geometry? {
        if (json.isNullOrBlank()) {
            return null
        }

        try {
            return readGeometry(json.reader())
        } catch (e: Exception) {
            Logger.warn(e)
        }

        return null
    }

    /**
     * parse a `JSON` reader to convert as `Geometry`.
     *
     * @param in the `Reader` to parse
     *
     * @return a `Geometry` instance from the `JSON` reader
     *
     * @throws IOException if something goes wrong
     */
    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    fun readGeometry(`in`: Reader): Geometry {
        val jsonReader = JsonReader(`in`)
        val geometry = readGeometry(jsonReader)
        jsonReader.close()

        return geometry
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    fun readGeometry(reader: JsonReader): Geometry {
        return when (reader.peek()) {
            NULL -> {
                reader.nextNull()
                throw IOException("Geometry must not be null")
            }
            BEGIN_OBJECT -> {
                reader.beginObject()

                val type = reader
                    .nextName(
                        "type",
                        STRING
                    )
                    .nextStringOrNull() ?: throw IOException("Missing 'type' property for Geometry")

                val geometry: Geometry

                when (type) {
                    "Point" -> geometry = readPoint(reader)
                    "MultiPoint" -> geometry = readMultiPoint(reader)
                    "LineString" -> geometry = readLineString(
                        reader,
                        true
                    )
                    "MultiLineString" -> geometry = readMultiLineString(reader)
                    "Polygon" -> geometry = readPolygon(
                        reader,
                        true
                    )
                    "MultiPolygon" -> geometry = readMultiPolygon(reader)
                    "GeometryCollection" -> geometry = readGeometryCollection(reader)
                    else -> throw IOException("No such geometry type '$type'")
                }

                reader.endObject()

                geometry
            }
            else -> {
                reader.skipValue()
                throw IOException("Invalid Geometry")
            }
        }
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readPoint(reader: JsonReader): Point {
        reader.nextName(
            "coordinates",
            BEGIN_ARRAY
        )

        return gf.createPoint(readCoordinate(reader))
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readMultiPoint(reader: JsonReader): MultiPoint {
        reader.nextName(
            "coordinates",
            BEGIN_ARRAY
        )

        return gf.createMultiPointFromCoords(readCoordinates(reader))
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readLineString(
        reader: JsonReader,
        readCoordinatesJsonKey: Boolean
    ): LineString {
        if (readCoordinatesJsonKey) {
            reader.nextName(
                "coordinates",
                BEGIN_ARRAY
            )
        }

        return gf.createLineString(readCoordinates(reader))
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readMultiLineString(reader: JsonReader): MultiLineString {
        reader.nextName(
            "coordinates",
            BEGIN_ARRAY
        )

        val lineStrings = ArrayList<LineString>()

        reader.beginArray()

        while (reader.hasNext()) {
            lineStrings.add(
                readLineString(
                    reader,
                    false
                )
            )
        }

        reader.endArray()

        return gf.createMultiLineString(lineStrings.toTypedArray())
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readPolygon(
        reader: JsonReader,
        readCoordinatesJsonKey: Boolean
    ): Polygon {
        if (readCoordinatesJsonKey) {
            reader.nextName(
                "coordinates",
                BEGIN_ARRAY
            )
        }

        val linearRings = ArrayList<LinearRing>()

        reader.beginArray()

        while (reader.hasNext()) {
            linearRings.add(gf.createLinearRing(readCoordinates(reader)))
        }

        reader.endArray()

        if (linearRings.isEmpty()) {
            throw IOException("No coordinates defined for polygon")
        }

        // this is a polygon with no holes defined
        return if (linearRings.size == 1) {
            gf.createPolygon(linearRings[0])
        } else {
            gf.createPolygon(
                linearRings[0],
                linearRings.subList(
                    1,
                    linearRings.size
                )
                    .toTypedArray()
            )
        }
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readMultiPolygon(reader: JsonReader): MultiPolygon {
        reader.nextName(
            "coordinates",
            BEGIN_ARRAY
        )

        val polygons = ArrayList<Polygon>()

        reader.beginArray()

        while (reader.hasNext()) {
            polygons.add(
                readPolygon(
                    reader,
                    false
                )
            )
        }

        reader.endArray()

        return gf.createMultiPolygon(polygons.toTypedArray())
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readGeometryCollection(reader: JsonReader): GeometryCollection {
        reader.nextName(
            "geometries",
            BEGIN_ARRAY
        )

        val geometries = ArrayList<Geometry>()

        reader.beginArray()

        while (reader.hasNext()) {
            geometries.add(readGeometry(reader))
        }

        reader.endArray()

        return gf.createGeometryCollection(geometries.toTypedArray())
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readCoordinates(reader: JsonReader): Array<Coordinate> {
        val coordinates = ArrayList<Coordinate>()

        reader.beginArray()

        while (reader.hasNext()) {
            coordinates.add(readCoordinate(reader))
        }

        reader.endArray()

        return coordinates.toTypedArray()
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readCoordinate(reader: JsonReader): Coordinate {
        val coordinate = Coordinate()
        var ordinateIndex = 0

        reader.beginArray()

        while (reader.hasNext()) {
            when (val jsonToken = reader.peek()) {
                NUMBER -> if (ordinateIndex < 3) {
                    coordinate.setOrdinate(
                        ordinateIndex,
                        reader.nextDouble()
                    )
                    ordinateIndex++
                }
                else -> throw IOException("Invalid coordinate JSON token $jsonToken")
            }
        }

        reader.endArray()

        return coordinate
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readProperties(reader: JsonReader): Bundle {
        val bundle = Bundle()

        if (reader.peek() == BEGIN_OBJECT) {
            reader.beginObject()
            var key: String? = null

            while (reader.hasNext()) {
                when (reader.peek()) {
                    NAME -> key = reader.nextName()
                    STRING -> if (!key.isNullOrBlank()) {
                        bundle.putString(
                            key,
                            reader.nextString()
                        )
                    }
                    BOOLEAN -> if (!key.isNullOrBlank()) {
                        bundle.putBoolean(
                            key,
                            reader.nextBoolean()
                        )
                    }
                    NUMBER -> if (!key.isNullOrBlank()) {
                        val rawValue = reader.nextString()

                        try {
                            bundle.putInt(
                                key,
                                parseInt(rawValue)
                            )
                        } catch (nfe: NumberFormatException) {
                            bundle.putDouble(
                                key,
                                parseDouble(rawValue)
                            )
                        }
                    }
                    BEGIN_OBJECT -> if (!key.isNullOrBlank()) {
                        bundle.putBundle(
                            key,
                            readProperties(reader)
                        )
                    }
                    else -> {
                        key = null
                        reader.skipValue()
                    }
                }
            }

            reader.endObject()
        }

        return bundle
    }
}
