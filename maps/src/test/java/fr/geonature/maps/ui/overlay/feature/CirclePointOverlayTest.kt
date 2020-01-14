package fr.geonature.maps.ui.overlay.feature

import fr.geonature.maps.jts.geojson.GeometryUtils.fromPoint
import fr.geonature.maps.jts.geojson.JTSTestHelper
import fr.geonature.maps.settings.LayerStyleSettings
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.GeometryFactory
import org.osmdroid.util.BoundingBox
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [CirclePointOverlay].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class CirclePointOverlayTest {

    private lateinit var gf: GeometryFactory

    @Before
    fun setUp() {
        gf = GeometryFactory()
    }

    @Test
    fun testCreateOverlayFromPoint() {
        // given Point
        val point = JTSTestHelper.createPoint(
            gf,
            47.2256258,
            -1.5545135
        )

        // when create Overlay from Point
        val circlePointOverlay = CirclePointOverlay().apply { setGeometry(point) }

        // then
        assertEquals(
            fromPoint(point).longitude,
            BoundingBox.fromGeoPoints(circlePointOverlay.backendOverlay.points).centerLongitude,
            0.00001
        )
        assertEquals(
            fromPoint(point).latitude,
            BoundingBox.fromGeoPoints(circlePointOverlay.backendOverlay.points).centerLatitude,
            0.00001
        )
        assertEquals(
            LayerStyleSettings().color,
            circlePointOverlay.backendOverlay.outlinePaint.color
        )
        assertEquals(
            LayerStyleSettings().weight.toFloat(),
            circlePointOverlay.backendOverlay.outlinePaint.strokeWidth
        )
        assertEquals(
            LayerStyleSettings().fillColor,
            circlePointOverlay.backendOverlay.fillPaint.color
        )
    }

    @Test
    fun testCreateOverlayFromPointWithStyle() {
        // given Point
        val point = JTSTestHelper.createPoint(
            gf,
            47.2256258,
            -1.5545135
        )

        // when create Overlay from Point
        val style = LayerStyleSettings.Builder.newInstance()
            .stroke(true)
            .color("#FF0000")
            .weight(10)
            .opacity(0.9f)
            .fill(true)
            .fillColor("#0000FF")
            .fillOpacity(0.25f)
            .build()
        val overlayAsCirclePolygon = CirclePointOverlay().apply {
            setGeometry(
                point,
                style
            )
        }

        // then
        assertEquals(
            style.color,
            overlayAsCirclePolygon.backendOverlay.outlinePaint.color
        )
        assertEquals(
            style.weight.toFloat(),
            overlayAsCirclePolygon.backendOverlay.outlinePaint.strokeWidth
        )
        assertEquals(
            style.fillColor,
            overlayAsCirclePolygon.backendOverlay.fillPaint.color
        )
    }
}
