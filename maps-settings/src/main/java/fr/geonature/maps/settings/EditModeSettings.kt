package fr.geonature.maps.settings

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Settings about the edit mode of the map.
 *
 * When [MapSettings.edit] is `null`, the edit feature is disabled.
 *
 * @author S. Grimault
 */
@Parcelize
data class EditModeSettings(

    /**
     * Whether to show the edit marker guide (default: `true`).
     */
    val showEditMarkerGuide: Boolean = Builder.newInstance().showEditMarkerGuide,

    /**
     * The minimum zoom level required to enable the edit mode (default: `0.0`).
     */
    val minZoomEditing: Double = Builder.newInstance().minZoomEditing
) : Parcelable {

    private constructor(builder: Builder) : this(
        builder.showEditMarkerGuide,
        builder.minZoomEditing
    )

    fun builder(): Builder = Builder.newInstance()
        .from(this)

    class Builder {

        var showEditMarkerGuide: Boolean = true
            private set

        var minZoomEditing: Double = 0.0
            private set

        /**
         * Makes a copy of given [EditModeSettings].
         */
        fun from(editModeSettings: EditModeSettings?) = apply {
            if (editModeSettings == null) return@apply

            this.showEditMarkerGuide = editModeSettings.showEditMarkerGuide
            this.minZoomEditing = editModeSettings.minZoomEditing
        }

        fun showEditMarkerGuide(showEditMarkerGuide: Boolean) =
            apply { this.showEditMarkerGuide = showEditMarkerGuide }

        fun minZoomEditing(minZoomEditing: Double) = apply { this.minZoomEditing = minZoomEditing }

        fun build() = EditModeSettings(this)

        companion object {
            fun newInstance(): Builder = Builder()
        }
    }
}

