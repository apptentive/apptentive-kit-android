package apptentive.com.android.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_UNSPECIFIED

open class ApptentiveViewModelActivity<VM : ApptentiveViewModel> : ApptentiveActivity() {
    // instance id must be passed with the intent when starting the activity and will be used to
    // resolve an appropriate ViewModel factory
    private val _viewModel: ApptentiveViewModel by viewModels {
        val instanceId =
            intent.getStringExtra(EXTRA_VIEW_MODEL_INSTANCE_ID) ?: throw IllegalArgumentException(
                "Missing '$EXTRA_VIEW_MODEL_INSTANCE_ID' extra"
            )
        InteractionViewModelFactoryProvider.getViewModelFactory(instanceId)
    }

    @Suppress("UNCHECKED_CAST")
    protected val viewModel: VM
        get() = _viewModel as VM

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set the local night mode to the same value as the parent context has
        delegate.localNightMode = intent.getIntExtra(EXTRA_LOCAL_DARK_MODE, MODE_NIGHT_UNSPECIFIED)
    }

    companion object {
        const val EXTRA_VIEW_MODEL_INSTANCE_ID =
            "apptentive.intent.extra.EXTRA_VIEW_MODEL_INSTANCE_ID"
        const val EXTRA_LOCAL_DARK_MODE =
            "apptentive.intent.extra.EXTRA_LOCAL_DARK_MODE"
    }
}

inline fun <reified T : ApptentiveViewModelActivity<*>> Context.startViewModelActivity(
    instanceId: String,
    extras: Bundle? = null
) {
    val intent = Intent(this, T::class.java)
    intent.putExtra(ApptentiveViewModelActivity.EXTRA_VIEW_MODEL_INSTANCE_ID, instanceId)
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