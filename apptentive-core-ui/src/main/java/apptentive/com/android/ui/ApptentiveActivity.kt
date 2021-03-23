package apptentive.com.android.ui

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity

abstract class ApptentiveActivity : AppCompatActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        overrideTheme()

        super.onCreate(savedInstanceState)
    }
}