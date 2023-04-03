package fr.geonature.maps.settings

import android.graphics.Color
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils
import kotlinx.parcelize.Parcelize

/**
 * Layer style to apply.
 *
 * @author S. Grimault
 */
@Parcelize
data class LayerStyleSettings(
    val stroke: Boolean = Builder.newInstance().stroke,
    @ColorInt
    val color: Int = Builder.newInstance().color,
    val weight: Int = Builder.newInstance().weight,
    val fill: Boolean = Builder.newInstance().fill,
    @ColorInt
    val fillColor: Int = Builder.newInstance().fillColor
) : Parcelable {

    private constructor(builder: Builder) : this(
        builder.stroke,
        builder.color,
        builder.weight,
        builder.fill,
        builder.fillColor
    )

    class Builder {

        internal var stroke: Boolean = true
            private set

        @ColorInt
        internal var color: Int = Color.DKGRAY
            private set

        internal var weight: Int = 8
            private set

        internal var fill: Boolean = false
            private set

        @ColorInt
        internal var fillColor: Int = Color.TRANSPARENT
            private set

        @FloatRange(
            from = 0.0,
            to = 1.0
        )
        private var opacity: Float = 1f

        @FloatRange(
            from = 0.0,
            to = 1.0
        )
        private var fillOpacity: Float = 0.2f

        fun from(layerStyleSettings: LayerStyleSettings?) =
            apply {
                if (layerStyleSettings == null) return@apply

                stroke = layerStyleSettings.stroke
                color(layerStyleSettings.color)
                weight = layerStyleSettings.weight
                fill = layerStyleSettings.fill
                fillColor(layerStyleSettings.fillColor)
            }

        fun stroke(stroke: Boolean) =
            apply { this.stroke = stroke }

        fun color(
            @ColorInt
            color: Int
        ) =
            apply {
                this.color = color
                this.opacity = (Color.alpha(color)
                    .toDouble() / 255).toFloat()
            }

        fun color(colorString: String) =
            apply {
                this.color = Color.parseColor(colorString)
                var opacity = (Color.alpha(color)
                    .toDouble() / 255).toFloat()
                opacity = if (opacity == 1f) this.opacity else opacity

                opacity(opacity)
            }

        fun weight(weight: Int) =
            apply {
                if (weight > 0) {
                    this.weight = weight
                }
            }

        fun opacity(
            @FloatRange(
                from = 0.0,
                to = 1.0
            )
            opacity: Float
        ) =
            apply {
                if (opacity in 0.0..1.0) {
                    this.opacity = opacity
                    this.color = ColorUtils.setAlphaComponent(
                        this.color,
                        (opacity * 255).toInt()
                    )
                }
            }

        fun fill(fill: Boolean) =
            apply {
                this.fill = fill
            }

        fun fillColor(
            @ColorInt
            fillColor: Int
        ) =
            apply {
                this.fillColor = fillColor
                this.fillOpacity = (Color.alpha(fillColor)
                    .toDouble() / 255).toFloat()
            }

        fun fillColor(fillColorString: String) =
            apply {
                this.fillColor = Color.parseColor(fillColorString)

                var fillOpacity = (Color.alpha(fillColor)
                    .toDouble() / 255).toFloat()
                fillOpacity = if (fillOpacity == 1f) this.fillOpacity else fillOpacity

                fillOpacity(fillOpacity)
            }

        fun fillOpacity(
            @FloatRange(
                from = 0.0,
                to = 1.0
            )
            fillOpacity: Float
        ) =
            apply {
                if (fillOpacity in 0.0..1.0) {
                    this.fillOpacity = fillOpacity
                    this.fillColor = ColorUtils.setAlphaComponent(
                        this.fillColor,
                        (fillOpacity * 255).toInt()
                    )
                }
            }

        fun build(): LayerStyleSettings {
            return LayerStyleSettings(this)
        }

        companion object {
            fun newInstance(): Builder =
                Builder()
        }
    }
}
