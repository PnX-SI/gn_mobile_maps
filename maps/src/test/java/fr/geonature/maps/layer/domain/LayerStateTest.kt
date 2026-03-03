package fr.geonature.maps.layer.domain

import android.net.Uri
import android.os.Parcel
import fr.geonature.maps.layer.error.LayerException
import fr.geonature.maps.settings.LayerPropertiesSettings
import fr.geonature.maps.settings.LayerSettings
import fr.geonature.maps.settings.LayerStyleSettings
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertEquals
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

    @Test
    fun `should obtain LayerState Layer instance from parcelable`() {
        // given some LayerState of type Layer
        val layerStateLayer = LayerState.Layer(
            LayerSettings.Builder.newInstance()
                .label("Nantes")
                .addSource("nantes.mbtiles")
                .build(),
            listOf(Uri.parse("nantes.mbtiles"))
        )

        // when we obtain a Parcel object to write the LayerState instance to it
        val parcel = Parcel.obtain()
        layerStateLayer.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            layerStateLayer,
            parcelableCreator<LayerState.Layer>().createFromParcel(parcel)
        )
    }

    @Test
    fun `should obtain LayerState SelectedLayer instance from parcelable`() {
        // given some LayerState of type SelectedLayer
        val layerStateSelectedLayer = LayerState.SelectedLayer(
            LayerSettings.Builder.newInstance()
                .label("Nantes")
                .addSource("nantes.mbtiles")
                .build(),
            listOf(Uri.parse("nantes.mbtiles"))
        )

        // when we obtain a Parcel object to write the LayerState instance to it
        val parcel = Parcel.obtain()
        layerStateSelectedLayer.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            layerStateSelectedLayer,
            parcelableCreator<LayerState.SelectedLayer>().createFromParcel(parcel)
        )
    }

    @Test
    fun `should obtain LayerState Error instance from parcelable`() {
        // given some LayerState of type Error
        val layerStateError = LayerState.Error(
            LayerException.NotFoundException(
                LayerSettings.Builder.newInstance()
                    .label("Nantes")
                    .addSource("nantes.mbtiles")
                    .properties(
                        LayerPropertiesSettings.Builder.newInstance()
                            .style(
                                LayerStyleSettings.Builder.newInstance()
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
        )

        // when we obtain a Parcel object to write the LayerState instance to it
        val parcel = Parcel.obtain()
        layerStateError.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            layerStateError,
            parcelableCreator<LayerState.Error>().createFromParcel(parcel)
        )
    }
}