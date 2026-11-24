package fr.geonature.maps.maplibre.ui.widget

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests about [ScaleBarView].
 *
 * @author S. Grimault
 */
class ScaleBarViewTest {

    @Test
    fun `should round distances down to 1, 2 or 5 times a power of ten`() {
        assertEquals(
            100.0,
            ScaleBarView.niceDistance(130.0),
            0.0
        )
        assertEquals(
            200.0,
            ScaleBarView.niceDistance(290.0),
            0.0
        )
        assertEquals(
            500.0,
            ScaleBarView.niceDistance(999.0),
            0.0
        )
        assertEquals(
            5000.0,
            ScaleBarView.niceDistance(5000.0),
            0.0
        )
        assertEquals(
            1.0,
            ScaleBarView.niceDistance(1.5),
            0.0
        )
    }
}

