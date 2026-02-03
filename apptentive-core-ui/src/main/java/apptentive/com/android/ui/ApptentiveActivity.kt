package apptentive.com.android.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
abstract class ApptentiveActivity : AppCompatActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        overrideTheme()

        super.onCreate(savedInstanceState)
    }

    fun applyWindowInsets(root: View) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            val leftInset = systemBarsInsets.left
            val rightInset = systemBarsInsets.right
            val topInset = systemBarsInsets.top
            val bottomInset = if (imeVisible) imeInsets.bottom else systemBarsInsets.bottom

            v.setPadding(
                leftInset,
                topInset,
                rightInset,
                bottomInset
            )

            WindowInsetsCompat.CONSUMED
        }
    }
}
