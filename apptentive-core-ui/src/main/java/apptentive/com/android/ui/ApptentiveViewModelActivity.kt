package apptentive.com.android.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_UNSPECIFIED
import apptentive.com.android.util.InternalUseOnly

@InternalUseOnly
open class ApptentiveViewModelActivity : ApptentiveActivity() {
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        // set the local night mode to the same value as the parent context has
        delegate.localNightMode = intent.getIntExtra(EXTRA_LOCAL_DARK_MODE, MODE_NIGHT_UNSPECIFIED)

        super.onCreate(savedInstanceState)
    }

    companion object {
        const val EXTRA_LOCAL_DARK_MODE =
            "apptentive.intent.extra.EXTRA_LOCAL_DARK_MODE"
    }
}

@InternalUseOnly
inline fun <reified T : ApptentiveViewModelActivity> Context.startViewModelActivity(
    extras: Bundle? = null
) {
    val intent = Intent(this, T::class.java)
    if (extras != null) {
        intent.putExtras(extras)
    }

    // in case if the parent context is AppCompatActivity - we can check it's [localNightMode] flag
    // and pass it to our activity
    if (this is AppCompatActivity) {
        intent.putExtra(ApptentiveViewModelActivity.EXTRA_LOCAL_DARK_MODE, delegate.localNightMode)
    }
    // if the event was engaged with the Application context, then start activity as a new task
    else if (this !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }
    startActivity(intent)
}