package fr.geonature.maps.maplibre.ui.widget

import android.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [MyLocationButton].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class MyLocationButtonTest {

    private val button = MyLocationButton(
        ContextThemeWrapper(
            ApplicationProvider.getApplicationContext(),
            com.google.android.material.R.style.Theme_MaterialComponents_Light
        )
    )

    @Test
    fun `should be disabled until the location permission is granted`() {
        assertFalse(button.isEnabled)
        assertEquals(
            MyLocationButton.MyLocationState.DISABLED,
            button.myLocationState
        )
    }

    @Test
    fun `should stay disabled when the location permission is denied`() {
        button.onPermissionResult(false)

        assertFalse(button.isEnabled)
        assertEquals(
            MyLocationButton.MyLocationState.DISABLED,
            button.myLocationState
        )
    }
}
