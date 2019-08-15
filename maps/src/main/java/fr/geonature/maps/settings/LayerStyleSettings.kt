package fr.geonature.maps.settings

import android.graphics.Color
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils

/**
 * Layer style to apply.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
data class LayerStyleSettings(var stroke: Boolean = true,
                              @ColorInt
                              var color: Int = Color.DKGRAY,
                              var weight: Int = 8,
                              var fill: Boolean = false,
                              @ColorInt
                              var fillColor: Int = Color.TRANSPARENT) : Parcelable {

    private constructor(builder: Builder) : this(builder.stroke,
                                                 builder.color,
                                                 builder.weight,
                                                 builder.fill,
                                                 builder.fillColor)

    private constructor(parcel: Parcel) : this(parcel.readByte() == Integer.valueOf(1).toByte(), // as boolean value
                                               parcel.readInt(),
                                               parcel.readInt(),
                                               parcel.readByte() == Integer.valueOf(1).toByte(), // as boolean value
                                               parcel.readInt())

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel?,
                               flags: Int) {
        dest?.writeByte((if (stroke) 1 else 0).toByte()) // as boolean value
        dest?.writeInt(color)
        dest?.writeInt(weight)
        dest?.writeByte((if (fill) 1 else 0).toByte()) // as boolean value
        dest?.writeInt(fillColor)
    }

    class Builder {

        var stroke: Boolean = true
            private set

        @ColorInt
        var color: Int = Color.DKGRAY
            private set

        var weight: Int = 8
            private set

        var fill: Boolean = false
            private set

        @ColorInt
        var fillColor: Int = Color.TRANSPARENT
            private set

        @FloatRange(from = 0.0,
                    to = 1.0)
        private var opacity: Float = 1f

        @FloatRange(from = 0.0,
                    to = 1.0)
        private var fillOpacity: Float = 0.2f

        fun stroke(stroke: Boolean) =
                apply { this.stroke = stroke }

        fun color(@ColorInt
                  color: Int) =
                apply { this.color = color }

        fun color(colorString: String) =
                apply {
                    this.color = Color.parseColor(colorString)

                    val opacity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val opacity = Color.valueOf(this.color)
                                .alpha()
                        if (opacity == 1f) this.opacity else opacity
                    }
                    else {
                        this.opacity
                    }

                    opacity(opacity)
                }

        fun weight(weight: Int) =
                apply {
                    if (weight > 0) {
                        this.weight = weight
                    }
                }

        fun opacity(@FloatRange(from = 0.0,
                                to = 1.0)
                    opacity: Float) =
                apply {
                    if (opacity in 0.0..1.0) {
                        this.opacity = opacity
                        this.color = ColorUtils.setAlphaComponent(this.color,
                                                                  (opacity * 255).toInt())
                    }
                }

        fun fill(fill: Boolean) =
                apply {
                    this.fill = fill
                }

        fun fillColor(@ColorInt
                      fillColor: Int) =
                apply {
                    this.fillColor = fillColor
                }

        fun fillColor(fillColorString: String) =
                apply {
                    fill(true)
                    this.fillColor = Color.parseColor(fillColorString)

                    val fillOpacity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val fillOpacity = Color.valueOf(this.fillColor)
                                .alpha()
                        if (fillOpacity == 1f) this.fillOpacity else fillOpacity
                    }
                    else {
                        this.fillOpacity
                    }

                    fillOpacity(fillOpacity)
                }

        fun fillOpacity(@FloatRange(from = 0.0,
                                    to = 1.0)
                        fillOpacity: Float) =
                apply {
                    if (fillOpacity in 0.0..1.0) {
                        this.fillOpacity = fillOpacity
                        this.fillColor = ColorUtils.setAlphaComponent(this.fillColor,
                                                                      (fillOpacity * 255).toInt())
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

    companion object CREATOR : Parcelable.Creator<LayerStyleSettings> {
        override fun createFromParcel(parcel: Parcel): LayerStyleSettings {
            return LayerStyleSettings(parcel)
        }

        override fun newArray(size: Int): Array<LayerStyleSettings?> {
            return arrayOfNulls(size)
        }
    }
}