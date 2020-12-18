package apptentive.com.android.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels

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

    companion object {
        const val EXTRA_VIEW_MODEL_INSTANCE_ID =
            "apptentive.intent.extra.EXTRA_VIEW_MODEL_INSTANCE_ID"
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
    if (this !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }
    startActivity(intent)
}