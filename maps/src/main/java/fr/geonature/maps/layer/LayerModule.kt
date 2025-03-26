package fr.geonature.maps.layer

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.maps.jts.geojson.repository.FeatureRepositoryImpl
import fr.geonature.maps.jts.geojson.repository.IFeatureRepository
import fr.geonature.maps.layer.data.ILayerLocalDataSource
import fr.geonature.maps.layer.data.ISelectedLayersLocalDataSource
import fr.geonature.maps.layer.data.InMemorySelectedLayersLocalDataSourceImpl
import fr.geonature.maps.layer.data.LayerLocalDataSourceImpl
import fr.geonature.maps.layer.repository.ILayerRepository
import fr.geonature.maps.layer.repository.LayerRepositoryImpl
import javax.inject.Singleton

/**
 * Layer module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object LayerModule {

    @Singleton
    @Provides
    fun provideLayerLocalDataSource(@ApplicationContext appContext: Context): ILayerLocalDataSource {
        return LayerLocalDataSourceImpl(appContext)
    }

    @Singleton
    @Provides
    fun provideSelectedLayersLocalDataSource(): ISelectedLayersLocalDataSource {
        return InMemorySelectedLayersLocalDataSourceImpl()
    }

    @Singleton
    @Provides
    fun provideFeatureRepository(): IFeatureRepository {
        return FeatureRepositoryImpl()
    }

    @Singleton
    @Provides
    fun provideLayerSettingsRepository(
        localLayerDataSource: ILayerLocalDataSource,
        selectedLayersLocalDataSource: ISelectedLayersLocalDataSource
    ): ILayerRepository {
        return LayerRepositoryImpl(
            localLayerDataSource,
            selectedLayersLocalDataSource
        )
    }
}