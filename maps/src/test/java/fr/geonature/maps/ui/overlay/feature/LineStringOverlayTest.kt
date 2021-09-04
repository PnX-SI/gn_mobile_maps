package fr.geonature.maps.ui.overlay.feature

import fr.geonature.maps.jts.geojson.JTSTestHelper.createCoordinate
import fr.geonature.maps.jts.geojson.JTSTestHelper.createLineString
import fr.geonature.maps.settings.LayerStyleSettings
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.GeometryFactory
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [LineStringOverlay].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class LineStringOverlayTest {

    private lateinit var gf: GeometryFactory

    @Before
    fun setUp() {
        gf = GeometryFactory()
    }

    @Test
    fun testCreateOverlayFromLineString() {
        // given LineString
        val lineString = createLineString(
            gf,
            createCoordinate(
                47.2256258,
                -1.5545135
            ),
            createCoordinate(
                47.225136,
                -1.553913
            )
        )

        // when create Overlay from LineString
        val lineStringOverlay = LineStringOverlay().apply { setGeometry(lineString) }

        // then
        assertEquals(
            2,
            lineStringOverlay.backendOverlay.actualPoints.size
        )
        assertEquals(
            LayerStyleSettings().color,
            lineStringOverlay.backendOverlay.outlinePaint.color
        )
        assertEquals(
            LayerStyleSettings().weight.toFloat(),
            lineStringOverlay.backendOverlay.outlinePaint.strokeWidth
        )
    }

    @Test
    fun testCreateOverlayFromLineStringWithStyle() {
        // given LineString
        val lineString = createLineString(
            gf,
            createCoordinate(
                47.2256258,
                -1.5545135
            ),
            createCoordinate(
                47.225136,
                -1.553913
            )
        )

        // when create Overlay from LineString
        val style = LayerStyleSettings.Builder.newInstance()
            .stroke(false)
            .color("#FF0000")
            .weight(10)
            .opacity(0.9f)
            .build()
        val lineStringOverlay = LineStringOverlay().apply {
            setGeometry(
                lineString,
                style
            )
        }

        // then
        assertEquals(
            style.color,
            lineStringOverlay.backendOverlay.outlinePaint.color
        )
        assertEquals(
            style.weight.toFloat(),
            lineStringOverlay.backendOverlay.outlinePaint.strokeWidth
        )
    }
}
