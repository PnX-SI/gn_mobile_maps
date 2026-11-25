package fr.geonature.maps.maplibre.ui.widget

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.google.android.material.snackbar.Snackbar
import fr.geonature.maps.maplibre.R
import fr.geonature.maps.maplibre.util.ThemeUtils

/**
 * Button showing the attribution notice of the current base layer in a [Snackbar] (HTML links are
 * supported). The button is hidden as long as there is no attribution to display.
 *
 * @author S. Grimault
 */
internal class AttributionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatImageButton(
    context,
    attrs
) {
    var attribution: String? = null
        set(value) {
            field = value?.takeIf { it.isNotBlank() }
            visibility = if (field == null) GONE else VISIBLE
        }

    private var snackbar: Snackbar? = null

    init {
        setImageResource(R.drawable.ic_attribution)
        background = null
        contentDescription = context.getString(R.string.button_attribution_hint)
        visibility = GONE

        setOnClickListener { showAttribution() }
    }

    override fun onDetachedFromWindow() {
        snackbar?.dismiss()
        snackbar = null
        super.onDetachedFromWindow()
    }

    private fun showAttribution() {
        val text = attribution ?: return

        snackbar?.dismiss()
        snackbar = Snackbar.make(
            this,
            HtmlCompat.fromHtml(
                text,
                HtmlCompat.FROM_HTML_MODE_COMPACT
            ),
            Snackbar.LENGTH_LONG
        )
            .setAction(R.string.attribution_ok) { }
            .setBackgroundTint(ThemeUtils.getSurfaceColor(context))
            .setTextColor(ThemeUtils.getOnSurfaceColor(context))
            .setActionTextColor(ThemeUtils.getAccentColor(context))
            .also { snackbar ->
                snackbar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    ?.apply {
                        maxLines = Int.MAX_VALUE
                        movementMethod = LinkMovementMethod.getInstance()
                    }
                snackbar.show()
            }
    }
}
