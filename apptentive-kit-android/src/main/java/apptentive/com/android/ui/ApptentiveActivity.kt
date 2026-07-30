package apptentive.com.android.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding

internal abstract class ApptentiveActivity : AppCompatActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        overrideTheme()

        super.onCreate(savedInstanceState)
    }

    /**
     * Applies insets for screens where only the toolbar needs to reach the true edge;
     * [root] (WebView) keep the original behavior of being inset via root's own
     * padding.
     *
     * @param root screen's root view; keeps absorbing left/right/bottom padding.
     * @param topBar toolbar whose content should stay clear of the inset; gets top/left/right
     * padding.
     * @param topBarContainer [root]'s direct child on [topBar]'s ancestor chain (or [topBar] itself
     * if it has no wrapper, e.g. no surrounding `AppBarLayout`). Gets a negative left/right margin
     * so it - and everything nested inside it - escapes [root]'s padding and reaches the true edge,
     * while [topBar]'s own padding keeps its content clear of the inset within that full-width bar.
     */
    fun applyWindowInsetsForWebView(root: View, topBar: View, topBarContainer: View = topBar) {
        val rootBase = root.capturePadding()
        val topBarBase = topBar.capturePadding()

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val padding = resolveWindowInsetPadding(insets)

            root.updatePadding(
                left = rootBase.left + padding.left,
                right = rootBase.right + padding.right,
                bottom = rootBase.bottom + padding.bottom
            )
            topBar.updatePadding(
                left = topBarBase.left + padding.left,
                top = topBarBase.top + padding.top,
                right = topBarBase.right + padding.right
            )
            topBarContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                marginStart = -padding.left
                marginEnd = -padding.right
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    /**
     * Applies insets for screens whose bars already carry a brand-color background meant to
     * extend into the system bar/cutout area (e.g. Survey's toolbar and terms-and-conditions bar).
     *
     * @param root screen's root view; gets no padding of its own, since the other parameters
     * together are expected to cover the whole screen.
     * @param topBar top bar (e.g. a toolbar); gets top/left/right padding.
     * @param bottomBar bottom bar or main content below the toolbar (e.g. a terms-and-conditions
     * bar, or a body view holding a message list/composer); gets bottom/left/right padding.
     * @param sideInsetViews other content between [topBar] and [bottomBar] that only needs
     * left/right padding.
     * @param fullBleedViews views that should span edge-to-edge regardless of a side cutout/nav
     * bar, via a matching negative margin instead of padding. Defaults to none.
     */
    fun applyWindowInsets(
        root: View,
        topBar: View,
        bottomBar: View,
        vararg sideInsetViews: View,
        fullBleedViews: List<View> = emptyList()
    ) {
        val topBarBase = topBar.capturePadding()
        val bottomBarBase = bottomBar.capturePadding()
        val sideInsetBase = sideInsetViews.map { it.capturePadding() }

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val padding = resolveWindowInsetPadding(insets)

            topBar.updatePadding(
                left = topBarBase.left + padding.left,
                top = topBarBase.top + padding.top,
                right = topBarBase.right + padding.right
            )
            bottomBar.updatePadding(
                left = bottomBarBase.left + padding.left,
                right = bottomBarBase.right + padding.right,
                bottom = bottomBarBase.bottom + padding.bottom
            )
            sideInsetViews.forEachIndexed { index, view ->
                val base = sideInsetBase[index]
                view.updatePadding(left = base.left + padding.left, right = base.right + padding.right)
            }
            fullBleedViews.forEach {
                it.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    marginStart = -padding.left
                    marginEnd = -padding.right
                }
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun View.capturePadding() = InsetPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)

    internal companion object {
        internal fun resolveWindowInsetPadding(insets: WindowInsetsCompat): InsetPadding {
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val systemBarsAndCutoutInsets = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            return InsetPadding(
                left = systemBarsAndCutoutInsets.left,
                top = systemBarsAndCutoutInsets.top,
                right = systemBarsAndCutoutInsets.right,
                bottom = if (imeVisible) imeInsets.bottom else systemBarsAndCutoutInsets.bottom
            )
        }
    }
}

internal data class InsetPadding(val left: Int, val top: Int, val right: Int, val bottom: Int)
