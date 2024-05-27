package fr.geonature.maps.util

import android.util.JsonReader
import android.util.JsonToken
import android.util.JsonToken.BEGIN_ARRAY
import android.util.JsonToken.BEGIN_OBJECT
import android.util.JsonToken.BOOLEAN
import android.util.JsonToken.NULL
import android.util.JsonToken.NUMBER
import android.util.JsonToken.STRING

/**
 * Utility functions about JsonReader.
 *
 * @author S. Grimault
 */

/**
 * Returns the string value of the next token and consuming it.
 * If the next token is not a string returns `null`.
 */
fun JsonReader.nextStringOrNull(): String? {
    return when (peek()) {
        STRING -> {
            nextString()
        }

        else -> {
            skipValue()
            null
        }
    }
}

/**
 * Returns a Map representation of this JsonReader as JSON object.
 */
fun JsonReader.readObject(): Map<String, Any?>? {
    return when (peek()) {
        BEGIN_OBJECT -> {
            val map = hashMapOf<String, Any?>()

            beginObject()

            while (hasNext()) {
                val name: String = nextName()
                val token: JsonToken = peek()

                when (token) {
                    STRING -> {
                        map[name] = nextString()
                    }

                    NUMBER -> {
                        map[name] =
                            nextString().let { rawValue -> runCatching { rawValue.toLong() }.getOrElse { rawValue.toDouble() } }
                    }

                    NULL -> {
                        map[name] = null
                    }

                    BOOLEAN -> {
                        map[name] = nextBoolean()
                    }

                    BEGIN_OBJECT -> {
                        map[name] = readObject()
                    }

                    BEGIN_ARRAY -> {
                        map[name] = readArray()
                    }

                    else -> {
                        skipValue()
                    }
                }
            }

            endObject()

            map
        }

        else -> {
            skipValue()
            null
        }
    }
}

/**
 * Returns a List representation of this JsonReader as JSON array object.
 */
fun JsonReader.readArray(): List<Any?>? {
    return when (peek()) {
        BEGIN_ARRAY -> {
            val list = mutableListOf<Any?>()

            beginArray()

            while (hasNext()) {
                val token: JsonToken = peek()
                when (token) {
                    STRING -> {
                        list.add(nextString())
                    }

                    NUMBER -> {
                        list.add(runCatching { nextLong() }.getOrElse { nextDouble() })
                    }

                    NULL -> {
                        list.add(null)
                    }

                    BOOLEAN -> {
                        list.add(nextBoolean())
                    }

                    BEGIN_OBJECT -> {
                        readObject()?.also {
                            list.add(it)
                        }
                    }

                    BEGIN_ARRAY -> {
                        readArray()?.also {
                            list.add(it)
                        }
                    }

                    else -> {
                        skipValue()
                    }
                }
            }

            endArray()

            list
        }

        else -> {
            skipValue()
            null
        }
    }
}
