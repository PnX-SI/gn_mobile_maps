package fr.geonature.maps.ui.overlay

import fr.geonature.maps.jts.geojson.JTSTestHelper.createPoint
import fr.geonature.maps.ui.overlay.OverlayUtils.calculateBounds
import fr.geonature.maps.ui.overlay.feature.CirclePointOverlay
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.locationtech.jts.geom.GeometryFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.overlay.FolderOverlay
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [CirclePointOverlay].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
internal class OverlayUtilsTest {

    private lateinit var gf: GeometryFactory

    @Before
    fun setUp() {
        gf = GeometryFactory()
    }

    @Test
    fun `should get bounds from single point`() {
        // given a point as circle
        val circlePointOverlay = CirclePointOverlay().apply {
            setGeometry(
                createPoint(
                    gf,
                    47.22614760176713,
                    -1.5544867433651681
                )
            )
        }

        // then
        assertTrue(
            BoundingBox(
                47.22623743329553,
                -1.5543544643436566,
                47.226057770238704,
                -1.5546190223866798
            ).isSame(calculateBounds(circlePointOverlay))
        )
    }

    @Test
    fun `should get bounds from group of overlays`() {
        // given a group of overlays
        val folderOverlay = FolderOverlay().apply {
            add(
                CirclePointOverlay().apply {
                    setGeometry(
                        createPoint(
                            gf,
                            47.22614760176713,
                            -1.5544867433651681
                        )
                    )
                },
            )
            add(
                CirclePointOverlay().apply {
                    setGeometry(
                        createPoint(
                            gf,
                            47.225817270242516,
                            -1.5545477893197608
                        )
                    )
                },
            )
        }

        // then
        assertTrue(
            BoundingBox(
                47.22623743329553,
                -1.5543544643436566,
                47.22572743871409,
                -1.5546800675169516
            ).isSame(calculateBounds(folderOverlay))
        )
    }
}