package apptentive.com.android.convert

import apptentive.com.android.concurrent.AsyncPromise
import apptentive.com.android.concurrent.Executors
import apptentive.com.android.concurrent.Promise

/**
 * An interface responsible for object serialization without
 * specifying any underlying implementation details.
 */
interface Serialization {
    fun serializeObject(obj: Any): Promise<Unit>
    fun deserializeObject(): Promise<Any>
}

/**
 * A concrete [Serialization] implementation responsible for an async
 * object serialization with loose implementation coupling.
 * @param executors executors for dispatching tasks.
 * @param serializer object responsible for serialization.
 * @param deserializer object responsible for deserialization.
 */
class SerializationImpl(
    private val executors: Executors,
    private val serializer: Serializer,
    private val deserializer: Deserializer
) : Serialization {
    override fun serializeObject(obj: Any): Promise<Unit> {
        val promise = AsyncPromise<Unit>(executors.callback)
        executors.io.execute {
            try {
                serializeObjectSync(obj)
                promise.resolve(Unit)
            } catch (e: Exception) {
                promise.reject(e)
            }
        }
        return promise
    }

    override fun deserializeObject(): Promise<Any> {
        val promise = AsyncPromise<Any>(executors.callback)
        executors.io.execute {
            try {
                val obj = deserializeObjectSync()
                promise.resolve(obj)
            } catch (e: Exception) {
                promise.reject(e)
            }
        }
        return promise
    }

    private fun serializeObjectSync(obj: Any) {
        serializer.serialize(obj)
    }

    private fun deserializeObjectSync(): Any {
        return deserializer.deserialize()
    }
}