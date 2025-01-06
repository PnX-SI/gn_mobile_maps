package fr.geonature.maps.layer.data

import android.net.Uri

/**
 * In memory implementation of [ISelectedLayersLocalDataSource].
 *
 * @author S. Grimault
 */
class InMemorySelectedLayersLocalDataSourceImpl : ISelectedLayersLocalDataSource {

    private val uris = mutableSetOf<Uri>()

    override suspend fun getSelectedLayers(): Set<Uri> {
        return uris
    }

    override suspend fun setSelectedLayers(selectedLayers: Set<Uri>) {
        with(uris) {
            clear()
            addAll(selectedLayers)
        }
    }
}