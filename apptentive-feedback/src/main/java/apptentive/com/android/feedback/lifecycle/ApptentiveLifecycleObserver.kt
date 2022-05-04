package apptentive.com.android.feedback.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import apptentive.com.android.concurrent.Executor
import apptentive.com.android.feedback.ApptentiveClient
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.InternalEvent
import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.LIFE_CYCLE_OBSERVER

internal class ApptentiveLifecycleObserver(
    val client: ApptentiveClient,
    private val stateExecutor: Executor,
    val refreshManifest: () -> Unit
) : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onBackground() {
        stateExecutor.execute {
            Log.d(LIFE_CYCLE_OBSERVER, "App is in background")
            client.engage(Event.internal(InternalEvent.APP_EXIT.labelName))
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onForeground() {
        stateExecutor.execute {
            Log.d(LIFE_CYCLE_OBSERVER, "App is in foreground")
            client.engage(Event.internal(InternalEvent.APP_LAUNCH.labelName))
            refreshManifest()
        }
    }
}
