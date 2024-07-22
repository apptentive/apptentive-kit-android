package apptentive.com.android.core

import apptentive.com.android.util.InternalUseOnly

/** Service Locator design pattern implementation. Provides dependency based on a class object. */
@InternalUseOnly
object DependencyProvider {
    val lookup = mutableMapOf<Class<*>, Provider<*>>()

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

    inline fun <reified T> isRegistered() =
        lookup[T::class.java] != null

    inline fun <reified T> of(): T {
        val provider = lookup[T::class.java] ?: throw MissingProviderException("Provider is not registered: ${T::class.java}")
        return provider.get() as T
    }
}

class MissingProviderException(message: String, cause: Throwable? = null) : ApptentiveException(message, cause)
