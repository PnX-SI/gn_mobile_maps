package fr.geonature.maps.layer.data

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.settings.LayerPropertiesSettings
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
    context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ILayerLocalDataSource {

    private val internalRootPath = getInternalStorage(context).mountPath
    private val externalRootPath = getExternalStorageDirectory(context)
    private var basePath: File? = null

    override suspend fun resolvesLocalLayerFromLayerSettings(
        layerSettings: LayerSettings,
        basePath: String?
    ): LayerSettings {
        if (layerSettings.isOnline()) throw LayerException.InvalidFileLayerException(layerSettings.source)

        return when (layerSettings.getType()) {
            LayerType.TILES, LayerType.VECTOR -> {
                resolveLayerSettingsPath(
                    layerSettings,
                    basePath
                ).takeIf { it.isNotEmpty() }
                    ?.let {
                        LayerSettings.Builder()
                            .from(layerSettings)
                            .sources(it.map { file ->
                                file.toUri()
                                    .toString()
                            })
                            .properties(
                                LayerPropertiesSettings.Builder.newInstance()
                                    .from(layerSettings.properties)
                                    .active(true)
                                    .build()
                            )
                            .build()
                    } ?: throw LayerException.NotFoundException(layerSettings.source)
            }

            else -> {
                throw LayerException.NotSupportedException(layerSettings.source)
            }
        }
    }

    override suspend fun buildLocalLayerFromUri(uri: Uri): LayerSettings {
        if (uri.scheme.isNullOrBlank()) throw LayerException.InvalidFileLayerException(listOf(uri.toString()))
        if (listOf(
                "content",
                "file"
            ).none { uri.scheme?.startsWith(it) == true }
        ) throw LayerException.InvalidFileLayerException(listOf(uri.toString()))

        if (uri.scheme?.startsWith("file") == true) {
            return runCatching { uri.toFile() }.getOrNull()
                ?.takeIf { it.exists() && it.canRead() }
                ?.let {
                    LayerSettings.Builder()
                        .label(
                            it.name.substringBeforeLast(".")
                                .replace(
                                    "_",
                                    " "
                                )
                                .replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else it.name },
                        )
                        .sources(
                            listOf(
                                it.toUri()
                                    .toString()
                            )
                        )
                        .properties(
                            LayerPropertiesSettings.Builder.newInstance()
                                .active(true)
                                .build()
                        )
                        .build()
                }
                ?.also {
                    if (it.getType() == LayerType.NOT_IMPLEMENTED) {
                        throw LayerException.NotSupportedException(listOf(uri.toString()))
                    }
                } ?: throw LayerException.NotFoundException(listOf(uri.toString()))
        }

        return uri.lastPathSegment?.let {
            val files = resolvePaths(
                if (it.startsWith(externalRootPath.name)) externalRootPath else internalRootPath,
                listOf(it.substringAfterLast(":"))
            )

            runCatching {
                LayerSettings.Builder()
                    .label(
                        it.substringAfterLast("/")
                            .substringBeforeLast(".")
                            .replace(
                                "_",
                                " "
                            )
                            .replaceFirstChar { c -> if (c.isLowerCase()) c.titlecase(Locale.getDefault()) else it },
                    )
                    .sources(files.map { file ->
                        file.toUri()
                            .toString()
                    })
                    .build()
            }.getOrNull()
                ?.also { layer ->
                    if (layer.getType() == LayerType.NOT_IMPLEMENTED) {
                        throw LayerException.NotSupportedException(listOf(uri.toString()))
                    }
                }
        } ?: throw LayerException.InvalidFileLayerException(listOf(uri.toString()))
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