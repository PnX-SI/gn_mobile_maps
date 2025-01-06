package fr.geonature.maps.layer.repository

import android.net.Uri
import fr.geonature.maps.layer.domain.LayerState
import fr.geonature.maps.settings.LayerSettings

/**
 * [LayerState] repository.
 *
 * @author S. Grimault
 */
interface ILayerRepository {

    /**
     * Loads and prepare given [LayerSettings] from given base path.
     */
    suspend fun prepareLayers(
        layersSettings: List<LayerSettings>,
        basePath: String? = null
    ): List<LayerState>

    /**
     * Gets all layers.
     */
    suspend fun getAllLayers(): List<LayerState>

    /**
     * Creates and add layer from given URI.
     */
    suspend fun addLayerFromURI(uri: Uri): LayerState

    /**
     * Returns the current selected layers.
     *
     * @return a list of selected layers using primary source or an empty list if no selection was
     * made.
     */
    suspend fun getSelectedLayers(): List<LayerState.SelectedLayer>

    /**
     * Updates the layers selection.
     */
    suspend fun setSelectedLayers(selectedLayers: List<LayerState.SelectedLayer>)
}