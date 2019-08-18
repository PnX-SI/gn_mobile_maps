package fr.geonature.maps.ui.overlay.feature

import android.util.Log
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.maps.jts.geojson.io.WKTReader
import fr.geonature.maps.settings.LayerSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.views.overlay.Overlay
import java.io.File
import java.io.FileReader

/**
 * Loads vector layers as map Overlays from sources.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class FeatureOverlayProvider(private val basePath: String) {

    suspend fun loadFeaturesAsOverlays(layersSettings: List<LayerSettings>): List<Overlay> =
            withContext(Dispatchers.IO) {
                layersSettings.asSequence()
                        .filter { layerSettings ->
                            arrayOf(".geojson",
                                    ".json",
                                    ".wkt").any {
                                layerSettings.source.endsWith(it)
                            }
                        }
                        .map { layerSettings ->
                            Log.i(TAG,
                                  "Loading vector layer '${layerSettings.label}'...")


                            Pair(layerSettings,
                                 File(basePath,
                                      layerSettings.source))
                        }
                        .filter { it.second.exists() && it.second.canRead() }
                        .map {
                            when (it.second.extension) {
                                "geojson", "json" -> Pair(it.first,
                                                          GeoJsonReader().read(FileReader(it.second)))
                                "wkt" -> Pair(it.first,
                                              WKTReader().readFeatures(FileReader(it.second)))
                                else -> Pair(it.first,
                                             emptyList())
                            }
                        }
                        .filter {
                            val isLoaded = it.second.isNotEmpty()

                            if (isLoaded) {
                                Log.i(TAG,
                                      "Vector layer '${it.first.label}' loaded")
                            }
                            else {
                                Log.w(TAG,
                                      "Failed to load vector layer '${it.first.label}'")
                            }

                            isLoaded
                        }
                        .map {
                            FeatureCollectionOverlay().apply {
                                setFeatures(it.second,
                                            it.first.layerStyle)
                            }
                        }
                        .toList()
            }

    companion object {
        private val TAG = FeatureOverlayProvider::class.java.name
    }
}