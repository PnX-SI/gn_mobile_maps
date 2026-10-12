package fr.geonature.maps.settings

import android.os.Parcel
import kotlinx.parcelize.parcelableCreator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [EditModeSettings].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class EditModeSettingsTest {

    @Test
    fun `should have default values`() {
        val editModeSettings = EditModeSettings()
        assertTrue(editModeSettings.showEditMarkerGuide)
        assertEquals(
            0.0,
            editModeSettings.minZoomEditing,
            0.0
        )
        assertEquals(
            editModeSettings,
            EditModeSettings.Builder.newInstance()
                .build()
        )
    }

    @Test
    fun `should instantiate from builder`() {
        val editModeSettings = EditModeSettings.Builder.newInstance()
            .showEditMarkerGuide(false)
            .minZoomEditing(10.0)
            .build()
        assertEquals(
            EditModeSettings(
                showEditMarkerGuide = false,
                minZoomEditing = 10.0
            ),
            editModeSettings
        )
    }

    @Test
    fun `should instantiate a new instance from existing one`() {
        val editModeSettings = EditModeSettings(
            showEditMarkerGuide = false,
            minZoomEditing = 10.0
        )
        assertEquals(
            editModeSettings,
            editModeSettings.builder()
                .build()
        )
        assertEquals(
            EditModeSettings(
                showEditMarkerGuide = false,
                minZoomEditing = 12.0
            ),
            editModeSettings.builder()
                .minZoomEditing(12.0)
                .build()
        )
        assertFalse(
            EditModeSettings.Builder.newInstance()
                .from(editModeSettings).showEditMarkerGuide
        )
    }

    @Test
    fun `should keep default values when copying from null`() {
        assertEquals(
            EditModeSettings(),
            EditModeSettings.Builder.newInstance()
                .from(null)
                .build()
        )
    }

    @Test
    fun `should be parcelable`() {
        val editModeSettings = EditModeSettings(
            showEditMarkerGuide = false,
            minZoomEditing = 10.0
        )
        val parcel = Parcel.obtain()
        editModeSettings.writeToParcel(
            parcel,
            0
        )
        parcel.setDataPosition(0)
        assertEquals(
            editModeSettings,
            parcelableCreator<EditModeSettings>().createFromParcel(parcel)
        )
        parcel.recycle()
    }

    @Test
    fun `should have no edit mode settings in map settings by default`() {
        assertNull(
            MapSettings.Builder()
                .build().edit
        )
    }

    @Test
    fun `should keep edit mode settings in map settings`() {
        val editModeSettings = EditModeSettings(
            showEditMarkerGuide = false,
            minZoomEditing = 10.0
        )
        val mapSettings = MapSettings.Builder()
            .edit(editModeSettings)
            .build()
        assertEquals(
            editModeSettings,
            mapSettings.edit
        )
        assertEquals(
            editModeSettings,
            mapSettings.builder()
                .build().edit
        )
    }
}
