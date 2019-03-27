package apptentive.com.android.core

import android.app.Application

object DependencyProvider {
    val lookup = mutableMapOf<Class<*>, Provider<*>>()

    fun register(@Suppress("UNUSED_PARAMETER") application: Application) {
        // FIXME: this should be configured outside of this class
        register(PlatformLoggerProvider("Apptentive"))
        register(ExecutorQueueFactoryProvider())
    }

    inline fun <reified T> register(provider: Provider<T>) {
        lookup[T::class.java] = provider
    }

    inline fun <reified T> register(target: T) {
        lookup[T::class.java] = object : Provider<T> {
            override fun get(): T = target
        }
    }

    fun clear() {
        lookup.clear()
    }

    inline fun <reified T> of(): T {
        val provider = lookup[T::class.java] ?: throw IllegalArgumentException("Provider is not registered: ${T::class.java}")
        return provider.get() as T
    }
}