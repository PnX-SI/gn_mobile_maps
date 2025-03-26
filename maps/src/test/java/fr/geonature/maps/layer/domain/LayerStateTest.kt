package fr.geonature.maps.layer.domain

import android.net.Uri
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.settings.LayerSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [LayerState].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class LayerStateTest {

    @Test
    fun `should determine if two LayerState considered as the same`() {
        assertTrue(
            LayerState.Layer(
                LayerSettings.Builder.newInstance()
                    .label("Nantes")
                    .addSource("nantes.mbtiles")
                    .build(),
                listOf(Uri.parse("nantes.mbtiles"))
            )
                .isSame(
                    LayerState.Layer(
                        LayerSettings.Builder.newInstance()
                            .label("Nantes")
                            .addSource("nantes.mbtiles")
                            .build(),
                        listOf(Uri.parse("nantes.wkt"))
                    )
                )
        )
        assertTrue(
            LayerState.Layer(
                LayerSettings.Builder.newInstance()
                    .label("Nantes")
                    .addSource("nantes.mbtiles")
                    .build(),
                listOf(Uri.parse("nantes.mbtiles"))
            )
                .isSame(
                    LayerState.SelectedLayer(
                        LayerSettings.Builder.newInstance()
                            .label("Nantes")
                            .addSource("nantes.mbtiles")
                            .build(),
                        listOf(Uri.parse("nantes.wkt"))
                    )
                )
        )
        assertTrue(
            LayerState.Layer(
                LayerSettings.Builder.newInstance()
                    .label("Nantes")
                    .addSource("nantes.mbtiles")
                    .build(),
                listOf(Uri.parse("nantes.mbtiles"))
            )
                .isSame(
                    LayerState.Error(
                        LayerException.NotFoundException(
                            LayerSettings.Builder.newInstance()
                                .label("Nantes")
                                .addSource("nantes.mbtiles")
                                .build()
                        )
                    )
                )
        )

        assertFalse(
            LayerState.Layer(
                LayerSettings.Builder.newInstance()
                    .label("Nantes")
                    .addSource("nantes.mbtiles")
                    .build(),
                listOf(Uri.parse("nantes.mbtiles"))
            )
                .isSame(
                    LayerState.Layer(
                        LayerSettings.Builder.newInstance()
                            .label("Nantes")
                            .addSource("nantes.wkt")
                            .build(),
                        listOf(Uri.parse("nantes.wkt"))
                    )
                )
        )
    }
}