package apptentive.com.android.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

internal abstract class ApptentiveActivity : AppCompatActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        overrideTheme()

        super.onCreate(savedInstanceState)
    }

    fun applyWindowInsets(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val padding = resolveWindowInsetPadding(insets)

            v.setPadding(
                padding.left,
                padding.top,
                padding.right,
                padding.bottom
            )

            WindowInsetsCompat.CONSUMED
        }
    }

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
