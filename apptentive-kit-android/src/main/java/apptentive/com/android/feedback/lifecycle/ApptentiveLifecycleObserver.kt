package apptentive.com.android.feedback.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import apptentive.com.android.core.LogTags.LIFE_CYCLE_OBSERVER
import apptentive.com.android.core.concurrent.Executor
import apptentive.com.android.feedback.ApptentiveClient
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.InternalEvent
import apptentive.com.android.util.Log

internal class ApptentiveLifecycleObserver(
    val client: ApptentiveClient,
    private val stateExecutor: Executor,
    private val onForeground: () -> Unit,
    private val onBackground: () -> Unit,
) : DefaultLifecycleObserver {

    override fun onStop(owner: LifecycleOwner) {
        stateExecutor.execute {
            Log.d(LIFE_CYCLE_OBSERVER, "App is in background")
            client.engage(Event.internal(InternalEvent.APP_EXIT.labelName))
            onBackground()
        }
        super.onStop(owner)
    }

    // App is in the foreground
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        stateExecutor.execute {
            Log.d(LIFE_CYCLE_OBSERVER, "App is in foreground")
            onForeground()
        }
    }
}
