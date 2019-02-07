package apptentive.com.android.core

import android.app.Application

interface Providable

object Provider {
    val lookup = mutableMapOf<Class<*>, Providable>()

    fun register(application: Application) {
        // FIXME: this should be configured outside of this class
        register(createPlatformLogger())
        register(createExecutionQueueFactory())
    }

    inline fun <reified T : Providable> register(providable: T) {
        lookup[T::class.java] = providable
    }

    fun clear() {
        lookup.clear()
    }

    inline fun <reified T : Providable> of(): T {
        val providable = lookup[T::class.java] ?: throw IllegalArgumentException("Providable is not registered: ${T::class.java}")
        return providable as T
    }
}
