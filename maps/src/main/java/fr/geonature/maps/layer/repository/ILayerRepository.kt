package fr.geonature.maps.layer.repository

import android.net.Uri
import fr.geonature.maps.settings.LayerSettings

/**
 * [LayerSettings] repository.
 *
 * @author S. Grimault
 */
interface ILayerRepository {

    /**
     * Loads and prepare given layers with given base path.
     */
    suspend fun prepareLayers(
        layersSettings: List<LayerSettings>,
        basePath: String? = null
    ): Result<List<LayerSettings>>

    /**
     * Gets all layers.
     */
    suspend fun getAllLayers(): Result<List<LayerSettings>>

    /**
     * Creates and add [LayerSettings] from given URI.
     */
    suspend fun addLayerFromURI(uri: Uri): Result<LayerSettings>

    /**
     * Returns the current selected layers.
     *
     * @return a list of selected layers using primary source or an empty list if no selection was
     * made.
     */
    suspend fun getSelectedLayers(): Result<List<LayerSettings>>

    /**
     * Updates the layers selection.
     */
    suspend fun setSelectedLayers(selectedLayers: List<LayerSettings>)
}