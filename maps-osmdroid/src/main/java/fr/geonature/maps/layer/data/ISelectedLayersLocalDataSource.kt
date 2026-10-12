package fr.geonature.maps.layer.data

import android.net.Uri
import fr.geonature.maps.settings.LayerSettings

/**
 * Keeps track of selected [LayerSettings] using the primary source.
 *
 * @author S. Grimault
 */
interface ISelectedLayersLocalDataSource {

    /**
     * Returns the current selected layers.
     *
     * @return a list of selected layers using primary source or an empty list if no selection was
     * made.
     */
    suspend fun getSelectedLayers(): Set<Uri>

    /**
     * Updates the layers selection.
     */
    suspend fun setSelectedLayers(selectedLayers: Set<Uri>)
}