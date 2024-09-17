package fr.geonature.maps.layer.data

import android.net.Uri
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.settings.LayerSettings

/**
 * Data source about [LayerSettings] using local source.
 *
 * @author S. Grimault
 */
interface ILayerLocalDataSource {

    /**
     * Resolves all sources from given [LayerSettings] and returns a copy of this [LayerSettings]
     * with these valid sources.
     *
     * @throws LayerException if something goes wrong or if the given
     * [LayerSettings] is not valid.
     */
    suspend fun resolvesLocalLayerFromLayerSettings(
        layerSettings: LayerSettings,
        basePath: String? = null
    ): LayerSettings

    /**
     * Builds the corresponding [LayerSettings] from given URI.
     *
     * @throws LayerException if something goes wrong or if the given
     * URI is not valid.
     */
    suspend fun buildLocalLayerFromUri(uri: Uri): LayerSettings
}