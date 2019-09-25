package apptentive.com.android.core

import androidx.annotation.WorkerThread

typealias Observer<T> = (T) -> Unit

sealed class Observable<T>(private var _value: T) {
    private val observers = mutableListOf<Observer<T>>()

    open var value: T
        get() = _value
        protected set(value) {
            _value = value
            notifyObservers(value)
        }


    @WorkerThread
    fun observe(observer: Observer<T>): Subscription {
        observers.add(observer)
        notifyObserver(observer, value)

        return object : Subscription {
            override fun unsubscribe() {
                removeObserver(observer)
            }
        }
    }

    @WorkerThread
    fun removeObserver(observer: Observer<T>) {
        observers.remove(observer)
    }

    @WorkerThread
    private fun notifyObservers(value: T) {
        val temp = observers.toList()
        for (observer in temp) {
            notifyObserver(observer, value)
        }
    }

    private fun notifyObserver(observer: Observer<T>, value: T) {
        observer(value)
    }
}

class MutableObservable<T>(value: T) : Observable<T>(value) {
    override var value: T
        get() = super.value
        public set(value) {
            super.value = value
        }
}

interface Subscription {
    fun unsubscribe()
}