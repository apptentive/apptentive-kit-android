package apptentive.com.android.core

import androidx.lifecycle.LiveData

fun <T> Observable<T>.asLiveData(): LiveData<T> = ObservableLiveData(this)

private class ObservableLiveData<T>(private val target: Observable<T>) : LiveData<T>() {
    private var subscription: Subscription? = null

    override fun onActive() {
        subscription = target.observe { value ->
            postValue(value)
        }
    }

    override fun onInactive() {
        subscription?.unsubscribe()
        subscription = null
    }
}