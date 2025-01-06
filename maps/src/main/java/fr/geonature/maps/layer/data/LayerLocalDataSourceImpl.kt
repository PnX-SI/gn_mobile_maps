package fr.geonature.maps.layer.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import fr.geonature.maps.R
import fr.geonature.maps.layer.domain.LayerState
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.LayerType
import fr.geonature.mountpoint.util.FileUtils.getExternalStorageDirectory
import fr.geonature.mountpoint.util.MountPointUtils.getInternalStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tinylog.Logger
import java.io.File
import java.util.Locale

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
        return uri.lastPathSegment?.let {
            val files = resolvePaths(
                if (it.startsWith(externalRootPath.name)) externalRootPath else internalRootPath,
                listOf(it.substringAfterLast(":"))
            )

            runCatching {
                LayerState.Layer(
                    settings = LayerSettings.Builder()
                        .label(
                            it.substringAfterLast("/")
                                .substringBeforeLast(".")
                                .replace(
                                    "_",
                                    " "
                                )
                                .replaceFirstChar { c -> c.titlecase(Locale.getDefault()) },
                        )
                        .sources(files.map { file ->
                            file.toUri()
                                .toString()
                        })
                        .build(),
                    source = files.map { file -> file.toUri() },
                )
            }.getOrNull()
                ?.let { layer ->
                    if (layer.settings.getType() == LayerType.NOT_IMPLEMENTED) {
                        LayerState.Error(LayerException.NotSupportedException(defaultLayerSettings))
                    } else layer
                }
        } ?: LayerState.Error(LayerException.InvalidFileLayerException(defaultLayerSettings))
    }

    private suspend fun resolveBasePath(basePath: String? = null): File = withContext(dispatcher) {
        this@LayerLocalDataSourceImpl.basePath ?: (basePath?.split(File.separator)
            ?.filter { it.isNotBlank() } ?: emptyList()).let { segments ->
            if (segments.isEmpty()) {
                externalRootPath.let {
                    if (it.exists() && it.canRead()) it
                    else internalRootPath
                }
            }
            // absolute path
            else if (basePath?.startsWith(File.separator) == true) {
                File(
                    segments.joinToString(
                        separator = File.separator,
                        prefix = File.separator
                    )
                )
            }
            // relative path
            else {
                // first: try to find tiles relative path from external storage
                (externalRootPath.walkTopDown()
                    .filter { it.isDirectory }
                    .filter { segments.first() == it.name }
                    .find {
                        if (segments.size > 1) it.resolve(
                            segments.drop(1)
                                .joinToString(File.separator)
                        )
                            .let { relativeFile ->
                                relativeFile.exists() && relativeFile.canRead()
                            } else segments.first() == it.name
                    }
                // if not found, try to find tiles relative path from internal storage
                    ?: internalRootPath.walkTopDown()
                        .filter { it.isDirectory }
                        .filter { segments.first() == it.name }
                        .find {
                            if (segments.size > 1) it.resolve(
                                segments.drop(1)
                                    .joinToString(File.separator)
                            )
                                .let { relativeFile ->
                                    relativeFile.exists() && relativeFile.canRead()
                                } else segments.first() == it.name
                        })?.let {
                    if (segments.size > 1) it.resolve(
                        segments.drop(1)
                            .joinToString(File.separator)
                    ) else it
                }
                // use external storage as fallback
                    ?: externalRootPath
            }
        }
            .also {
                this@LayerLocalDataSourceImpl.basePath = it

                Logger.info {
                    "base path: '$it'"
                }
            }
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
            val asUri = Uri.parse(it)

            if (asUri.isAbsolute) {
                return@mapNotNull runCatching { asUri.toFile() }.onFailure {
                    Logger.warn { "invalid source path: '$it'" }
                }
                    .getOrNull()
                    ?.takeIf { file -> file.exists() && file.canRead() }
            } else null
        } +
            // then any relative paths
            layerSettings.source.filter { Uri.parse(it).isRelative }
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

    private suspend fun resolvePaths(
        rootPath: File,
        paths: List<String>
    ): List<File> = withContext(dispatcher) {
        if (paths.isEmpty()) return@withContext emptyList()

        rootPath.walkTopDown()
            .filter { f ->
                f.isFile && f.canRead() && paths.any { f.absolutePath.endsWith(it) }
            }
            .toList()
    }
}