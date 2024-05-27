package fr.geonature.maps.jts.geojson.io

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
import fr.geonature.maps.util.nextStringOrNull
import fr.geonature.maps.util.readObject
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.LineString
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
 * @author S. Grimault
 *
 * @see <a href="https://tools.ietf.org/html/rfc7946">https://tools.ietf.org/html/rfc7946</a>
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
        return when (reader.peek()) {
            BEGIN_OBJECT -> {
                val features = mutableListOf<Feature>()

                var id: String? = null
                var type: String? = null
                var geometry: Geometry? = null
                var properties: Map<String, Any>? = null

                reader.beginObject()

                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "id" -> id = reader.nextStringOrNull()
                        "type" -> type = reader.nextString()
                        "geometry" -> geometry = readGeometry(reader)
                        "properties" -> properties = readProperties(reader)
                        "features" -> {
                            reader.beginArray()

                            while (reader.hasNext()) {
                                runCatching { readFeature(reader) }.onFailure { it.message?.also { m-> Logger.warn(it) { m } } }
                                    .getOrNull()
                                    ?.also {
                                        features.add(it)
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
                    id = if (id.isNullOrBlank()) properties?.get("id")
                        ?.toString() else id

                    if (geometry == null) {
                        throw IOException("No geometry found for feature $id")
                    }

                    val feature = Feature(
                        id,
                        geometry
                    )

                    if (!properties.isNullOrEmpty()) {
                        feature.properties.putAll(properties)
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

            else -> emptyList()
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
        var properties: Map<String, Any>? = null

        reader.beginObject()

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "id" -> id = reader.nextStringOrNull()
                "type" -> type = reader.nextString()
                "geometry" -> geometry = readGeometry(reader)
                "properties" -> properties = readProperties(reader)
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        // try to find ID value from properties
        id = if (id.isNullOrBlank()) properties?.get("id")
            ?.toString() else id

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

        if (!properties.isNullOrEmpty()) {
            feature.properties.putAll(properties)
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
                val asObject =
                    reader.readObject() ?: throw IOException("Invalid object as Geometry")
                readGeometry(asObject)
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
    private fun readGeometry(map: Map<String, Any?>): Geometry {
        return when (val type = map["type"] as String) {
            "Point" -> readPoint((map["coordinates"] as List<*>).map { (it as Number).toDouble() })
            "MultiPoint" -> readMultiPoint((map["coordinates"] as List<*>).map { c0 -> (c0 as List<*>).map { (it as Number).toDouble()} })
            "LineString" -> readLineString((map["coordinates"] as List<*>).map { c0 -> (c0 as List<*>).map { (it as Number).toDouble() } })
            "MultiLineString" -> readMultiLineString((map["coordinates"] as List<*>).map { c0 -> (c0 as List<*>).map { c1 -> (c1 as List<*>).map { (it as Number).toDouble() } } })
            "Polygon" -> readPolygon((map["coordinates"] as List<*>).map { c0 -> (c0 as List<*>).map { c1 -> (c1 as List<*>).map { (it as Number).toDouble() } } })
            "MultiPolygon" -> readMultiPolygon((map["coordinates"] as List<*>).map { c0 -> (c0 as List<*>).map { c1 -> (c1 as List<*>).map { c2 -> (c2 as List<*>).map { (it as Number).toDouble() } } } })
            "GeometryCollection" -> readGeometryCollection((map["geometries"] as List<*>).map {
                (it as Map<*, *>).map { e -> e.key as String to e.value }
                    .toMap()
            })

            else -> throw IOException("No such geometry type '$type'")
        }
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readPoint(coordinates: List<Double>): Point {
        return gf.createPoint(readCoordinate(coordinates))
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readMultiPoint(coordinates: List<List<Double>>): MultiPoint {
        return gf.createMultiPointFromCoords(readCoordinates(coordinates))
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readLineString(coordinates: List<List<Double>>): LineString {
        return gf.createLineString(readCoordinates(coordinates))
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readMultiLineString(coordinates: List<List<List<Double>>>): MultiLineString {
        return gf.createMultiLineString(coordinates.map {
            readLineString(it)
        }
            .toTypedArray())
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readPolygon(coordinates: List<List<List<Double>>>): Polygon {
        val linearRings = coordinates.map {
            gf.createLinearRing(readCoordinates(it))
        }

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
    private fun readMultiPolygon(coordinates: List<List<List<List<Double>>>>): MultiPolygon {
        return gf.createMultiPolygon(coordinates.map {
            readPolygon(it)
        }
            .toTypedArray())
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readGeometryCollection(geometries: List<Map<String, Any?>>): GeometryCollection {
        return gf.createGeometryCollection(geometries.map { readGeometry(it) }
            .toTypedArray())
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readCoordinates(coordinates: List<List<Double>>): Array<Coordinate> {
        return coordinates.map {
            readCoordinate(it)
        }
            .toTypedArray()
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readCoordinate(coordinates: List<Double>): Coordinate {
        return Coordinate().apply {
            coordinates.forEachIndexed { index, d ->
                setOrdinate(
                    index,
                    d
                )
            }
        }
    }

    @Throws(
        IOException::class,
        IllegalStateException::class
    )
    private fun readProperties(reader: JsonReader): Map<String, Any> {
        val properties = hashMapOf<String, Any>()

        if (reader.peek() == BEGIN_OBJECT) {
            reader.beginObject()
            var key: String? = null

            while (reader.hasNext()) {
                when (reader.peek()) {
                    NAME -> key = reader.nextName()
                    STRING -> if (!key.isNullOrBlank()) {
                        properties[key] = reader.nextString()
                    }

                    BOOLEAN -> if (!key.isNullOrBlank()) {
                        properties[key] = reader.nextBoolean()
                    }

                    NUMBER -> if (!key.isNullOrBlank()) {
                        val rawValue = reader.nextString()

                        runCatching { parseInt(rawValue) }.recoverCatching { parseDouble(rawValue) }
                            .getOrNull()
                            ?.also { v ->
                                key?.also { k -> properties[k] = v }
                            }
                    }

                    BEGIN_OBJECT -> if (!key.isNullOrBlank()) {
                        readProperties(reader).takeIf { it.isNotEmpty() }
                            ?.also { v ->
                                key?.also { k -> properties[k] = v }
                            }
                    }

                    else -> {
                        key = null
                        reader.skipValue()
                    }
                }
            }

            reader.endObject()
        }

        return properties
    }
}
