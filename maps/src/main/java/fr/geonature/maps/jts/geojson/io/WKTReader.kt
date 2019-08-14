package fr.geonature.maps.jts.geojson.io

import android.util.Log
import fr.geonature.maps.jts.geojson.AbstractGeoJson
import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.FeatureCollection
import org.locationtech.jts.io.ParseException
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader
import java.util.ArrayList
import java.util.regex.Pattern

/**
 * Converts a GeoJSON in Well-Known Text format to a [AbstractGeoJson] implementation.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class WKTReader {

    private val wktLinePattern = Pattern.compile("^([0-9]+),([A-Z]+\\s*\\(.+\\))$")
    private val wktReader: org.locationtech.jts.io.WKTReader = org.locationtech.jts.io.WKTReader()

    /**
     * parse a Well-Known Text format reader to convert as [Feature].
     *
     * @param in       the `Reader` to use
     * @param listener the callback to monitor the progression
     */
    fun readFeatures(`in`: Reader,
                     listener: OnWKTReaderListener) {
        val featureCollection = FeatureCollection()
        val bufferedReader = BufferedReader(`in`)
        var currentLine = 0
        var line: String?

        try {
            line = bufferedReader.readLine()

            do {
                val matcher = wktLinePattern.matcher(line)

                if (matcher.matches()) {
                    try {
                        val feature = Feature(matcher.group(1),
                                              wktReader.read(matcher.group(2)))
                        featureCollection.addFeature(feature)

                        listener.onProgress(currentLine + 1,
                                            feature)
                    }
                    catch (pe: ParseException) {
                        Log.w(TAG,
                              pe.message)
                    }

                }

                currentLine++

                line = bufferedReader.readLine()
            }
            while (line != null)

            listener.onFinish(featureCollection)
            bufferedReader.close()
        }
        catch (ioe: IOException) {
            Log.w(TAG,
                  ioe.message)

            listener.onError(ioe)
        }

    }

    /**
     * parse a Well-Known Text format reader to convert as [Feature].
     *
     * @param in the `Reader` to use
     * @return a [Feature] instance from the `Reader`
     */
    fun readFeatures(`in`: Reader): List<Feature> {
        val features = ArrayList<Feature>()
        readFeatures(`in`,
                     object : OnWKTReaderListener {
                         override fun onProgress(progress: Int,
                                                 feature: Feature) {
                             features.add(feature)
                         }

                         override fun onFinish(featureCollection: FeatureCollection) {}

                         override fun onError(t: Throwable) {}
                     })

        return features
    }

    /**
     * parse a Well-Known Text format reader to convert as [FeatureCollection].
     *
     * @param in the `Reader` to use
     * @return a [FeatureCollection] instance from the `Reader`
     */
    fun readFeatureCollection(`in`: Reader): FeatureCollection {
        val featureCollection = FeatureCollection()
        featureCollection.addAllFeatures(readFeatures(`in`))

        return featureCollection
    }

    /**
     * Callback used by [WKTReader].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    interface OnWKTReaderListener {

        fun onProgress(progress: Int,
                       feature: Feature)

        fun onFinish(featureCollection: FeatureCollection)

        fun onError(t: Throwable)
    }

    companion object {

        private val TAG = WKTReader::class.java.name
    }
}
