package fr.geonature.maps.sample.ui

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import fr.geonature.maps.sample.R
import fr.geonature.maps.util.ThemeUtils.getPrimaryDarkColor

/**
 * Handles edge-to-edge enforcement (Android 15+, API 35) for every activity by applying system
 * bars and IME insets as padding on the content root.
 */
class EdgeToEdgeCallbacks : Application.ActivityLifecycleCallbacks {

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    }

    override fun onActivityStarted(activity: Activity) {
        val content = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        val hasActionBar = TypedValue().let {
            activity.theme.resolveAttribute(androidx.appcompat.R.attr.windowActionBar, it, true)
            it.data != 0
        }
        val isNightMode = (activity.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        // with an action bar, the status bar blends with it (dark primary color); otherwise it follows the app theme
        WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            .isAppearanceLightStatusBars = !hasActionBar && !isNightMode

        val scrim = (content as? FrameLayout)?.let { addStatusBarScrim(activity, it, hasActionBar) }

        // the decor action bar is drawn over the content frame, which then needs to be pushed below it
        val actionBarContainer =
            if (hasActionBar) activity.findViewById<View>(androidx.appcompat.R.id.action_bar_container)
            else null
        var systemInsets = Insets.NONE

        fun applyInsets() {
            val top = maxOf(systemInsets.top, actionBarContainer?.bottom ?: 0)
            content.setPadding(systemInsets.left, top, systemInsets.right, systemInsets.bottom)
            scrim?.layoutParams?.height = systemInsets.top
            scrim?.translationY = -top.toFloat()
            scrim?.requestLayout()
        }

        content.clipToPadding = false
        actionBarContainer?.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom != oldBottom) applyInsets()
        }
        ViewCompat.setOnApplyWindowInsetsListener(content) { _, windowInsets ->
            systemInsets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
                        or WindowInsetsCompat.Type.ime()
            )
            applyInsets()

            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(content)
    }

    private fun addStatusBarScrim(
        activity: Activity,
        content: FrameLayout,
        hasActionBar: Boolean
    ): View {
        (content.findViewById<View>(R.id.status_bar_scrim))?.let { return it }

        val backgroundColor = TypedValue().let {
            activity.theme.resolveAttribute(android.R.attr.colorBackground, it, true)
            it.data
        }

        return View(activity).apply {
            id = R.id.status_bar_scrim
            setBackgroundColor(if (hasActionBar) getPrimaryDarkColor(activity) else backgroundColor)
            elevation = 100f
            content.addView(
                this,
                FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, Gravity.TOP)
            )
        }
    }

    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
