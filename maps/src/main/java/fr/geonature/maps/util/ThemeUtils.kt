package fr.geonature.maps.util

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.annotation.ColorInt

/**
 * Helper class about application theme.
 *
 * @author S. Grimault
 */
object ThemeUtils {

    @ColorInt
    fun getPrimaryColor(context: Context): Int {
        return getColor(
            context,
            androidx.appcompat.R.attr.colorPrimary
        )
    }

    @ColorInt
    fun getPrimaryDarkColor(context: Context): Int {
        return getColor(
            context,
            androidx.appcompat.R.attr.colorPrimaryDark
        )
    }

    @ColorInt
    fun getAccentColor(context: Context): Int {
        return getColor(
            context,
            androidx.appcompat.R.attr.colorAccent
        )
    }

    @ColorInt
    private fun getColor(
        context: Context,
        colorAttribute: Int
    ): Int {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(colorAttribute))
        val color = typedArray.getColor(
            0,
            0
        )

        typedArray.recycle()

        // If the color could not be resolved (returns 0), the context is likely an ApplicationContext
        // which has no theme applied. In that case, wrap it with the app's declared theme resource
        // (from ApplicationInfo) so that theme attributes like colorAccent can be resolved correctly.
        if (color == 0) {
            val appThemeResId = context.applicationInfo.theme
            if (appThemeResId != 0) {
                val themedContext = ContextThemeWrapper(context, appThemeResId)
                val themedArray = themedContext.theme.obtainStyledAttributes(intArrayOf(colorAttribute))
                val themedColor = themedArray.getColor(0, 0)
                themedArray.recycle()
                return themedColor
            }
        }

        return color
    }
}
