package fr.geonature.maps.layer.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.net.toFile
import androidx.core.net.toUri
import fr.geonature.maps.R
import fr.geonature.maps.layer.domain.LayerState
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.settings.LayerPropertiesSettings
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.LayerStyleSettings
import fr.geonature.maps.settings.LayerType
import fr.geonature.maps.util.ThemeUtils.getAccentColor
import fr.geonature.mountpoint.util.FileUtils.getExternalStorageDirectory
import fr.geonature.mountpoint.util.MountPointUtils.getInternalStorage
import fr.geonature.mountpoint.util.find
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tinylog.Logger
import java.io.File
import java.util.Date
import java.util.Locale
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Local layer factory to get the corresponding [File] from given [LayerSettings] with local source.
 *
 * @author S. Grimault
 */
class LayerLocalDataSourceImpl(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ILayerLocalDataSource {

    private val internalRootPath = getInternalStorage(context).mountPath
    private val externalRootPath = getExternalStorageDirectory(context)
    private var basePath: File? = null

    override suspend fun resolvesLocalLayerFromLayerSettings(
        layerSettings: LayerSettings,
        basePath: String?
    ): List<Uri> {
        if (layerSettings.isOnline()) throw LayerException.InvalidFileLayerException(layerSettings)

        return when (layerSettings.getType()) {
            LayerType.TILES, LayerType.VECTOR -> {
                resolveLayerSettingsPath(
                    layerSettings,
                    basePath
                ).takeIf { it.isNotEmpty() }
                    ?.let {
                        it.map { file -> file.toUri() }
                    } ?: throw LayerException.NotFoundException(layerSettings)
            }

            else -> throw LayerException.NotSupportedException(layerSettings)
        }
    }

    override suspend fun buildLocalLayerFromUri(uri: Uri): LayerState {
        val startTime = Date()
        val defaultLayerSettings = LayerSettings(
            label = uri.lastPathSegment?.substringAfterLast("/")
                ?.substringBeforeLast(".")
                ?.replace(
                    "_",
                    " "
                )
                ?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
                ?: context.getString(R.string.layer_label_undefined),
            source = listOf(uri.toString()),
        )

        if (uri.scheme.isNullOrBlank()) return LayerState.Error(LayerException.InvalidFileLayerException(defaultLayerSettings))

        if (listOf(
                "content",
                "file"
            ).none { uri.scheme?.startsWith(it) == true }
        ) return LayerState.Error(LayerException.InvalidFileLayerException(defaultLayerSettings))

        // from file://
        if (uri.scheme?.startsWith("file") == true) {
            return runCatching { uri.toFile() }.getOrNull()
                ?.takeIf { it.exists() && it.canRead() }
                ?.let {
                    LayerState.Layer(
                        settings = LayerSettings.Builder()
                            .label(
                                it.name.substringAfterLast("/")
                                    .substringBeforeLast(".")
                                    .replace(
                                        "_",
                                        " "
                                    )
                                    .replaceFirstChar { c -> c.titlecase(Locale.getDefault()) },
                            )
                            .sources(
                                listOf(
                                    it.toUri()
                                        .toString()
                                )
                            )
                            .build(),
                        source = listOf(it.toUri())
                    )
                }
                ?.let {
                    if (it.settings.getType() == LayerType.NOT_IMPLEMENTED) {
                        LayerState.Error(LayerException.NotSupportedException(defaultLayerSettings))
                    } else it
                } ?: LayerState.Error(LayerException.NotFoundException(defaultLayerSettings))
        }

        // from content://
        return uri.lastPathSegment?.let { lastPathSegment ->
            Logger.debug { "resolving relative path '$lastPathSegment' from root path '${if (lastPathSegment.startsWith(externalRootPath.name)) externalRootPath else internalRootPath}'..." }

            val files = if (lastPathSegment.startsWith("msf:")) {
                val displayName = context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst())
                        cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                    else null
                }

                resolvePaths(externalRootPath, listOfNotNull(displayName)).takeIf { it.isNotEmpty() } ?:
                resolvePaths(internalRootPath, listOfNotNull(displayName))
            } else {
                listOfNotNull(
                    // tries to resolve URI as absolute path...
                    File(
                        (if (lastPathSegment.startsWith(externalRootPath.name)) externalRootPath else internalRootPath),
                        lastPathSegment.substringAfterLast(":")
                    ).takeIf { f -> f.isFile && f.canRead() }).takeIf { f -> f.isNotEmpty() }
                // if not found, tries to resolve as relative path
                    ?: resolvePaths(
                        if (lastPathSegment.startsWith(externalRootPath.name)) externalRootPath else internalRootPath,
                        listOf(lastPathSegment.substringAfterLast(":"))
                    )
            }

            val layerName = runCatching { files.single() }.map { it.nameWithoutExtension }
                .getOrElse {
                    lastPathSegment.substringAfterLast("/")
                        .substringBeforeLast(".")
                }
                .replace(
                    "_",
                    " "
                )
                .replaceFirstChar { c -> c.titlecase(Locale.getDefault()) }

            files.takeIf { it.size == 1 }?.first()
            runCatching {
                LayerState.Layer(
                    settings = LayerSettings.Builder()
                        .label(layerName)
                        .sources(files.map { file ->
                            file.toUri()
                                .toString()
                        })
                        .properties(
                            LayerPropertiesSettings.Builder.newInstance()
                                .style(
                                    LayerStyleSettings.Builder.newInstance()
                                        .color(getAccentColor(context))
                                        .build()
                                )
                                .build()
                        )
                        .build(),
                    source = files.map { file -> file.toUri() },
                )
            }.getOrNull()
                ?.let { layer ->
                    if (layer.settings.getType() == LayerType.NOT_IMPLEMENTED) {
                        LayerState.Error(LayerException.NotSupportedException(defaultLayerSettings))
                    } else layer.also {
                        Logger.info {
                            "resolve local layer '${it.settings.label}' from URI '$uri' as file '${it.source}' took ${
                                (Date().time - startTime.time).toDuration(
                                    DurationUnit.MILLISECONDS
                                )
                            }"
                        }
                    }
                }
        } ?: LayerState.Error(LayerException.InvalidFileLayerException(defaultLayerSettings))
    }

    private suspend fun resolveLayerSettingsPath(
        layerSettings: LayerSettings,
        basePath: String? = null
    ): List<File> = withContext(dispatcher) {
        if (layerSettings.isOnline()) {
            return@withContext emptyList()
        }

        val rootPath = resolveBasePath(basePath)

        // tries to resolve first any absolute paths
        layerSettings.source.mapNotNull {
            val asUri = it.toUri()

            if (asUri.isAbsolute) {
                return@mapNotNull runCatching { asUri.toFile() }.onFailure {
                    Logger.warn { "invalid source path: '$it'" }
                }
                    .getOrNull()
                    ?.takeIf { file -> file.exists() && file.canRead() }
            } else null
        } +
            // then any relative paths
            layerSettings.source.filter { it.toUri().isRelative }
                .let { paths ->
                    resolvePaths(
                        rootPath,
                        paths
                    ).takeIf { files -> files.isNotEmpty() } ?: externalRootPath.also {
                        Logger.warn {
                            "no layer '${layerSettings.label}' found from root path: '$rootPath', try to perform a deep scan from ${
                                if (it.absolutePath == internalRootPath.absolutePath) "internal"
                                else "external"
                            } storage '${it}'..."
                        }
                    }
                        .let {
                            resolvePaths(
                                it,
                                paths
                            )
                        }
                        // no layer found from external storage, tries to resolve paths from internal storage
                        .takeIf { it.isNotEmpty() } ?: internalRootPath.let {
                        if (it.absolutePath == externalRootPath.absolutePath) null else it
                    }
                        ?.also {
                            Logger.warn {
                                "no layer '${layerSettings.label}' found from external storage: '${externalRootPath.absolutePath}', try to perform a deep scan from internal storage '${it}'..."
                            }
                        }
                        ?.let {
                            resolvePaths(
                                it,
                                layerSettings.source
                            )
                        } ?: run {
                        Logger.warn {
                            "no layer '${layerSettings.label}' found from storage"
                        }
                        emptyList()
                    }
                }
    }

    private suspend fun resolveBasePath(basePath: String? = null): File = withContext(dispatcher) {
        this@LayerLocalDataSourceImpl.basePath ?: when {
            // undefined: use external root path or internal root path as fallback
            basePath.isNullOrBlank() -> externalRootPath.takeIf { it.canRead() } ?: internalRootPath
            // absolute path, if not found use external root path or internal root path as fallback
            basePath.startsWith(File.separator) -> File(basePath).takeIf { it.isDirectory && it.canRead() }
                ?: externalRootPath.takeIf { it.canRead() } ?: internalRootPath
            // relative path from external root path or from internal root path as fallback
            else -> externalRootPath.find(basePath)
                ?.takeIf { it.isDirectory && it.canRead() } ?: internalRootPath.find(basePath)
                ?.takeIf { it.isDirectory && it.canRead() }
            ?: externalRootPath.takeIf { it.canRead() } ?: internalRootPath
        }.also {
            this@LayerLocalDataSourceImpl.basePath = it

            Logger.info {
                "base path: '$it'"
            }
        }
    }

    /**
     * Performs a deep scan to find files matching given paths.
     */
    private suspend fun resolvePaths(
        rootPath: File,
        paths: List<String>
    ): List<File> = withContext(dispatcher) {
        if (paths.isEmpty()) return@withContext emptyList()

        paths.mapNotNull { rootPath.find(it) }
            .filter { it.isFile && it.canRead() }
    }
}