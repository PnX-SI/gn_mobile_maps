package fr.geonature.maps.jts.geojson.io

import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.LineNumberReader
import fr.geonature.maps.jts.geojson.AbstractGeoJson
import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.FeatureCollection

/**
 * Converts a GeoJSON in Well-Known Text format from `File` to a [AbstractGeoJson] implementation.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see WKTReader
 */
class WKTFileReader {

    private val wktReader: WKTReader = WKTReader()
    private var lineNumber: Int = 0

    /**
     * parse a Well-Known Text format reader to convert as [Feature].
     *
     * @param wkt      the WKT `File` to read
     * @param listener the callback to monitor the progression
     */
    fun readFeatures(
        wkt: File,
        listener: OnWKTFileReaderListener
    ) {
        try {
            val lineNumberReader = LineNumberReader(FileReader(wkt))

            while (lineNumberReader.readLine() != null) {

            }

            lineNumber = lineNumberReader.lineNumber
            listener.onStart(lineNumber)

            val onWKTReaderListener = object : WKTReader.OnWKTReaderListener {
                override fun onProgress(
                    progress: Int,
                    feature: Feature
                ) {
                    listener.onProgress(
                        progress,
                        lineNumber,
                        feature
                    )
                }

                override fun onFinish(featureCollection: FeatureCollection) {
                    listener.onFinish(featureCollection)
                }

                override fun onError(t: Throwable) {
                    listener.onError(t)
                }
            }

            wktReader.readFeatures(
                FileReader(wkt),
                onWKTReaderListener
            )
        }
        catch (ioe: IOException) {
            listener.onError(ioe)
        }
    }

    /**
     * Callback used by [WKTFileReader].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    interface OnWKTFileReaderListener : WKTReader.OnWKTReaderListener {

        fun onStart(size: Int)

        fun onProgress(
            progress: Int,
            size: Int,
            feature: Feature
        )
    }
}
