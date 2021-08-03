package apptentive.com.android.feedback.lifecycle

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import apptentive.com.android.feedback.ApptentiveClient
import apptentive.com.android.feedback.LIFE_CYCLE_OBSERVER
import apptentive.com.android.feedback.engagement.Event
import apptentive.com.android.feedback.engagement.InternalEvent
import apptentive.com.android.util.Log

internal class ApptentiveLifecycleObserver(val client: ApptentiveClient, val context: Context) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    @WorkerThread
    fun onBackground() {
        Log.d(LIFE_CYCLE_OBSERVER, "App is in background")
        client.engage(context, Event.internal(InternalEvent.APP_EXIT.labelName))
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    @WorkerThread
    fun onForeground() {
        Log.d(LIFE_CYCLE_OBSERVER, "App is in foreground")
        client.engage(context, Event.internal(InternalEvent.APP_LAUNCH.labelName))
    }
}
