package fr.geonature.maps.jts.geojson.io

import android.os.Bundle
import android.text.TextUtils
import android.util.JsonWriter
import android.util.Log
import fr.geonature.maps.jts.geojson.AbstractGeoJson
import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.FeatureCollection
import java.io.IOException
import java.io.StringWriter
import java.io.Writer
import org.locationtech.jts.geom.CoordinateSequence
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.GeometryCollection
import org.locationtech.jts.geom.LineString
import org.locationtech.jts.geom.MultiLineString
import org.locationtech.jts.geom.MultiPoint
import org.locationtech.jts.geom.MultiPolygon
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon

/**
 * Default `JsonWriter` about writing an [AbstractGeoJson] as `JSON`.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see [https://tools.ietf.org/html/rfc7946](https://tools.ietf.org/html/rfc7946)
 *
 * @see GeoJsonReader
 */
class GeoJsonWriter {

    /**
     * Convert the given [Feature] as `JSON` string.
     *
     * @param feature the [Feature] to convert
     *
     * @return a `JSON` string representation of the given [Feature] or `null` if something goes wrong
     *
     * @see .write
     */
    fun write(feature: Feature?): String? {
        if (feature == null) {
            return null
        }

        val writer = StringWriter()

        try {
            write(
                writer,
                feature
            )
        } catch (ioe: IOException) {
            Log.w(
                TAG,
                ioe.message
            )

            return null
        }

        return writer.toString()
    }

    /**
     * Convert the given [Feature] as `JSON` and write it to the given `Writer`.
     *
     * @param out the `Writer` to use
     * @param feature the [Feature] to convert
     *
     * @throws IOException if something goes wrong
     */
    @Throws(IOException::class)
    fun write(
        out: Writer,
        feature: Feature
    ) {
        val writer = JsonWriter(out)
        writeFeature(
            writer,
            feature
        )
        writer.flush()
        writer.close()
    }

    /**
     * Convert the given [FeatureCollection] as `JSON` string.
     *
     * @param featureCollection the [FeatureCollection] to convert
     *
     * @return a `JSON` string representation of the given [FeatureCollection] or `null` if something goes wrong
     *
     * @see .write
     */
    fun write(featureCollection: FeatureCollection?): String? {
        if (featureCollection == null) {
            return null
        }

        val writer = StringWriter()

        try {
            write(
                writer,
                featureCollection
            )
        } catch (ioe: IOException) {
            Log.w(
                TAG,
                ioe.message
            )

            return null
        }

        return writer.toString()
    }

    /**
     * Convert the given [FeatureCollection] as `JSON` and write it to the given `Writer`.
     *
     * @param out the `Writer` to use
     * @param featureCollection the [FeatureCollection] to convert
     *
     * @throws IOException if something goes wrong
     */
    @Throws(IOException::class)
    fun write(
        out: Writer,
        featureCollection: FeatureCollection
    ) {
        val writer = JsonWriter(out)
        writeFeatureCollection(
            writer,
            featureCollection
        )
        writer.flush()
        writer.close()
    }

    @Throws(IOException::class)
    fun writeFeature(
        writer: JsonWriter,
        feature: Feature
    ) {
        writer.beginObject()
        writer.name("id").value(feature.id)
        writer.name("type").value(feature.type)
        writer.name("geometry")
        writeGeometry(
            writer,
            feature.geometry
        )
        writeProperties(
            writer,
            feature.properties
        )
        writer.endObject()
    }

    @Throws(IOException::class)
    fun writeFeatureCollection(
        writer: JsonWriter,
        featureCollection: FeatureCollection
    ) {
        writer.beginObject()
        writer.name("type").value(featureCollection.type)
        writer.name("features")
        writer.beginArray()

        for (feature in featureCollection.getFeatures()) {
            writeFeature(
                writer,
                feature
            )
        }

        writer.endArray()
        writer.endObject()
    }

    @Throws(IOException::class)
    fun writeGeometry(
        writer: JsonWriter,
        geometry: Geometry
    ) {
        if (TextUtils.isEmpty(geometry.geometryType)) {
            throw IOException("invalid geometry type")
        }

        when (geometry.geometryType) {
            "Point" -> writePoint(
                writer,
                geometry as Point
            )
            "MultiPoint" -> writeMultiPoint(
                writer,
                geometry as MultiPoint
            )
            "LineString" -> writeLineString(
                writer,
                geometry as LineString
            )
            "MultiLineString" -> writeMultiLineString(
                writer,
                geometry as MultiLineString
            )
            "Polygon" -> writePolygon(
                writer,
                geometry as Polygon
            )
            "MultiPolygon" -> writeMultiPolygon(
                writer,
                geometry as MultiPolygon
            )
            "GeometryCollection" -> {
                writer.beginObject()
                writer.name("type").value(geometry.geometryType)
                writer.name("geometries")
                writer.beginArray()

                for (i in 0 until geometry.numGeometries) {
                    writeGeometry(
                        writer,
                        geometry.getGeometryN(i)
                    )
                }

                writer.endArray()
                writer.endObject()
            }
        }
    }

    @Throws(IOException::class)
    private fun writePoint(
        writer: JsonWriter,
        point: Point
    ) {
        writer.beginObject()
        writer.name("type").value(point.geometryType)
        writer.name("coordinates")
        writeCoordinateSequence(
            writer,
            point.coordinateSequence
        )
        writer.endObject()
    }

    @Throws(IOException::class)
    private fun writeMultiPoint(
        writer: JsonWriter,
        multiPoint: MultiPoint
    ) {
        writer.beginObject()
        writer.name("type").value(multiPoint.geometryType)
        writer.name("coordinates")
        writeGeometryCollection(
            writer,
            multiPoint
        )
        writer.endObject()
    }

    @Throws(IOException::class)
    private fun writeLineString(
        writer: JsonWriter,
        lineString: LineString
    ) {
        writer.beginObject()
        writer.name("type").value(lineString.geometryType)
        writer.name("coordinates")
        writeCoordinateSequence(
            writer,
            lineString.coordinateSequence
        )
        writer.endObject()
    }

    @Throws(IOException::class)
    private fun writeMultiLineString(
        writer: JsonWriter,
        multiLineString: MultiLineString
    ) {
        writer.beginObject()
        writer.name("type").value(multiLineString.geometryType)
        writer.name("coordinates")
        writeGeometryCollection(
            writer,
            multiLineString
        )
        writer.endObject()
    }

    @Throws(IOException::class)
    private fun writePolygon(
        writer: JsonWriter,
        polygon: Polygon
    ) {
        writer.beginObject()
        writer.name("type").value(polygon.geometryType)
        writer.name("coordinates")
        writePolygonCoordinates(
            writer,
            polygon
        )

        writer.endObject()
    }

    @Throws(IOException::class)
    private fun writePolygonCoordinates(
        writer: JsonWriter,
        polygon: Polygon
    ) {
        writer.beginArray()
        writeCoordinateSequence(
            writer,
            polygon.exteriorRing.coordinateSequence
        )

        for (i in 0 until polygon.numInteriorRing) {
            writeCoordinateSequence(
                writer,
                polygon.getInteriorRingN(i).coordinateSequence
            )
        }

        writer.endArray()
    }

    @Throws(IOException::class)
    private fun writeMultiPolygon(
        writer: JsonWriter,
        multiPolygon: MultiPolygon
    ) {
        writer.beginObject()
        writer.name("type").value(multiPolygon.geometryType)
        writer.name("coordinates")
        writeGeometryCollection(
            writer,
            multiPolygon
        )
        writer.endObject()
    }

    @Throws(IOException::class)
    private fun writeGeometryCollection(
        writer: JsonWriter,
        geometryCollection: GeometryCollection
    ) {
        writer.beginArray()

        for (i in 0 until geometryCollection.numGeometries) {
            val geometry = geometryCollection.getGeometryN(i)

            if (TextUtils.isEmpty(geometry.geometryType)) {
                Log.w(
                    TAG,
                    "invalid geometry type"
                )
                continue
            }

            when (geometry.geometryType) {
                "Point" -> writeCoordinateSequence(
                    writer,
                    (geometry as Point).coordinateSequence
                )
                "LineString" -> writeCoordinateSequence(
                    writer,
                    (geometry as LineString).coordinateSequence
                )
                "Polygon" -> writePolygonCoordinates(
                    writer,
                    geometry as Polygon
                )
            }
        }

        writer.endArray()
    }

    @Throws(IOException::class)
    private fun writeCoordinateSequence(
        writer: JsonWriter,
        coordinateSequence: CoordinateSequence
    ) {
        if (coordinateSequence.size() > 1) {
            writer.beginArray()
        }

        for (i in 0 until coordinateSequence.size()) {
            writer.beginArray()
            writer.value(
                coordinateSequence.getOrdinate(
                    i,
                    CoordinateSequence.X
                )
            )
            writer.value(
                coordinateSequence.getOrdinate(
                    i,
                    CoordinateSequence.Y
                )
            )

            if (coordinateSequence.dimension > 2) {
                val z = coordinateSequence.getOrdinate(
                    i,
                    CoordinateSequence.Z
                )

                if (!java.lang.Double.isNaN(z)) {
                    writer.value(z)
                }
            }

            writer.endArray()
        }

        if (coordinateSequence.size() > 1) {
            writer.endArray()
        }
    }

    @Throws(IOException::class)
    private fun writeProperties(
        writer: JsonWriter,
        properties: Bundle
    ) {
        writer.name("properties")
        writeBundle(
            writer,
            properties
        )
    }

    @Throws(IOException::class)
    private fun writeBundle(
        writer: JsonWriter,
        bundle: Bundle
    ) {
        writer.beginObject()

        for (key in bundle.keySet()) {
            val value = bundle.get(key)

            if (value is String) {
                writer.name(key).value(value)
            }

            if (value is Boolean) {
                writer.name(key).value(value)
            }

            if (value is Number) {
                writer.name(key).value(value)
            }

            if (value is Bundle) {
                writer.name(key)
                writeBundle(
                    writer,
                    value
                )
            }
        }

        writer.endObject()
    }

    companion object {
        private val TAG = GeoJsonWriter::class.java.name
    }
}
