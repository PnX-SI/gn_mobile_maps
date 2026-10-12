package fr.geonature.maps.layer.data

import android.net.Uri
import fr.geonature.maps.layer.domain.LayerState
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.settings.LayerSettings

/**
 * Data source about [LayerSettings] using local source.
 *
 * @author S. Grimault
 */
interface ILayerLocalDataSource {

    /**
     * Resolves all sources from given [LayerSettings] and returns all valid sources as URI.
     *
     * @throws LayerException if something goes wrong or if the given [LayerSettings] is not valid.
     */
    suspend fun resolvesLocalLayerFromLayerSettings(
        layerSettings: LayerSettings,
        basePath: String? = null
    ): List<Uri>

    /**
     * Builds the corresponding [LayerState] from given URI.
     *
     * Returns [LayerState.Error] if something goes wrong or if the given URI is not valid.
     */
    suspend fun buildLocalLayerFromUri(uri: Uri): LayerState
}