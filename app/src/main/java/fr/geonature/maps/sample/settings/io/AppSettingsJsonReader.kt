package fr.geonature.maps.sample.settings.io

import android.text.TextUtils
import android.util.JsonReader
import android.util.Log
import fr.geonature.maps.sample.settings.IAppSettings
import java.io.IOException
import java.io.Reader
import java.io.StringReader

/**
 * Default [JsonReader] about reading a `JSON` stream and build the corresponding [IAppSettings] metadata.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class AppSettingsJsonReader<T : IAppSettings>(private val onAppSettingsJsonReaderListener: OnAppSettingsJsonReaderListener<T>) {

    /**
     * parse a `JSON` string to convert as [IAppSettings].
     *
     * @param json the `JSON` string to parse
     *
     * @return a [IAppSettings] instance from the `JSON` string or `null` if something goes wrong
     */
    fun read(json: String?): T? {
        if (TextUtils.isEmpty(json)) {
            return null
        }

        try {
            return read(StringReader(json))
        }
        catch (e: Exception) {
            Log.w(
                TAG,
                e.message
            )
        }

        return null
    }

    /**
     * parse a `JSON` reader to convert as [IAppSettings].
     *
     * @param reader the [Reader] to parse
     *
     * @return a [IAppSettings] instance from the `JSON` reader
     *
     * @throws IOException if something goes wrong
     */
    @Throws(
        IOException::class,
        IllegalArgumentException::class
    )
    fun read(reader: Reader): T {
        val jsonReader = JsonReader(reader)
        val appSettings = read(jsonReader)
        jsonReader.close()

        return appSettings
    }

    /**
     * Use a [JsonReader] instance to convert as [IAppSettings].
     *
     * @param reader the [JsonReader] to use
     *
     * @return a [IAppSettings] instance from [JsonReader]
     *
     * @throws IOException if something goes wrong
     */
    @Throws(
        IOException::class,
        IllegalArgumentException::class
    )
    private fun read(reader: JsonReader): T {
        val appSettings = onAppSettingsJsonReaderListener.createAppSettings()

        reader.beginObject()

        while (reader.hasNext()) {
            when (val keyName = reader.nextName()) {
                else -> onAppSettingsJsonReaderListener.readAdditionalAppSettingsData(
                    reader,
                    keyName,
                    appSettings
                )
            }
        }

        reader.endObject()

        return appSettings
    }

    /**
     * Callback used by [AppSettingsJsonReader].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    interface OnAppSettingsJsonReaderListener<T : IAppSettings> {

        /**
         * Returns a new instance of [IAppSettings].
         *
         * @return new instance of [IAppSettings]
         */
        fun createAppSettings(): T

        /**
         * Reading some additional data to set to the given [IAppSettings].
         *
         * @param reader  the current @code JsonReader} to use
         * @param keyName the JSON key read
         * @param appSettings   the current [AbstractInput] to use
         *
         * @throws IOException if something goes wrong
         * @throws IllegalArgumentException if invalid parameter was given
         */
        @Throws(
            IOException::class,
            IllegalArgumentException::class
        )
        fun readAdditionalAppSettingsData(
            reader: JsonReader,
            keyName: String,
            appSettings: T
        )
    }

    companion object {

        private val TAG = AppSettingsJsonReader::class.java.name
    }
}