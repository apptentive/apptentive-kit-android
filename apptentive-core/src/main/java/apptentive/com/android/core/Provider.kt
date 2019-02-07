package apptentive.com.android.core

import apptentive.com.android.util.Log
import apptentive.com.android.util.LogTags.injection

interface Providable

object Provider {
    val lookup = mutableMapOf<Class<*>, Providable>()

    init {
        // FIXME: this should be configured outside of this class
        register<MainQueueChecker>(MainQueueCheckerImpl())
        register<PlatformLogger>(PlatformLoggerImpl("Apptentive"))
    }

    inline fun <reified T : Providable> register(providable: T) {
        lookup[T::class.java] = providable
        Log.w(injection, "Register providable: ${T::class.java} (${providable.javaClass})")
    }

    fun clear() {
        lookup.clear()
    }

    inline fun <reified T : Providable> of(): T {
        val providable = lookup[T::class.java] ?: throw IllegalArgumentException("Providable is not registered: ${T::class.java}")
        return providable as T
    }
}
