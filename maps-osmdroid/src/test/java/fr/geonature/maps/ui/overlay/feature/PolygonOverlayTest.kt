package fr.geonature.maps.ui.overlay.feature

import fr.geonature.maps.jts.geojson.JTSTestHelper.createCoordinate
import fr.geonature.maps.jts.geojson.JTSTestHelper.createLinearRing
import fr.geonature.maps.jts.geojson.JTSTestHelper.createPolygon
import fr.geonature.maps.settings.LayerStyleSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.GeometryFactory
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [PolygonOverlay].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class PolygonOverlayTest {

    private lateinit var gf: GeometryFactory

    @Before
    fun setUp() {
        gf = GeometryFactory()
    }

    @Test
    fun testCreateOverlayFromSimplePolygon() {
        // given Polygon
        val polygon = createPolygon(
            gf,
            createCoordinate(
                47.226219,
                -1.554430
            ),
            createCoordinate(
                47.226237,
                -1.554261
            ),
            createCoordinate(
                47.226122,
                -1.554245
            ),
            createCoordinate(
                47.226106,
                -1.554411
            ),
            createCoordinate(
                47.226219,
                -1.554430
            )
        )

        // when create Overlay from Polygon
        val polygonOverlay = PolygonOverlay().apply { setGeometry(polygon) }

        // then
        assertEquals(
            5,
            polygonOverlay.backendOverlay.actualPoints.size
        )
        assertTrue(polygonOverlay.backendOverlay.holes.isEmpty())
        assertEquals(
            LayerStyleSettings().color,
            polygonOverlay.backendOverlay.outlinePaint.color
        )
        assertEquals(
            LayerStyleSettings().weight.toFloat(),
            polygonOverlay.backendOverlay.outlinePaint.strokeWidth
        )
        assertEquals(
            LayerStyleSettings().fillColor,
            polygonOverlay.backendOverlay.fillPaint.color
        )
    }

    @Test
    fun testCreateOverlayFromPolygonWithHoles() {
        // given Polygon with holes
        val polygonWithHoles = createPolygon(
            gf,
            createLinearRing(
                gf,
                createCoordinate(
                    47.226257,
                    -1.554564
                ),
                createCoordinate(
                    47.226295,
                    -1.554202
                ),
                createCoordinate(
                    47.226075,
                    -1.554169
                ),
                createCoordinate(
                    47.226049,
                    -1.554496
                ),
                createCoordinate(
                    47.226257,
                    -1.554564
                )
            ),
            createLinearRing(
                gf,
                createCoordinate(
                    47.226219,
                    -1.554430
                ),
                createCoordinate(
                    47.226237,
                    -1.554261
                ),
                createCoordinate(
                    47.226122,
                    -1.554245
                ),
                createCoordinate(
                    47.226106,
                    -1.554411
                ),
                createCoordinate(
                    47.226219,
                    -1.554430
                )
            )
        )

        // when create Overlay from Polygon
        val polygonOverlay = PolygonOverlay().apply { setGeometry(polygonWithHoles) }

        // then
        assertEquals(
            5,
            polygonOverlay.backendOverlay.actualPoints.size
        )
        assertEquals(
            1,
            polygonOverlay.backendOverlay.holes.size
        )
        assertEquals(
            5,
            (polygonOverlay).backendOverlay.holes[0].size
        )
        assertEquals(
            LayerStyleSettings().color,
            polygonOverlay.backendOverlay.outlinePaint.color
        )
        assertEquals(
            LayerStyleSettings().weight.toFloat(),
            polygonOverlay.backendOverlay.outlinePaint.strokeWidth
        )
        assertEquals(
            LayerStyleSettings().fillColor,
            polygonOverlay.backendOverlay.fillPaint.color
        )
    }

    @Test
    fun testCreateOverlayFromPolygonWithStyle() {
        // given Polygon
        val polygon = createPolygon(
            gf,
            createCoordinate(
                47.226219,
                -1.554430
            ),
            createCoordinate(
                47.226237,
                -1.554261
            ),
            createCoordinate(
                47.226122,
                -1.554245
            ),
            createCoordinate(
                47.226106,
                -1.554411
            ),
            createCoordinate(
                47.226219,
                -1.554430
            )
        )

        // when create Overlay from Polygon
        val style = LayerStyleSettings.Builder.newInstance()
            .stroke(true)
            .color("#FF0000")
            .weight(10)
            .opacity(0.9f)
            .fill(true)
            .fillColor("#0000FF")
            .fillOpacity(0.25f)
            .build()
        val overlayAsPolygon = PolygonOverlay().apply {
            setGeometry(
                polygon,
                style
            )
        }

        // then
        assertEquals(
            style.color,
            overlayAsPolygon.backendOverlay.outlinePaint.color
        )
        assertEquals(
            style.weight.toFloat(),
            overlayAsPolygon.backendOverlay.outlinePaint.strokeWidth
        )
        assertEquals(
            style.fillColor,
            overlayAsPolygon.backendOverlay.fillPaint.color
        )
    }
}
