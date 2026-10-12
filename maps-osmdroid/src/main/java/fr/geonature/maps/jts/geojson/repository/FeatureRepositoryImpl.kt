package fr.geonature.maps.jts.geojson.repository

import fr.geonature.maps.jts.geojson.Feature
import fr.geonature.maps.jts.geojson.error.FeatureException
import fr.geonature.maps.jts.geojson.io.GeoJsonReader
import fr.geonature.maps.jts.geojson.io.WKTReader
import fr.geonature.maps.layer.repository.ILayerRepository
import org.tinylog.Logger
import java.io.File
import java.io.FileReader

/**
 * Default implementation of [ILayerRepository].
 *
 * @author S. Grimault
 */
class FeatureRepositoryImpl : IFeatureRepository {

    override fun loadFeatures(vararg file: File): Result<List<Feature>> {
        val result = file.map { f ->
            if (!f.canRead()) return Result.failure(FeatureException.NotFoundException(f))

            when (f.extension) {
                "geojson", "json" -> runCatching {
                    GeoJsonReader().read(FileReader(f))
                }.recoverCatching {
                    Logger.warn { "failed to load vector layer from file '${f.absolutePath}'" }

                    throw FeatureException.ParseException(
                        f,
                        it
                    )
                }

                "wkt" -> runCatching {
                    WKTReader().readFeatures(FileReader(f))
                }.recoverCatching {
                    Logger.warn { "failed to load vector layer from file '${f.absolutePath}'" }

                    throw FeatureException.ParseException(
                        f,
                        it
                    )
                }

                else -> {
                    Logger.warn { "unsupported file as vector layer '${f.absolutePath}'" }

                    Result.failure(FeatureException.NotSupportedException(f))
                }
            }.mapCatching {
                if (it.isEmpty()) throw FeatureException.NoFeatureFoundException(f)
                it
            }
        }

        if (result.all { it.isFailure }) return result.first { it.isFailure }

        return Result.success(result.mapNotNull { it.getOrNull() }
            .flatten())
    }
}