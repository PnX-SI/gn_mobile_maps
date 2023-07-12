package fr.geonature.maps.layer.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Default implementation of [ISelectedLayersLocalDataSource] using [SharedPreferences].
 *
 * @author S. Grimault
 */
class SelectedLayersLocalDataSourceImpl(
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ISelectedLayersLocalDataSource {

    private val preferenceManager: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    override suspend fun getSelectedLayers(): Set<Uri> = withContext(dispatcher) {
        preferenceManager.getStringSet(
            KEY_SELECTED_LAYERS,
            emptySet()
        )
            ?.asSequence()
            ?.mapNotNull { it?.let { Uri.parse(it) } }
            ?.toSet() ?: emptySet()
    }

    override suspend fun setSelectedLayers(selectedLayers: Set<Uri>) = withContext(dispatcher) {
        preferenceManager.edit(commit = true) {
            putStringSet(KEY_SELECTED_LAYERS,
                selectedLayers.asSequence()
                    .map { it.toString() }
                    .toSet())
        }
    }

    companion object {
        private const val KEY_SELECTED_LAYERS = "key_selected_layers"
    }
}