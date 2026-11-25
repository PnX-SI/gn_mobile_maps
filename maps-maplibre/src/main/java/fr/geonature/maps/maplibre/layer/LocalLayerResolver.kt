package fr.geonature.maps.maplibre.layer

import android.content.Context
import androidx.core.net.toUri
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.mountpoint.util.FileUtils
import fr.geonature.mountpoint.util.MountPointUtils
import fr.geonature.mountpoint.util.find
import org.tinylog.kotlin.Logger
import java.io.File

/**
 * Resolves the files of local layers (embedded tiles) from the storage.
 *
 * Absolute sources are used as is. Relative sources are searched from the base tiles path, then
 * from the external storage and finally from the internal storage.
 *
 * **Note:** this performs disk accesses and should not be called from the main thread.
 *
 * @author S. Grimault
 */
class LocalLayerResolver(
    context: Context,
    private val baseTilesPath: String?
) {

    private val externalRootPath = FileUtils.getExternalStorageDirectory(context)
    private val internalRootPath = MountPointUtils.getInternalStorage(context).mountPath

    fun resolve(layerSettings: LayerSettings): List<File> {
        if (layerSettings.isOnline()) return emptyList()

        val (absolutePaths, relativePaths) = layerSettings.source.partition {
            it.startsWith(File.separator) || it.toUri().scheme == "file"
        }
        val absoluteFiles = absolutePaths.mapNotNull {
            runCatching {
                if (it.startsWith(File.separator)) File(it) else File(checkNotNull(it.toUri().path))
            }.getOrNull()
        }
            .filter { it.isFile && it.canRead() }

        if (absoluteFiles.isNotEmpty() || relativePaths.isEmpty()) return absoluteFiles

        val roots = listOfNotNull(
            resolveBasePath(),
            externalRootPath,
            internalRootPath
        ).distinctBy { it.absolutePath }

        return roots.firstNotNullOfOrNull { root ->
            relativePaths.mapNotNull { root.find(it) }
                .filter { it.isFile && it.canRead() }
                .takeIf { it.isNotEmpty() }
        } ?: run {
            Logger.warn { "no layer '${layerSettings.label}' found from storage" }
            emptyList()
        }
    }

    private fun resolveBasePath(): File? {
        if (baseTilesPath.isNullOrBlank()) return null
        return if (baseTilesPath.startsWith(File.separator)) File(baseTilesPath).takeIf { it.isDirectory && it.canRead() }
        else externalRootPath.find(baseTilesPath)
            ?.takeIf { it.isDirectory && it.canRead() } ?: internalRootPath.find(baseTilesPath)
            ?.takeIf { it.isDirectory && it.canRead() }
    }
}
