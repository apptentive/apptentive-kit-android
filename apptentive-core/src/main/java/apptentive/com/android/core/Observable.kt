package apptentive.com.android.core

import apptentive.com.android.util.InternalUseOnly

open class Observable<T>(private var _value: T) {
    private val observers = mutableSetOf<(T) -> Unit>()

    open var value: T
        get() = _value
        protected set(value) {
            _value = value
            notifyObservers(value)
        }

    fun observe(observer: (T) -> Unit): Subscription {
        observers.add(observer)
        notifyObserver(observer, value)

        return object : Subscription {
            override fun unsubscribe() {
                removeObserver(observer)
            }
        }
    }

    fun removeObserver(observer: (T) -> Unit) {
        observers.remove(observer)
    }

    private fun notifyObservers(value: T) {
        val temp = observers.toList()
        for (observer in temp) {
            notifyObserver(observer, value)
        }
    }

    private fun notifyObserver(observer: (T) -> Unit, value: T) {
        observer(value)
    }
}

@InternalUseOnly
interface Subscription {
    fun unsubscribe()
}
