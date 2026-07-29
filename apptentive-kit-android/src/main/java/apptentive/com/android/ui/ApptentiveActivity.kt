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
     * Applies system bar/cutout insets for screens where only the toolbar needs its background to
     * reach the true top/left/right edge - everything else (root and whatever content it holds,
     * e.g. a WebView) keeps the original plain behavior of being inset via root's own padding on
     * left/right/bottom, unchanged.
     *
     * Deliberately named differently from [applyWindowInsets] (rather than overloaded) even though
     * both can take three [View] arguments: a same-named 3-arg overload here would be ambiguous
     * with [applyWindowInsets]'s `(root, topBar, bottomBar)` overload called with zero
     * `sideInsetViews` - Kotlin's overload resolution would silently prefer this method's non-vararg
     * exact match over the vararg one, so a caller intending [applyWindowInsets]'s `bottomBar`
     * padding behavior would instead get this method's `topBarContainer` negative-margin behavior,
     * with no compiler error.
     *
     * Since [root] keeps its own left/right padding here (unlike [applyWindowInsets], where root
     * gets none), that padding narrows [topBarContainer] - and every `match_parent` descendant of
     * it, including a deeply-nested [topBar] - regardless of any padding added to [topBar] itself,
     * which only shrinks [topBar]'s *content*, not its outer bounds. A negative margin on
     * [topBarContainer] cancels root's padding at the one place it first applies, letting the
     * whole chain reach the true edge; [topBar]'s own padding then keeps its content (title/icon)
     * clear of the cutout within that now full-width bar.
     *
     * @param root the screen's root view; keeps absorbing left/right/bottom padding as before this
     * inset handling was added, so content below the toolbar (e.g. a WebView) is unaffected by
     * this method.
     * @param topBar the toolbar (or other top bar) whose content should stay clear of the
     * inset/cutout; receives top/left/right padding only.
     * @param topBarContainer root's direct child on [topBar]'s ancestor chain - pass [topBar]
     * itself when it has no wrapper (e.g. no surrounding `AppBarLayout`), or the wrapper view when
     * one exists. Receives a negative left/right margin equal to the current inset so it (and
     * everything nested inside it) escapes root's own left/right padding and reaches the true
     * edge. Defaults to [topBar] when omitted.
     */
    fun applyWindowInsetsKeepingRootPadding(root: View, topBar: View, topBarContainer: View = topBar) {
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
     * Applies system bar/cutout insets for screens whose top/bottom bars already carry a
     * brand-color background that's meant to extend into the system bar/cutout area (e.g.
     * Survey's toolbar and terms-and-conditions bar, or Message Center's toolbar and body). See
     * [applyWindowInsetsKeepingRootPadding] for why that's a separate method rather than a same-named
     * overload of this one.
     *
     * [root] itself gets no padding at all - each region pads itself so its own background, not
     * root's, shows through behind the system bars/cutout on every edge it touches. [topBar] and
     * [bottomBar] also need left/right (not just top/bottom) so their color reaches the same edge
     * as everyone else when the cutout/nav bar lands on a side edge, e.g. in landscape - otherwise
     * root's background would still peek through between the bar's un-padded side and that edge.
     * Each view's own pre-existing padding (e.g. from styling) is preserved and added to, not
     * overwritten.
     *
     * @param root the screen's root view; receives no padding of its own here, since [topBar],
     * [bottomBar], and [sideInsetViews]/[fullBleedViews] together are expected to cover the whole
     * screen.
     * @param topBar the top bar (e.g. a toolbar); receives top/left/right padding on top of
     * whatever padding it already had.
     * @param bottomBar the bottom bar or main content area below the toolbar (e.g. a
     * terms-and-conditions bar, or a body view holding a message list/composer); receives
     * bottom/left/right padding on top of whatever padding it already had.
     * @param sideInsetViews additional views (e.g. other content areas between [topBar] and
     * [bottomBar]) that only need left/right padding to stay clear of a side cutout/nav bar, added
     * on top of whatever padding each already had.
     * @param fullBleedViews views that should visually span edge-to-edge (e.g. a full-width
     * divider) regardless of a side cutout/nav bar, deliberately opting out of avoiding it. Since
     * they're laid out inside an ancestor that already pads for the inset, they're pulled back out
     * to the true edge with a matching negative margin rather than being excluded from that
     * ancestor's padding. Defaults to none.
     */
    fun applyWindowInsets(
        root: View,
        topBar: View,
        bottomBar: View,
        vararg sideInsetViews: View,
        fullBleedViews: List<View> = emptyList()
    ) {
        // Captured once, before the listener ever runs, so a view's own styled padding (e.g. the
        // Profile body's form margins) is preserved and added to, not overwritten - and so repeated
        // dispatches (rotation, etc.) add the inset on top of that same original base each time
        // instead of compounding on top of the previous call's result.
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
        // Combined with systemBars so the padding also covers a display cutout (notch/punch-hole)
        // wherever its safe inset is larger than the system bar inset on that side - e.g. a side
        // cutout in landscape, or any cutout when the status bar is hidden.
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
